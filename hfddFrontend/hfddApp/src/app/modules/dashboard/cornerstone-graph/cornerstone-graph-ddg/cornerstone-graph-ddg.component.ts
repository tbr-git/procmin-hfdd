import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import * as d3 from 'd3';
import { BehaviorSubject, catchError, combineLatest, filter, iif, map, mergeMap, observable, Observable, of, ReplaySubject, Subscription, switchMap, throwError } from 'rxjs';
import { DDGBuilder } from 'src/app/modules/hfdd/data/dd-graph/DDGBuilder';
import { DDGEdge } from 'src/app/modules/hfdd/data/dd-graph/edge/DDGEdge';
import { DDGPlotter } from 'src/app/modules/hfdd/data/dd-graph/DDGPlotter';
import { DDGraph } from 'src/app/modules/hfdd/data/dd-graph/DDGraph';
import { DDGVertex } from 'src/app/modules/hfdd/data/dd-graph/vertex/DDGVertex';
import { HFDDSessionService } from '../../hfddsession.service';
import { ActivatedRoute, Params } from '@angular/router';
import { HFDDSession } from 'src/app/modules/hfdd/hfddsession';
import { CSGraphSpec } from 'src/app/modules/hfdd/cs-graph-spec';
import { FormControl, UntypedFormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-cornerstone-graph-ddg',
  templateUrl: './cornerstone-graph-ddg.component.html',
  styleUrls: ['./cornerstone-graph-ddg.component.sass'],
  encapsulation: ViewEncapsulation.None
})
export class CornerstoneGraphDdgComponent implements OnInit, AfterViewInit {

  private _jsonURL = 'assets/ddgData.json';

  @ViewChild('myMeasureCanvas', {static: false})
  private myCanvas!: ElementRef<HTMLCanvasElement>;

  private context: CanvasRenderingContext2D | null;

  hfddSess: HFDDSession | undefined = undefined;

  private subjNrVariants: BehaviorSubject<number>;

  private subjDDGGraph: ReplaySubject<DDGraph>;

  private ddGraphVizUpdate$: Observable<DDGraph>;

  ddgConfigForm = new UntypedFormGroup({
    kVariants: new FormControl<number>(10, [Validators.required, Validators.min(1)]),
  })

  ////////////////////////////////////////////////////////////
  // SVG
  ////////////////////////////////////////////////////////////
  //////////////////////////////
  // General
  //////////////////////////////

  @ViewChild('chartContainer') 
  private chartContainerElement!: ElementRef;

  /**
   * SVG Height
   */
  private heightValue = 800;

  /**
   * SVG Width
   */
  private widthValue = 1200;

  /**
   * SVG Margins
   */
  private margin: { top: number, bottom: number, left: number; right: number} =  {top: 20, bottom: 30, left: 30, right: 20};

  /**
   * Handle to the svg element
   */
  private svgContent : any;

  //////////////////////////////
  // Vertices
  //////////////////////////////
  /**
   * SVG Group for the vertices.
   */
  private svgGVertices: any;

  /**
   * SVG group element containing itemset vertices.
   */
  private groupVerticesIS: any;

  /**
   * SVG group element containing flowsplit vertices.
   */
  private groupVerticesFS: any;

  /**
   * SVG group element containing emd trace vertices.
   */
  private groupVerticesTrace: any;

  //////////////////////////////
  // Edges
  //////////////////////////////
  /**
   * SVG Group for the edges.
   */
  private svgGEdges: any;

  /**
   * SVG group element containing edges between sets.
   */
  private groupEdgesIS: any;

  /**
   * SVG group element containing edges between flow splits.
   */
  private groupEdgesFS: any;

  /**
   * SVG group element containing edges between emd vertices.
   */
  private groupEdgesEMD: any;

  //////////////////////////////
  // Plotting
  //////////////////////////////
  /**
   * Graph plotter (using d3.js)
   */
  private ddgPlotter : DDGPlotter;

  constructor(private route: ActivatedRoute, private hfddSessService: HFDDSessionService, private http: HttpClient) {
    this.ddgPlotter = new DDGPlotter();
    this.context = null;

    this.subjNrVariants = new BehaviorSubject<number>(10);
    this.subjDDGGraph = new ReplaySubject<DDGraph>(1);


    this.ddGraphVizUpdate$ = combineLatest([this.subjDDGGraph, this.subjNrVariants])
      .pipe(
        switchMap(([ddGraph, kVariant]: [DDGraph, number]) => {
            // Copy
            // Does not work because I lose the functions :(
            //ddGraph = structuredClone(ddGraph);
            ddGraph.restrictVariants(kVariant);
            ddGraph.hideEmptyEmptyReallocation(true);
            ddGraph.layout();
            return of(ddGraph);
        })
      );
    
    this.ddGraphVizUpdate$.subscribe(
      {
        next: (ddGraph: DDGraph) => {
            this.printChart(ddGraph);
          },
        error: (e) => console.error(e)
      }
    );
  }

  ngOnInit(): void {
    this.ddgConfigForm.get("kVariants")?.valueChanges.subscribe(
      {
        next: (kVar: number) => {
          this.subjNrVariants.next(kVar);
        }
      }
    )
  }

  private resolveSess(params: Params) : Observable<[string, CSGraphSpec]> {
    const sessId = params['sessId'];

    let hfddSessUnresolved = this.hfddSessService.getSessionById(sessId);

    if (hfddSessUnresolved) {
      this.hfddSess = hfddSessUnresolved;
      return combineLatest([of(hfddSessUnresolved.sessId), hfddSessUnresolved.cornerstoneSpecSubject$]);
    }
    return throwError(() => Error("session undefined"));
  }

  public getJSON(): Observable<any> {
    return this.http.get(this._jsonURL);
  }    

  ngAfterViewInit(): void {
    this.context = this.myCanvas.nativeElement.getContext('2d');
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

    
    //////////////////////////////
    // Add Top-level Element Groups
    //////////////////////////////
    this.svgGEdges = gGraph.append("g").attr("class", "edgeGroup");
    this.svgGVertices = gGraph.append("g").attr("stroke-width", 1).attr("stroke", "#808080");

    ////////////////////
    // Different vertex groups
    ////////////////////
    // Itemset
    this.groupVerticesIS = this.svgGVertices.append("g");
    // Flow split
    this.groupVerticesFS = this.svgGVertices.append("g");
    // Trace
    this.groupVerticesTrace = this.svgGVertices.append("g");

    ////////////////////
    // Different edge groups
    ////////////////////
    // Itemset
    this.groupEdgesIS = this.svgGEdges.append("g").attr("class", "edgesIS");
    // Flow split
    this.groupEdgesFS = this.svgGEdges.append("g").attr("class", "edgesFS");
    // EMD
    this.groupEdgesEMD = this.svgGEdges.append("g").attr("class", "edgesEMD");

    ////////////////////////////////////////
    // Subscripting to DATA
    ////////////////////////////////////////
    this.route.parent?.params
      .pipe(
        switchMap(params => 
          iif(
            () => Object.keys(params).length === 0,
            this.getJSON(),
            this.resolveSess(params) 
              .pipe(
                filter(([sessId, csSpec]: [string, CSGraphSpec]) => 
                  csSpec.cornerstoneVertices.length > 0), // Nothing to do if not vertex requested
                switchMap(([sessId, csSpec]: [string, CSGraphSpec]) => 
                  this.hfddSessService.getDDGraph(sessId, csSpec)), // Request newest DDG Graph
              )
            )
          )
        )
        .subscribe(
          {
            next: (graphData: any) => {
                console.log('New cornerstone graph');
                console.log(graphData)

                const ddgBuilder: DDGBuilder = new DDGBuilder(this.context);
                const ddGraph: DDGraph = ddgBuilder.build_from_json(graphData);

                this.subjDDGGraph.next(ddGraph);
              },
              error: (e) => console.error(e)
          });
  }

  printChart(ddgGraph: DDGraph) {
    // TODO Better clearance
    this.groupEdgesIS.selectAll('*').remove();
    this.groupEdgesFS.selectAll('*').remove();
    this.groupEdgesEMD.selectAll('*').remove();
    this.groupVerticesIS.selectAll('*').remove();
    this.groupVerticesFS .selectAll('*').remove();
    this.groupVerticesTrace.selectAll('*').remove();
    this.ddgPlotter.plot_edges(ddgGraph, this.groupEdgesIS, this.groupEdgesFS, this.groupEdgesEMD);
    this.ddgPlotter.plot_vertices(ddgGraph, this.groupVerticesIS, this.groupVerticesFS, this.groupVerticesTrace);
    //let enterSelection: d3.Selection<SVGElement, DDGVertex, any, any> =  
    //  this.svgGVertices.selectAll("g")
    //    .data(ddgGraph.vertices).enter();
    //plot_vertex(enterSelection);
  }

}
