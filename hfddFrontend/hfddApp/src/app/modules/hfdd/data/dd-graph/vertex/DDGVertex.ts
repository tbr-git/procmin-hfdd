import { DDGEdge } from "../edge/DDGEdge";
import { DDGVertexType } from "./DDGVertexType";
import { DDGEl } from "../base/DDGEl";

export abstract class DDGVertex extends DDGEl {
    
  /**
   * Id of the vertex.
   */
  private readonly _id: number;

  /**
   * Id string (reference to this vertex in different DDGs).
   */
  private readonly _idString: string;

  /**
   * Type of the vertex.
   * Used for the visualization with d3.js
   */
  private readonly _vertexType: DDGVertexType;

  /**
   * Level in the visualization.
   */
  private readonly _visLevel: number;

  /**
   * Intra-level: Left-to-right sorting within its visualization level
   */
  private _intraLevelSortKey: number;

  /**
   * Handles to vertices that count for the OUT degree.
   */
  private _edgesOut: DDGEdge[];

  /**
   * Handles to vertices that count for the IN degree.
   */
  private _edgesIn: DDGEdge[];

  /**
   * Reference length that probability 1 should fill.
   */
	private _sizeUnitProb: number;

  /**
   * Baseline distance for spacing between nested compontents.
   */
  private _innerVertexSep: number;

  /**
   * Larger distance for subcomponents that are "relatively close".
   */
  private _childSep: number;

  public constructor(id: number, idString: string,
      vertexType : DDGVertexType, 
      visLevel: number) {
    super();
    this._id = id;
    this._idString = idString;
    this._vertexType = vertexType;
    this._visLevel = visLevel;
    this._edgesIn = [];
    this._edgesOut = [];
    this._intraLevelSortKey = 0;
    this._sizeUnitProb = 100;
    
    this._innerVertexSep = Math.min(10, this.sizeUnitProb / 40);
    this._childSep = Math.min(5, this.sizeUnitProb / 2);
  }

  /**
   * Add incoming edge.
   * @param e incoming edge
   */
  public addInEdge(e: DDGEdge): void {
    this._edgesIn.push(e);
  }

  /**
   * Add outgoing edge.
   * @param e outgoing edge
   */
  public addOutEdge(e: DDGEdge): void {
    this._edgesOut.push(e);
  }


  public layoutAdjacentEdges(): void {
    const totalWidthIn = this.getTotalEdgeWidth(this._edgesIn);
    const totalWidthOut = this.getTotalEdgeWidth(this._edgesOut);

    ///////////////////
    // Layout incoming edges
    ///////////////////
    if (this._edgesIn.length > 0) {
      // Edges start a y of this vertex and distribute free spacw w.r.t. verte height
      const startY = this.y + (this.size.height - totalWidthIn) / 2
      // Sort incoming edges by y coordinates (ascending) of adjacent vertex
      this.edgesIn.sort((e, f) => e.u.y - f.u.y);
      this.edgesIn.map((accStart => (e => {
        // Assuming a rectangle shape of this vertex (end of the edge)
        e.vx = this.x;
        e.vy =  accStart + e.width / 2;
        accStart += e.width;
      }))(startY));     }
    ///////////////////
    // Layout outgoing edges
    ///////////////////
    if (this.edgesOut.length > 0) {
      // Edges start a y of this vertex and distribute free spacw w.r.t. verte height
      const startY = this.y + (this.size.height - totalWidthOut) / 2
      // Sort incoming edges by y coordinates (ascending) of adjacent vertex
      this.edgesOut.sort((e, f) => e.v.y - f.v.y);
      this.edgesOut.map((accStart => (e => {
        // Assuming a rectangle shape of this vertex (edge start)
        e.ux = this.x + this.size.width;
        e.uy = accStart + e.width / 2;
        accStart += e.width;
      }))(startY));
    }
  }

  protected getTotalEdgeWidth(edges: DDGEdge[]): number {
    return edges.map(e => e.width).reduce((accWidth, width) => accWidth + width, 0);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Getter and Setter
  ////////////////////////////////////////////////////////////////////////////////

  public get id(): number {
    return this._id;
  }

  public get vertexType(): DDGVertexType {
    return this._vertexType;
  }

  public get visLevel(): number {
    return this._visLevel;
  }

  public get edgesOut(): DDGEdge[] {
    return this._edgesOut;
  }

  public set edgesOut(value: DDGEdge[]) {
    this._edgesOut = value;
  }

  public get edgesIn(): DDGEdge[] {
    return this._edgesIn;
  }

  public set verticesIn(value: DDGEdge[]) {
    this._edgesIn = value;
  }

  public get intraLevelSortKey(): number {
    return this._intraLevelSortKey;
  }
    
  public set intraLevelSortKey(value: number) {
    this._intraLevelSortKey = value;
  }

	public get sizeUnitProb(): number {
		return this._sizeUnitProb;
	}

	public set sizeUnitProb(value: number) {
		this._sizeUnitProb = value;
	}

  public get innerVertexSep(): number {
    return this._innerVertexSep;
  }

  public set innerVertexSep(value: number) {
    this._innerVertexSep = value;
  }

  public get childSep(): number {
    return this._childSep;
  }

  public set childSep(value: number) {
    this._childSep = value;
  }


}

