import { RandomNumberGenerationSource } from "d3";
import { DDGActivityTraceSink } from "./activity/DDGActivityTraceSink";
import { EdgeAccessor } from "./edge/DDGEdgeType";
import { DDGVertex } from "./vertex/DDGVertex";
import { DDGVertexEMDTrace } from "./vertex/DDGVertexEMDTrace";
import { DDGVertexFlowSplit } from "./vertex/DDGVertexFlowSplit";
import { DDGVertexHierarchy } from "./vertex/DDGVertexHierarchy";
import { DDGVertexHSet } from "./vertex/DDGVertexHSet";
import { DDGVertexType, DDGVertexTypeMap } from "./vertex/DDGVertexType";
import { DDGEmptyTrace } from "./vertex/DDGEmptyTrace";
import { DDGEdgeEMD } from "./edge/DDGEdgeEMD";

export class DDGraph {

    /**
     * Array of vertices.
     */
    private _vertices: DDGVertex[];

    /**
     * List of all edges.
     */
    private _edges: EdgeAccessor;

    /**
     * Root vertex of the hierarchy
     */
    private _root: DDGVertexHierarchy;

    /**
     * Direct acces to set verices in the hierarchy.
     */
    private readonly _verticesHierSet: DDGVertexHSet[];

    /**
     * Access to the flow splits.
     */
    private readonly _verticesFlowSplit: DDGVertexFlowSplit[];

    /**
     * Access to the left EMD traces.
     */
    private readonly _verticesEMDLeft: DDGVertexEMDTrace[];

    /**
     * Access to the right EMD traces.
     */
    private readonly _verticesEMDRight: DDGVertexEMDTrace[];

    /**
     * Vertices for each level.
     */
    private readonly _level2vertices: DDGVertex[][];

    /**
     * Per-level level start x-position (in left-to-right graph layout).
     */
    private _levelXBoxes: [number, number][];

    /**
     * Minimum distance between levels.
     * 
     * Ith entry is the desired minimum distance between level i and level i + 1.
     * For simplicity, it should also contain an entry for the last level.
     * This makes traversal easier and the resulting entry will usually be ignored afterwards.
     */
    private _interLevelDistance: number[];

    public constructor(vertices: DDGVertex[], edges: EdgeAccessor, root: DDGVertexHierarchy, level2vertices: DDGVertex[][], 
            verticesHierSet: DDGVertexHSet[], 
            verticesFlowSplit: DDGVertexFlowSplit[], 
            verticesEMDLeft: DDGVertexEMDTrace[], verticesEMDRight: DDGVertexEMDTrace[]) {
        this._vertices = vertices;
        this._edges = edges;
        this._root = root;
        this._level2vertices = level2vertices;
        this._verticesHierSet = verticesHierSet;
        this._verticesFlowSplit = verticesFlowSplit;
        this._verticesEMDLeft = verticesEMDLeft;
        this._verticesEMDRight = verticesEMDRight;

        this._levelXBoxes = [];
        // Default intra level distance of 30
        this._interLevelDistance = Array(level2vertices.length - 1).fill(30);
        this._interLevelDistance[level2vertices.length - 2] = 60;
    }

    public layout(): void {
        // !!!!! Be careful about the order that the following functions
        // !!!!! are called. Some depend on each other !!!!!
        ////////////////////////////////////////
        // Vertices
        ////////////////////////////////////////
        ////////////////////
        // Size computation
        ////////////////////
        // 1. Hide edges between flow splits and sink activities
        // 2. For each element, calculate minimum size
        // 3. Local (per vertex) sub component target sizes 
        // 4. Global target size update round
        this.hideFlowsplit2SinkEdges();
        this._vertices.forEach(v => v.calcMinSize());
        this._vertices.forEach(v => v.calcAssignSubElementTargetSize());
        this._vertices.forEach(v => v.calcSize());
        this._vertices.forEach(v => v.applyInternalLayout());
        this.calcNestedHierachyHeights();
        this.initLevelXPosition();
        this.calculateVertexPositions();

        ////////////////////////////////////////
        // Edges
        ////////////////////////////////////////
        const edgeUnitProbability: number = this._verticesEMDLeft[0].sizeUnitProb;
        this._edges.ALL.forEach(e => e.calcWidth(edgeUnitProbability));
        this.vertices.forEach(v => v.layoutAdjacentEdges());
    }

    private calcNestedHierachyHeights() : void {
        this.root.recCalcHierachyHeight();
    }

    private initLevelXPosition(): void {

        const levelWidths: number[] = this.level2vertices
            .map(levelVertices => levelVertices
                .map(v => v.size.width)
                .reduce((curMaxWidth, width) => Math.max(curMaxWidth, width)));
        
        this._levelXBoxes = levelWidths
            .map((accWidth => ((levelWidth, levelIndex) => {
                const res: [number, number] = [accWidth, levelWidth];
                accWidth += levelWidth + this._interLevelDistance[levelIndex];
                return res;
            }))(0));

    }

    public calculateVertexPositions(): void {
        this.root.recPositionDescdants(this._levelXBoxes, 0);
    }

    public hideFlowsplit2SinkEdges(): void {
        this._verticesEMDLeft
            .filter(v => v.activities[0] instanceof DDGActivityTraceSink)
            .flatMap(v => v.edgesIn)
            .forEach(e => e.visible = false);
    }

    public restrictVariants(topK: number) : void {
        // Hide vertices
        this._verticesEMDLeft.forEach(v => v.show = (v?.probabilityRank ?? topK) < topK); 
        this._verticesEMDRight.forEach(v => v.show = (v?.probabilityRank ?? topK) < topK); 

        // Hide edges
        this._edges.EMDFLOW.forEach(
            e => e.show = ((e.u as DDGVertexEMDTrace).show && (e.v as DDGVertexEMDTrace).show)
        );
        this._edges.FLOWSPLIT.forEach(
            e => e.show = (e.v as DDGVertexEMDTrace).show
        );
    }

    public hideEmptyEmptyReallocation(value: boolean): void {
        for (let u of this. verticesEMDLeft) {
            if (u instanceof DDGEmptyTrace) {
                let u2 = (u as DDGEmptyTrace);
                u2.enableE2EHiding = value;
                if (u2.hideWhenHidingEnabled) {
                    (u2.edgesOut[0] as DDGEdgeEMD).show = !value;
                }
            }
        }
        for (let v of this. verticesEMDRight) {
            if (v instanceof DDGEmptyTrace) {
                let v2 = (v as DDGEmptyTrace);
                v2.enableE2EHiding = value;
                if (v2.hideWhenHidingEnabled) {
                    (v2.edgesIn[0] as DDGEdgeEMD).show = !value;
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////

    public get vertices(): DDGVertex[] {
        return this._vertices;
    }

    public get edges(): EdgeAccessor {
        return this._edges;
    }

    public get root(): DDGVertexHierarchy {
        return this._root;
    }

    public get level2vertices(): DDGVertex[][] {
        return this._level2vertices;
    }

    public get verticesHierSet(): DDGVertexHSet[] {
        return this._verticesHierSet;
    }

    public get verticesFlowSplit(): DDGVertexFlowSplit[] {
        return this._verticesFlowSplit;
    }

    public get verticesEMDRight(): DDGVertexEMDTrace[] {
        return this._verticesEMDRight;
    }

    public get verticesEMDLeft(): DDGVertexEMDTrace[] {
        return this._verticesEMDLeft;
    }
}