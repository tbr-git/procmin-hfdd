import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HFDDSession } from '../../hfdd/hfddsession';
import { HFDDSessionService } from '../hfddsession.service';
import * as d3 from 'd3';
import * as d3Sankey from 'd3-sankey';
import { ScaleSequential } from 'd3';
import { HFDDSkGraph } from '../../hfdd/data/sankey/HFDDSkGraph';
import { HFDDFlowGraph, HFDDSLink, HFDDSNode, hfdd_connect_sankey } from '../../hfdd/data/sankey/SankeyConnector';
import { catchError, combineLatest, filter, last, map, Observable, of, Subscription, switchMap, throwError } from 'rxjs';
import { UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'app-cornerstone-graph',
  templateUrl: './cornerstone-graph.component.html',
  styleUrls: ['./cornerstone-graph.component.sass']
})
export class CornerstoneGraphComponent implements OnInit, AfterViewInit, OnDestroy {

  hfddSess: HFDDSession | undefined = undefined;

  @ViewChild('chartContainer') 
  private chartContainerElement!: ElementRef;

  private heightValue = 800;

  private widthValue = 1200;

  private margin: { top: number, bottom: number, left: number; right: number} =  {top: 20, bottom: 30, left: 30, right: 20};

  /**
   * Sankey vertex width
   */
  private nodeWidth = 15;
  
  /**
   * Padding for the sankey vertex label
   */
  private nodeLabelPadding = 6; 

  /**
   * Padding of the Sankey vertex
   */
  private nodePadding = 8; 
  
  /**
   * Maximum flow value
   */
  private maxFlowVal = 0.5

  /**
   * TODO not any
   */
  private svgContent: any;

  private svgGLinks: any;

  private svgGVertices: any;

  private svgLabels: any;

  private skData : HFDDSkGraph | undefined;

  /**
   * Color map for the coloring of the flows according to the costs
   */
  private costColorMap : ScaleSequential<string, never>;

  /**
   * Coloring the variants
   */
  private variantColorMap : d3.ScaleOrdinal<string, string, never>;

  private slgHFDDGraph: d3Sankey.SankeyLayout<HFDDFlowGraph, HFDDSNode, HFDDSLink>;

  private graphUpdateSub: Subscription|undefined;

  visualizationTypeControl = new UntypedFormControl('probability');

  private visualizationTypeChanges: Subscription|undefined = undefined;

  private visualizationType: string = 'probability';

  private hfddFlowGraph: HFDDFlowGraph|undefined = undefined;

  constructor(private route: ActivatedRoute, private hfddSessService: HFDDSessionService) {
    this.costColorMap = d3.scaleSequential().domain([0, this.maxFlowVal]).interpolator(d3.interpolateViridis);
    this.variantColorMap = d3.scaleOrdinal(d3.schemeCategory10);

    this.slgHFDDGraph = 
      d3Sankey.sankey<HFDDFlowGraph, HFDDSNode, HFDDSLink>();
    // Set the sankey diagram properties
    this.slgHFDDGraph = this.slgHFDDGraph
      .nodeWidth(this.nodeWidth)
      .nodePadding(this.nodePadding)
      .extent([[this.margin.left, this.margin.top], [this.widthValue - this.margin.right, this.heightValue - this.margin.bottom]]);

    // Data and id accessor
    this.slgHFDDGraph = this.slgHFDDGraph.nodeId((v: HFDDSNode) => v.nodeId)
      .nodes((g: HFDDFlowGraph) => g.hfddNodes)
      .links((g: HFDDFlowGraph) => g.hfddLinks);

    // Positioning 
    this.slgHFDDGraph = this.slgHFDDGraph
      .nodeAlign(n => n.skLevel)
      .nodeSort((u, v) => (u.intraLevelKey - v.intraLevelKey));
  }

  ngOnInit(): void {
    this.visualizationTypeChanges = this.visualizationTypeControl.valueChanges.subscribe(v => {
      this.visualizationType = v;
      this.updateChart();
    })
  }  


  ngAfterViewInit(): void {
    /*
        First create a d3 selection for the svg element
        that we added in the .html file
      */
    const svgContainer = d3.select(this.chartContainerElement?.nativeElement);

    ////////////////////////////////////////////////////////////////////////////////
    // Create Basic SVG structure
    ////////////////////////////////////////////////////////////////////////////////
    this.svgContent = svgContainer.append("svg")
      .attr("viewBox", `0 0 ${this.widthValue} ${this.heightValue}`)
      .attr("width", "100%")
      .attr("height", "100%")
      .append('g')
        .attr('transform', `translate(${this.margin.left},${this.margin.top})`);
      
      var gGraph = this.svgContent.append("g");
      this.svgContent.call(d3.zoom().on("zoom", function handleZoom(event) {
        gGraph.attr("transform", event.transform)
      }));

      this.svgGLinks = gGraph.append("g").attr("fill", "none");
      this.svgGVertices = gGraph.append("g").attr("stroke-width", 3).attr("stroke", "#808080");
      this.svgLabels = gGraph.append("g")
        .style("stroke-width", 0)
        .style("fill", "#ffffff")
        .style("font", "italic 13px sans-serif");

    this.graphUpdateSub = this.route.parent?.params
      .pipe(
        switchMap( params => {
          const sessId = params['sessId'];

          let hfddSessUnresolved = this.hfddSessService.getSessionById(sessId);

          if (hfddSessUnresolved) {
            this.hfddSess = hfddSessUnresolved;
            return combineLatest([of(hfddSessUnresolved.sessId), hfddSessUnresolved.cornerstoneSpecSubject$]);
          }
          return throwError(() => Error("session undefined"));
        }),
        filter(([sessId, csSpec]) => csSpec.cornerstoneVertices.length > 0),
        switchMap(([sessId, csSpec]) => this.hfddSessService.getCornerstoneSankey(sessId, csSpec.cornerstoneVertices)),
        map((res: any) => hfdd_connect_sankey(undefined, undefined, res))
      )
      .subscribe(
        {
          next: (graph: HFDDFlowGraph) => {
            console.log('New cornerstone graph');
            this.hfddFlowGraph = graph;
            this.updateChart();
          },
          error: (e) => console.error(e)
        });
  }

  ngOnDestroy() : void {
    this.graphUpdateSub?.unsubscribe();
    this.visualizationTypeChanges?.unsubscribe();
  }

  updateChart() {
    console.log("Updateing Cornerstone chart");
    if (this.hfddFlowGraph == undefined) {
      return;
    }
    var plotData: HFDDFlowGraph = this.hfddFlowGraph;
    if (this.visualizationType == 'probability') {
      plotData.hfddLinks.map(e => e.value = e.probabilityValue);
    }
    if (this.visualizationType == 'matching') {
      plotData = {
        hfddNodes: plotData?.hfddNodes.filter(v => v.matchingRelevant),
        hfddLinks: plotData?.hfddLinks.filter(e => e.matchingRelevant)
      }
      plotData.hfddLinks.map(e => e.value = e.matchingValue);
    }

    console.log("Drawing CS Graph");
    const sGraph: d3Sankey.SankeyGraph<HFDDSNode, HFDDSLink> = this.slgHFDDGraph(plotData);
      // Add in the links
      var dataJoinLinks = this.svgGLinks
        .selectAll("path")
        .data(sGraph.links, (e: HFDDSLink) => e.edgeId);
      dataJoinLinks
        .enter().append("path")
          .attr("class", ".sklink")
          .attr("d",  d3Sankey.sankeyLinkHorizontal())
          .attr("stroke", (l:HFDDSLink) => this.costColorMap(l.cost))
          .attr("stroke-opacity", 0.5)
          .attr("stroke-width", (l:HFDDSLink) => Math.max(1, l.width || 1))
          .sort(function(a: { dy: number; }, b: { dy: number; })  { return b.dy - a.dy; });

      dataJoinLinks.transition()
          .attr("d",  d3Sankey.sankeyLinkHorizontal())
          .attr("stroke-width", (l:HFDDSLink) => Math.max(1, l.width || 1));

      dataJoinLinks.exit().remove();

      var dataJoinNodes = this.svgGVertices
        .selectAll("rect")
        .data(sGraph.nodes, (v: HFDDSNode) => v.nodeId);
      dataJoinNodes
        .enter().append("rect")
          .attr("class", ".sknode")
          .attr("x", (d: HFDDSNode) => d.x0)
          .attr("y", (d: HFDDSNode) => d.y0)
          .attr("height", (d: HFDDSNode) => (d.y1 && d.y0) ? d.y1 - d.y0 : 3)
          .attr("width", (d: HFDDSNode) => (d.x1 && d.x0) ? d.x1 - d.x0 : 3)
          .attr("fill", (d: HFDDSNode) => this.getNodeColor(d, this.variantColorMap));

      dataJoinNodes.transition()
          .attr("x", (d: HFDDSNode) => d.x0)
          .attr("y", (d: HFDDSNode) => d.y0)
          .attr("height", (d: HFDDSNode) => (d.y1 && d.y0) ? d.y1 - d.y0 : 3)
          .attr("width", (d: HFDDSNode) => (d.x1 && d.x0) ? d.x1 - d.x0 : 3)
          .attr("fill", (d: HFDDSNode) => this.getNodeColor(d, this.variantColorMap));

      dataJoinNodes.exit().remove();

      var nodesWithLabel = sGraph.nodes.filter((v : HFDDSNode) => v.activities);
      var dataJoinLabel = this.svgLabels
        .selectAll("text")
        .data(nodesWithLabel, (v: HFDDSNode) => v.nodeId);
      dataJoinLabel
        .enter().append("text")
          .attr("x", (d: HFDDSNode) => (d.x0 && d.x1) ? (d.left ?  d.x0 - this.nodeLabelPadding : d.x1 + this.nodeLabelPadding) : 0)
          .attr("y", (d: HFDDSNode) => (d.y0 && d.y1) ? ((d.y1 + d.y0) / 2) : 0)
          .attr("dy", "0.35em")
          .attr("text-anchor", (d: HFDDSNode) => d.left ? "end" : "start")
          .text((d: HFDDSNode)=> `${(d.type == 'trace') ? "<" : "{"}${d.activities ? d.activities.join(",") : ""}${(d.type == 'trace') ? ">" : "}"}`);
          
      dataJoinLabel.transition()
        .attr("x", (d: HFDDSNode) => (d.x0 && d.x1) ? (d.left ?  d.x0 - this.nodeLabelPadding : d.x1 + this.nodeLabelPadding) : 0)
        .attr("y", (d: HFDDSNode) => (d.y0 && d.y1) ? ((d.y1 + d.y0) / 2) : 0);

      dataJoinLabel.exit().remove();
  }

  getNodeColor(n : HFDDSNode, variantColorMap : d3.ScaleOrdinal<string, string, never>) {
    if (n.variant) {
      return variantColorMap(n.variant.toString()); 
    }
    else if (n.type == 'itemset') {
      return "steel blue";
    }
    else {
      return "gray";
    }
  }


}
