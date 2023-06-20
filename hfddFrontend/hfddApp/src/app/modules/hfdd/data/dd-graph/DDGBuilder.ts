import { DDGActivitySet } from "./activity/DDGActivitySet";
import { DDGActivityTextState, DDGActStateData } from "./activity/DDGActivityTextMeasures";
import { DDGActivityTrace } from "./activity/DDGActivityTrace";
import { DDGActivityTraceSink } from "./activity/DDGActivityTraceSink";
import { DDGActivityEmptySet } from "./activity/DDGActivityEmptySet";
import { DDGEdge } from "./edge/DDGEdge";
import { DDGEdgeEMD } from "./edge/DDGEdgeEMD";
import { DDGEdgeFlowsplit } from "./edge/DDGEdgeFlowsplit";
import { DDGEdgeInterset } from "./edge/DDGEdgeInterset";
import { EdgeAccessor } from "./edge/DDGEdgeType";
import { DDGraph } from "./DDGraph";
import { DDGVertex } from "./vertex/DDGVertex";
import { DDGVertexEMDTrace } from "./vertex/DDGVertexEMDTrace";
import { DDGVertexFlowSplit } from "./vertex/DDGVertexFlowSplit";
import { DDGVertexHierarchy } from "./vertex/DDGVertexHierarchy";
import { DDGVertexHSet } from "./vertex/DDGVertexHSet";
import { DDGVertexType, DDGVertexTypeMap } from "./vertex/DDGVertexType";
import { LogSide } from "./base/LogSide";
import { TextSizeComputer } from "./util/TextSizeComputer";
import { DDGTextLabelEl } from "./auxelements/DDGTextLabelEl";
import { DDGVertexArtRoot } from "./vertex/DDGVertexArtRoot";
import { DDGVertexProbabilityInfo } from "./vertex/DDGVertexProbabilityInfo";
import { DDGEmptyTrace } from "./vertex/DDGEmptyTrace";

export class DDGBuilder {

  private  _verticesHierSet: DDGVertexHSet[];

  /**
   * Access to the flow splits.
   */
  private _verticesFlowSplit: DDGVertexFlowSplit[];

  /**
   * Access to the left EMD traces.
   */
  private _verticesEMDLeft: DDGVertexEMDTrace[];

  /**
   * Access to the right EMD traces.
   */
  private _verticesEMDRight: DDGVertexEMDTrace[];

  /**
   * Text size computer.
   */
  private readonly _textSizeComputer: TextSizeComputer;

  /**
   * Current font used to measure text.
   */
  private _font: string = 'Roboto';

  /**
   * Current font size used to measure text.
   */
  private _fontsize: number = 12;

  public constructor(measureContext: CanvasRenderingContext2D | null) {

    this._verticesHierSet = [];
    this._verticesFlowSplit = [];
    this._verticesEMDLeft = [];
    this. _verticesEMDRight = [];
    this._textSizeComputer = new TextSizeComputer(measureContext);

  }
    
  public build_from_json(requestRes:any) : DDGraph {
    
    //////////////////////////////
    // Base Elements
    //////////////////////////////
    ////////////////////
    // Vertices
    ////////////////////
    let ddgVertices : DDGVertex[] = 
        requestRes.Vertices.map(this.createVertex, this);

    // Easy access initialization
    // Every vertex must be assigned somewhere
    if (ddgVertices.length !==  this._verticesHierSet.length + this._verticesFlowSplit.length +
          this._verticesEMDLeft.length + this._verticesEMDRight.length) {
      throw new Error("Building the fast type to vertices map failed.");
    }

    // Mapping from vertex id to vertex
    let vertexMap = new Map<number, DDGVertex>();
    ddgVertices.forEach(v => vertexMap.set(v.id, v));

    ////////////////////
    // Edges
    ////////////////////
    const edgeAccessor: EdgeAccessor = new EdgeAccessor();
    // Create edges and update the adjacent vertices
    let ddgEdges : DDGEdge[] = 
        requestRes.Edges.map((e: any) => this.createEdgeAndUpdateVertices(e, vertexMap, edgeAccessor));
    this.updateE2EInfo();
    //////////////////////////////
    // Derived
    //////////////////////////////
    ////////////////////
    // Root
    // Get root and verify that there are not multiple roots
    ////////////////////
    let roots: DDGVertexHierarchy[] = this._verticesHierSet.filter(v => v.edgesIn.length === 0)
    let root: DDGVertexHierarchy;
    if (roots.length == 0) {
        throw Error("There is no root!");
    }
    else if (roots.length > 1) {
        throw new Error("There are multiple roots!");
    }
    else {
      root = roots[0];
    }


    ////////////////////
    // Derived easy vertex access
    ////////////////////
    //////////
    // Level to vertices 
    //////////
    let level2vertices: DDGVertex[][] = [];
    ddgVertices.map(v => {
      if (level2vertices[v.visLevel]) {
        level2vertices[v.visLevel].push(v);
      }
      else {
        level2vertices[v.visLevel] = [v];
      }
    });
    
    // Initialize internal flowsplit datastructure
    DDGBuilder.flowSplitEMDAccess(this._verticesFlowSplit, this._verticesEMDLeft, this._verticesEMDRight);

    // Initialize Hierarchy only structures
    //this._verticesFlowSplit.forEach(v => v.hierParent = (v.edgesIn[0].u as DDGVertexHierarchy))
    //this._verticesHierSet.forEach(v => {
    //  v.hierParent = (v.edgesIn[0]?.u as DDGVertexHierarchy);
    //  v.hierChildren = v.edgesOut.map(e => (e.v as DDGVertexHierarchy));
    //})

    // TODO
    //variant: tmpVariant,
    //matchingValue: tmpMatchingValue,
    //matchingRelevant: v.matchingRelevant
    return new DDGraph(ddgVertices, edgeAccessor, root, level2vertices,
            this._verticesHierSet, this._verticesFlowSplit,
            this._verticesEMDLeft, this._verticesEMDRight);

  }

  /**
   * Create the DDGVertex from the provided json data.
   * @param vData Dictionary (json data)
   * @returns vertex (of the most specific type)
   */
  private createVertex(vData:any) : DDGVertex {
    ////////////////////
    // Attributes shared by all vertices
    ////////////////////
    const id: number = vData.id;
    const idString: string = vData.idString;
    const visLevel: number = vData.visLevel;
    
    if (vData.vertexType === 'ARTROOT' ) {
      const probInfo: DDGVertexProbabilityInfo = {
        probNonCondLeft: 1, 
        probNonCondRight: 1, 
        probNonCondResLeft: 1, 
        probNonCondResRight: 1,
        probCondLeft: undefined,
        probCondRight: undefined,
        probCondResLeft: undefined,
        probCondResRight: undefined,
      };
      const activities: DDGActivitySet[] = [];
      // Empty set "activity"
      activities.push(new DDGActivityEmptySet(
        this.create_activity_emptyset_labels(this._font, this._fontsize)));
      let ddgVertex = new DDGVertexArtRoot(id, idString, visLevel, DDGVertexType.ARTROOT, 
        probInfo, activities, 
        false,
        this.createActSetTitle(),
        this.createProbPictoTitle());   // Is cornerstone set?

      // Save handle to set vertex
      this._verticesHierSet.push(ddgVertex);
      return ddgVertex;

    }
    else if (vData.vertexType === 'INTERSET' 
        || vData.vertexType === 'INTERSETCS') {
      ////////////////////
      // Vertex in set hierarchy
      ////////////////////
      const probInfo: DDGVertexProbabilityInfo = {
        probNonCondLeft: vData.probabilityInfo.probNonCondLeft, 
        probNonCondRight: vData.probabilityInfo.probNonCondRight, 
        probNonCondResLeft: vData.probabilityInfo.probNonCondResLeft, 
        probNonCondResRight: vData.probabilityInfo.probNonCondResRight,
        probCondLeft: (vData.probabilityInfo.probCondLeft == null) ? undefined : vData.probabilityInfo.probCondLeft,
        probCondRight: (vData.probabilityInfo.probCondRight == null) ? undefined : vData.probabilityInfo.probCondRight,
        probCondResLeft: (vData.probabilityInfo.probCondResLeft == null) ? undefined : vData.probabilityInfo.probCondResLeft,
        probCondResRight: (vData.probabilityInfo.probCondResRight == null) ? undefined : vData.probabilityInfo.probCondResRight,
      };
      const activities: DDGActivitySet[] = this.build_activities_set(vData.activities);
      let condActivities: DDGActivitySet[] | undefined = undefined; 
      if (vData.conditionActivities != null && vData.conditionActivities.length > 0) {
        condActivities = this.build_activities_set(vData.conditionActivities);
      }
      // Empty set "activity"
      if (activities.length === 0) {
        activities.push(new DDGActivityEmptySet(
          this.create_activity_emptyset_labels(this._font, this._fontsize)));
      }
      let ddgVertex = new DDGVertexHSet(id, idString, visLevel, ((vData.vertexType === 'INTERSET') ? DDGVertexType.INTERSET : DDGVertexType.INTERSETCS),
        probInfo, activities, 
        ((vData.vertexType === 'INTERSET') ? false: true),
        this.createActSetTitle(),
        this.createProbPictoTitle(), condActivities);   // Is cornerstone set?

      // Save handle to set vertex
      this._verticesHierSet.push(ddgVertex);
      return ddgVertex;
    }
    else if (vData.vertexType === 'FLOWSPLIT') {
      ////////////////////
      // Flowsplit Vertex 
      ////////////////////
      const probLeft: number = vData.probabilityMassLeft;
      const probRight: number = vData.probabilityMassRight;
      let ddgVertex = new DDGVertexFlowSplit(id, idString, visLevel, probLeft, probRight);
      this._verticesFlowSplit.push(ddgVertex);
      return ddgVertex;
    }
    else if (vData.vertexType === 'EMD') {
      ////////////////////
      //  EMD Trace Vertex
      ////////////////////
      const probability: number = vData.probability;
      const probabilityRank: number | undefined = vData.probabilityRank;

      // Easier but will break if the keys do not actually match!
      const logSide: LogSide = LogSide[vData.logSide as keyof typeof LogSide];
      const activities: DDGActivityTrace[] = this.build_activities_trace(vData.activities);
      // Empty trace
      let ddgVertex: DDGVertexEMDTrace;
      if (activities.length === 0) {
        activities.push(new DDGActivityTraceSink(
          this.create_activity_sink_labels(this._font, this._fontsize)))
        ddgVertex = new DDGEmptyTrace(id, idString, visLevel, probability, logSide, activities, probabilityRank);

      }
      else {
        ddgVertex = new DDGVertexEMDTrace(id, idString, visLevel, probability, logSide, activities, probabilityRank);
      }
      if (logSide === LogSide.LEFT) {
        this._verticesEMDLeft.push(ddgVertex);
      }
      else if (logSide === LogSide.RIGHT) {
        this._verticesEMDRight.push(ddgVertex);
      }
      else {
        throw new Error("Unexpected log side");
      }
      return ddgVertex;
    }
    else {
      throw new Error("Unexpected vertex type in json response");
    }
  }

  private createEdgeAndUpdateVertices(eData:any, vertexMap: Map<number, DDGVertex>, edgeAccessor?: EdgeAccessor) : DDGEdge {

    //////////////////////////////
    // Common Edge Data
    //////////////////////////////
    ////////////////////
    // Source and Destination
    ////////////////////
    const idSrc: number = eData.Source;
    const idDest: number = eData.Target;
    if (!vertexMap.has(idSrc)) {
      throw new Error("Source vertex referenced by the edge is invalid.")
    }
    if (!vertexMap.has(idDest)) {
      throw new Error("Target vertex referenced by the edge is invalid.")
    }
    const u: DDGVertex = vertexMap.get(idSrc)!;
    const v: DDGVertex = vertexMap.get(idDest)!;

    ////////////////////
    // Other shared information
    ////////////////////
    const edgeInfo: any = eData.EdgeInfo;
    const idEdge: number = edgeInfo.id;
    const edgeType: string = edgeInfo.edgeType;

    //////////////////////////////
    // Instantiation and Special Data
    //////////////////////////////
    let edge: DDGEdge;
    if (edgeType === 'INTERSET') {
      const probabilityLeft: number = edgeInfo.probabilityLeft;
      const probabilityRight: number = edgeInfo.probabilityRight;
      const isSpannTreeEdge: boolean = edgeInfo.edgeInLayoutTree;

      edge = new DDGEdgeInterset(idEdge, u, v, probabilityLeft, probabilityRight, isSpannTreeEdge);
      // Add to accessor
      if (edgeAccessor !== undefined) {
        edgeAccessor.addINTERSET(<DDGEdgeInterset> edge);
      }
    }
    else if (edgeType === 'FLOWSPLIT') {
      const probability: number = edgeInfo.probability;

      edge = new DDGEdgeFlowsplit(idEdge, u, v, probability);
      // Add to accessor
      if (edgeAccessor !== undefined) {
        edgeAccessor.addFLOWSPLIT(<DDGEdgeFlowsplit> edge);
      }
    }
    else if (edgeType === 'EMDFLOW') {
      const probability: number = edgeInfo.probability;
      const cost: number = edgeInfo.cost;

      edge = new DDGEdgeEMD(idEdge, u, v, probability, cost, probability * cost);
      // Add to accessor
      if (edgeAccessor !== undefined) {
        edgeAccessor.addEMDFLOW(<DDGEdgeEMD> edge);
      }
    }
    else {
      throw new Error("Unexpected edge type in json response.");
    }

    //////////////////////////////
    // Update vertices
    //////////////////////////////
    u.addOutEdge(edge);
    v.addInEdge(edge);

    return edge;
  } 

  /**
   * Transform a json array of activities into an array of trace activities.
   * 
   * @param dataActivities 
   * @returns 
   */
  private build_activities_trace(dataActivities:any) : DDGActivityTrace[] {
    // Important to use this type of function so that this. references builder 
    return dataActivities.map((dataA:any) => this.build_activity_trace(dataA));
  }
  
  /**
   * Transform a json activity into an instance of a trace activity.
   * 
   * @param dataActivity JSON data containing the activity specification
   * @returns Instantiated activity
   */
  private build_activity_trace(dataActivity:any) : DDGActivityTrace {
    let actLabel = (dataActivity.activity as string);
    let actLabelAbbrev = (dataActivity.activityAbbrev as string);

    const actLabels = this.create_activity_state_labels(actLabel, actLabelAbbrev, actLabel.substring(0, 3) + (actLabel.length > 3 ? '..' : ''),
      this._font, this._fontsize);
    return new DDGActivityTrace(actLabel, dataActivity.activityCode, 
                       actLabels, DDGActivityTextState.SUPERSHORT);
  }

  /**
   * Transform a json array of activities into an array of DDGActivities.
   * 
   * @param dataActivities 
   * @returns 
   */
  private build_activities_set(dataActivities:any) : DDGActivitySet[] {
    // Important to use this type of function so that this. references builder 
    return dataActivities.map((dataA:any) => this.build_activity_set(dataA));
  }

  /**
   * Transform a json activity into an instance of a trace activity.
   * 
   * @param dataActivity JSON data containing the activity specification
   * @returns Instantiated activity
   */
  private build_activity_set(dataActivity:any) : DDGActivitySet {
    let actLabel = (dataActivity.activity as string);
    let actLabelAbbrev = (dataActivity.activityAbbrev as string);

    const actLabels = this.create_activity_state_labels(actLabel, actLabelAbbrev, actLabel.substring(0, 2) + '..',
      this._font, this._fontsize);
    return new DDGActivitySet(actLabel, dataActivity.activityCode, 
                       actLabels, DDGActivityTextState.SHORT);
  }

  private create_activity_state_labels(activity: string, activityAbbrev: string, actSuperShort: string,
      font: string, fontsize: number): DDGActStateData<DDGTextLabelEl> {

    this._textSizeComputer.font = font;
    this._textSizeComputer.fontsize = fontsize;

    let actTextMeasures : Readonly<DDGActStateData<[number, number]>> = 
      this._textSizeComputer.getMeasures4Activity(activity, activityAbbrev, actSuperShort);
    
    return {
      NONE: new DDGTextLabelEl('',  this._textSizeComputer.font, 
        this._textSizeComputer.fontsize, 0, 0),
        
      SUPERSHORT: new DDGTextLabelEl(actSuperShort, 
        this._textSizeComputer.font, this._textSizeComputer.fontsize, 
        actTextMeasures.SUPERSHORT[0], actTextMeasures.SUPERSHORT[1]),

      SHORT: new DDGTextLabelEl(activityAbbrev, 
        this._textSizeComputer.font, this._textSizeComputer.fontsize, 
        actTextMeasures.SHORT[0], actTextMeasures.SHORT[1]),

      NORMAL: new DDGTextLabelEl(activity, 
        this._textSizeComputer.font, this._textSizeComputer.fontsize, 
        actTextMeasures.NORMAL[0], actTextMeasures.NORMAL[1])
    }
  }

  private create_activity_sink_labels(font: string, fontsize: number): DDGActStateData<DDGTextLabelEl> {
    return this.create_activity_state_labels(DDGActivityTraceSink.SYMBOL, DDGActivityTraceSink.SYMBOL, DDGActivityTraceSink.SYMBOL,
      font, fontsize);
  }

  private create_activity_emptyset_labels(font: string, fontsize: number): DDGActStateData<DDGTextLabelEl> {
    return this.create_activity_state_labels(DDGActivityEmptySet.SYMBOL, DDGActivityEmptySet.SYMBOL, DDGActivityEmptySet.SYMBOL,
      font, fontsize);
  }


  /**
   * Initializes the quick access from the flowsplits to the EMD vertices.
   * Idea:
   * Easy access to left and right side of the signature. 
   * 
   * This method assumes that the grpah structure, in particular the in and out edges, are intialized.
   *  
   * @param flowSplits Flow split vertices
   * @param verticesEMDLeft Left EMD vertices
   * @param verticesEMDRight Right EMD vertices
   */
  private static flowSplitEMDAccess(flowSplits: DDGVertexFlowSplit[],  
      verticesEMDLeft: DDGVertexEMDTrace[], verticesEMDRight: DDGVertexEMDTrace[]) : void {

    // By construction parent of a left vertex is a flow split
    verticesEMDLeft.forEach(v => (v.edgesIn[0].u as DDGVertexFlowSplit).sigLeft.push(v));
    // By construction parent of the parent parent of a right vertex is a flow split
    verticesEMDRight.forEach(v => (v.edgesIn[0].u.edgesIn[0].u as DDGVertexFlowSplit).sigRight.push(v));

    // Sort accessor arrays
    flowSplits.forEach(v => v.reorderSigMimimizeEMDCrossings());
  }


  /**
   * Sort the hierarchy vertices within each level, and sort EMD bipartite graphs according 
   * to key assigned by the backend (minimize crossings).
   * 
   * Moreover, update the internal data structures that reference vertices so that access of vertices
   * follows the sorting.   
   * For example:
   * - DFS search on the tree results in left to right traversal of the levels 
   * 
   * Approach:
   * Sort the flow splits left-to-right by covered probability mass (descending).
   * Propagate the sorting upwards. 
   * Independently sort the EMD bipartite graphs.
   * 
   * Input Requirement:
   * The graph structure in terms of vertices and edges must be initialized (i.e., vertices know their adjacent edges)
   * 
   * @param type2vertices mapping from vertex tpyes to vertices
   */
  private static createIntraLevelSorting(type2vertices: DDGVertexTypeMap<DDGVertex[]>): void {

      ////////////////////
      // Flow splits
      ////////////////////
      // Sort flowsplits by probability (descending)
      type2vertices.FLOWSPLIT.sort((u, v) => (v as DDGVertexEMDTrace).probability - (u as DDGVertexEMDTrace).probability);
      // Assign index
      let i = 0;
      for (let v of type2vertices.FLOWSPLIT) {
        v.intraLevelSortKey = i;
        i++;
      }
      
  }

  private createActSetTitle() {
    const text: string = "Activites";
    const fontSize = 12;
    this._textSizeComputer.fontsize = fontSize;
    const [textHeight, textWidth] = this._textSizeComputer.measureAuxText(text);

    return new DDGTextLabelEl(text, this._textSizeComputer.font, fontSize, textHeight, textWidth);
  }

  private createProbPictoTitle() {
    const text: string = "Log Covered";
    const fontSize = 10;
    this._textSizeComputer.fontsize = fontSize;
    const [textHeight, textWidth] = this._textSizeComputer.measureAuxText(text);

    return new DDGTextLabelEl(text, this._textSizeComputer.font, fontSize, textHeight, textWidth);
  }

  private updateE2EInfo() {
    for (let u of this._verticesEMDLeft) {
      if (!(u instanceof DDGEmptyTrace)) {
        continue;
      }
      for (let e of u.edgesOut) {
        if (!(e.v instanceof DDGEmptyTrace)) {
          continue;
        }
        let delta = (e as DDGEdgeEMD).probFlow;
        let u2 = (u as DDGEmptyTrace);
        let v2 = (e.v as DDGEmptyTrace);
        u2.e2eHiddenProbability = u2.realProbability - delta;
        v2.e2eHiddenProbability = v2.realProbability - delta;

        // This edge must be the e2e edge otherwise we would have skipped it
        if (u.edgesOut.length === 1) {
          u.hideWhenHidingEnabled = true;
        }
        else if (v2.edgesIn.length === 1) {
          v2.hideWhenHidingEnabled = true;
        }
        else {
          console.log('Why no edge to hide?');
          console.log(u2);
          console.log(v2);
        }
      }
    }
  }

}
