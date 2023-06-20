import { Component, ElementRef, Input, OnInit, OnChanges, SimpleChanges, ViewChild, AfterViewChecked, AfterViewInit } from '@angular/core';
import * as d3 from 'd3';
import * as d3Sankey from 'd3-sankey';
import { ScaleSequential } from 'd3';
import { HFDDSkGraph } from '../../../../hfdd/data/sankey/HFDDSkGraph';
import { HFDDSession } from '../../../../hfdd/hfddsession';
import { HFDDSessionService } from '../../../hfddsession.service';
import { last, map } from 'rxjs';
import { HFDDFlowGraph, HFDDSLink, HFDDSNode, hfdd_connect_sankey } from '../../../../hfdd/data/sankey/SankeyConnector';

@Component({
  selector: 'app-itemset-difference-view',
  templateUrl: './itemset-difference-view.component.html',
  styleUrls: ['./itemset-difference-view.component.sass']
})
export class ItemsetDifferenceViewComponent implements OnInit, AfterViewInit, OnChanges {

  /**
   * Handle to current session
   */
  @Input() hfddSession: HFDDSession | undefined = undefined;

  /**
   * Current HFDD iteration
   */
  @Input() iteration: number | undefined = undefined;

  /**
   * Currently selected vertex
   */
  @Input() vertexId: number | undefined = undefined;

  @ViewChild('chartContainer') 
  private chartContainerElement!: ElementRef;

  private heightValue = 400;

  private widthValue = 600;

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

  constructor(private hfddService: HFDDSessionService) { 
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
  }

  ngOnInit(): void {
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
    
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.hfddSession && this.iteration !== undefined && this.vertexId !== undefined) {
      //const graph_data$ = this.hfddService.getIntraItemsetSankey(this.hfddSession.sessId, this.iteration, this.vertexId)
      //  .subscribe(res => {
      //    console.log(res);
      //  })
      const graph_data$ = this.hfddService.getIntraItemsetSankey(this.hfddSession.sessId, this.iteration, this.vertexId).pipe(
        last(),
        map(res => hfdd_connect_sankey(this.vertexId, this.iteration, res))
      );
      graph_data$.subscribe((g:HFDDFlowGraph) => {
        this.updateChart(g);
      });
    }
  }

  updateChart(graphData : HFDDFlowGraph) {
    const sGraph: d3Sankey.SankeyGraph<HFDDSNode, HFDDSLink> = this.slgHFDDGraph(graphData);
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
          .text((d: HFDDSNode)=> `${(d.type == 'trace') ? "<" : "{"}
            ${d.activities ? d.activities.join(",") : ""}${(d.type == 'trace') ? ">" : "}"}`);
          
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
