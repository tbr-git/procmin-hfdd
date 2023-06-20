import { DDGEdge } from "../edge/DDGEdge";
import { DDGEdgeFlowsplit } from "../edge/DDGEdgeFlowsplit";
import { DDGEdgeInterset } from "../edge/DDGEdgeInterset";
import { DDGEdgeType } from "../edge/DDGEdgeType";
import { DDGVertex } from "./DDGVertex";
import { DDGVertexType } from "./DDGVertexType";

export abstract class DDGVertexHierarchy extends DDGVertex {

  /**
   * Height of the subtree (covered space in y direction)
   */
  private _heightHierarchy: number;

  /**
   * Handle to the parent in the tree hierarchy. 
   */
  private _hierParent: DDGVertexHierarchy | undefined;

  /**
   * Handles to hierarchy child vertices. 
   */
  private _hierChildren: DDGVertexHierarchy[];
  
  /**
   * Handles to hierarchy child vertices in a spanning tree of the hierarchy. 
   */
  private _hierChildrenTree: DDGVertexHierarchy[];


  /**
   * Space betwee child hierarchies.
   *        subtree11
   * tree1  | spacing
   *        subtree12 
   *        || higher level spacing
   *        subtree21 
   * tree2  | spacing
   *        subtree22 
   */
  private _subHierarchySpacing: number;

  public constructor(id: number, idString: string, visLevel: number, vertexType: DDGVertexType) {
    super(id, idString, vertexType, visLevel);
    this._subHierarchySpacing = 10;

    this._hierParent = undefined;
    this._hierChildren = [];
    this._hierChildrenTree = [];

    // Default values
    this._heightHierarchy = 0;
  }

  /**
   * Recursively calculate the height of each hierarchy tree.

   * (We consider the hierarchy a tree that grows left to right.)
   * @returns Height of the hierarchy rooted at this vertex.
   */
  public recCalcHierachyHeight(): number {
    const subHierHeight: number = this.hierChildrenTree
        .map(v => v.recCalcHierachyHeight())
        .reduce((aggHeight, childHeight) => aggHeight + childHeight, 0) // Subhierarchy heights
      + (this.hierChildrenTree.length - 1) * this._subHierarchySpacing;    // Subhierarchy spacing

    this.heightHierarchy = this.calcLocalHeight(subHierHeight);
    return this.heightHierarchy;
  }

  /**
   * Given the accumulated height of the hierarchy vertex children, calculate the height
   * of the hierarchy rooted at this vertex.  
   * 
   * @param hierChildrenHeight Accumulated height children (i.e., child vertices that are hierarchy vertices).
   * @returns Height of the hierarchy rooted at this vertex
   */
  public abstract calcLocalHeight(hierChildrenHeight: number): number;

  /**
   * Recursively position the vertices in this hierarchy part.
   * 
   * @param levelXPositions X-coordinate for each visualization level
   * @param yUpperLeft Y-ccordinate (upper left corner) for this vertex' hierarchy's space in y direction
   */
  public recPositionDescdants(levelXBoxes: [number, number][], yUpperLeft: number) {
    this.x = levelXBoxes[this.visLevel][0];

    // Position this vertex in the mid of the space covered by the
    // entire subhierarchy
    this.y = yUpperLeft + (this.heightHierarchy - this.size.height) / 2.0;

    // Recursively position hierarchical children
    let yUpperLeftNext: number = yUpperLeft;
    for (let v of this.hierChildrenTree) {
      v.recPositionDescdants(levelXBoxes, yUpperLeftNext);      
      yUpperLeftNext += v.heightHierarchy + this.subHierarchySpacing;

    }
  }

  /**
   * Add outgoing edge.
   * @param e outgoing edge
   */
  public override addOutEdge(e: DDGEdge): void {
    // Add Edge
    super.addOutEdge(e);

    if (e.edgeType.valueOf() === DDGEdgeType.INTERSET.valueOf()) {}
      // Update typed accessor
      this.hierChildren.push(e.v as DDGVertexHierarchy);

      // Is the edge in the spann tree
      if ((e as DDGEdgeInterset).isSpannTreeEdge) {
        // Update spann tree children
        this._hierChildrenTree.push(e.v as DDGVertexHierarchy);
        // In spann tree edge's source will be target's parent
        (e.v as DDGVertexHierarchy).hierParent = (e.u as DDGVertexHierarchy);
      }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Getter and Setter
  ////////////////////////////////////////////////////////////////////////////////


  public get hierParent(): DDGVertexHierarchy | undefined {
    return this._hierParent;
  }
  public set hierParent(value: DDGVertexHierarchy | undefined) {
    this._hierParent = value;
  }

  public get hierChildren(): DDGVertexHierarchy[] {
    return this._hierChildren;
  }

  public set hierChildren(value: DDGVertexHierarchy[]) {
    this._hierChildren = value;
  }

  public get hierChildrenTree(): DDGVertexHierarchy[] {
    return this._hierChildrenTree;
  }

  public get heightHierarchy(): number {
    return this._heightHierarchy;
  }

  public set heightHierarchy(value: number) {
    this._heightHierarchy = value;
  }

  public get subHierarchySpacing(): number {
    return this._subHierarchySpacing;
  }
  public set subHierarchySpacing(value: number) {
    this._subHierarchySpacing = value;
  }
}
