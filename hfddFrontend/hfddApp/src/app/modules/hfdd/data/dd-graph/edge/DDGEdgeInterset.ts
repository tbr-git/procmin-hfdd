import { DDGEdge } from "./DDGEdge";
import { DDGEdgeType } from "./DDGEdgeType";
import { DDGVertex } from "../vertex/DDGVertex";

export class DDGEdgeInterset extends DDGEdge {

    /**
     * Probability flow in the left log.
     */
    private readonly _probabilityLeft: number;

    /**
     * Probability flow in the right log.
     */
    private readonly _probabilityRight: number;

    /**
     * Is the edge contained in the
     * spann tree of the hierarchy?
     */
    private readonly _isSpannTreeEdge: boolean;

    public constructor(id: number, u: DDGVertex, v: DDGVertex, probabilityLeft: number, probabilityRight: number, 
            isSpannTreeEdge: boolean) {
        super(id, DDGEdgeType.INTERSET, u, v);
        this._probabilityLeft = probabilityLeft;
        this._probabilityRight = probabilityRight;
        this._isSpannTreeEdge = isSpannTreeEdge;
    }

    public calcWidth(edgeUnitProbability: number): void {
        this.width = 10;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////
    public get probabilityLeft(): number {
        return this._probabilityLeft;
    }

    public get probabilityRight(): number {
        return this._probabilityRight;
    }

    public get isSpannTreeEdge(): boolean {
        return this._isSpannTreeEdge;
    }

}