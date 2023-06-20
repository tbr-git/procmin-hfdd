import { DDGEdgeType } from "./DDGEdgeType";
import { DDGVertex } from "../vertex/DDGVertex";

export abstract class DDGEdge  implements d3.DefaultLinkObject {
    
    /**
     * Id of the edge.
     */
    private readonly _id: number;

    /**
     * Edge type.
     * Used by d3js.
     */
    private readonly _edgeType: DDGEdgeType;

    /**
     * Source vertex of the edge.
     */
    private readonly _u: DDGVertex;

    /**
     * Destinatin vertex of the edge.
     */
    private readonly _v: DDGVertex;

    /**
     * Edge width
     */
    private _width: number;

    /**
     * X-ccordinate of the starting point of this edge
     */
    private _ux: number;

    /**
     * Y-ccordinate of the starting point of this edge
     */
    private _uy: number;

    /**
     * X-ccordinate of the end point of this edge
     */
    private _vx: number;

    /**
     * Y-ccordinate of the end point of this edge
     */
    private _vy: number;

    /**
     * Should the edge be shown.
     * Hidden edges will be assigned width 0.
     */
    private _visible: boolean;

    public constructor(id: number, edgeType: DDGEdgeType, u: DDGVertex, v: DDGVertex) {
        this._id = id;
        this._edgeType = edgeType;
        this._u = u;
        this._v = v;
        this._width = 0;
        this._ux = 0;
        this._uy = 0;
        this._vx = 0;
        this._vy = 0;
        this._visible = true;
    }

    public abstract calcWidth(edgeUnitProbability: number): void;

    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////
    // Implement the d3.Defaultlink interface
    ////////////////////////////////////////
    public get source(): [number, number] {
        return [this.ux, this.uy];
    }

    public get target(): [number, number] {
        return [this.vx, this.vy];
    }

    public get id(): number {
        return this._id;
    }

    public get edgeType(): DDGEdgeType {
        return this._edgeType;
    }

    public get u(): DDGVertex {
        return this._u;
    }

    public get v(): DDGVertex {
        return this._v;
    }
    
    public get width(): number {
        return this._width;
    }

    /**
     * Setter of width.
     * If edge is not shown, will enforce width = 0
     */
    public set width(width: number) {
        if (this.visible) {
            this._width = width;
        }
        else {
            this._width = 0;
        }
    }

    public get ux(): number {
        return this._ux;
    }

    public set ux(value: number) {
        this._ux = value;
    }

    public get uy(): number {
        return this._uy;
    }

    public set uy(value: number) {
        this._uy = value;
    }

    public get vx(): number {
        return this._vx;
    }

    public set vx(value: number) {
        this._vx = value;
    }

    public get vy(): number {
        return this._vy;
    }

    public set vy(value: number) {
        this._vy = value;
    }

    public get visible(): boolean {
        return this._visible;
    }

    public set visible(value: boolean) {
        if (!value) {
            this._width = 0;
        }
        this._visible = value;
    }

}