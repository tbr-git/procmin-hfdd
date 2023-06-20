import { DDGEdge } from "./DDGEdge";
import { DDGEdgeEMD } from "./DDGEdgeEMD";
import { DDGEdgeFlowsplit } from "./DDGEdgeFlowsplit";
import { DDGEdgeInterset } from "./DDGEdgeInterset";

export enum DDGEdgeType {
    INTERSET = 'INTERSET',
    FLOWSPLIT = 'FLOWSPLIT',
    EMDFLOW = 'EMDFLOW'
}

export class EdgeAccessor {
    private _INTERSET: DDGEdgeInterset[];

    private _FLOWSPLIT: DDGEdgeFlowsplit[];

    private _EMDFLOW: DDGEdgeEMD[];

    public constructor() {
        this._INTERSET = [];
        this._FLOWSPLIT = [];
        this._EMDFLOW = [];
    }

    public get EMDFLOW(): DDGEdgeEMD[] {
        return this._EMDFLOW;
    }

    public get FLOWSPLIT(): DDGEdgeFlowsplit[] {
        return this._FLOWSPLIT;
    }

    public get INTERSET(): DDGEdgeInterset[] {
        return this._INTERSET;
    }

    public addEMDFLOW(e: DDGEdgeEMD) : void {
        this._EMDFLOW.push(e);
    }

    public addINTERSET(e: DDGEdgeInterset) : void {
        this._INTERSET.push(e);
    }

    public addFLOWSPLIT(e: DDGEdgeFlowsplit) : void {
        this._FLOWSPLIT.push(e);
    }

    public get ALL(): DDGEdge[] {
        return (<DDGEdge[]>this._INTERSET).concat(this._FLOWSPLIT, this._EMDFLOW);
    }

    public get length(): number {
        return this._INTERSET.length + this._FLOWSPLIT.length + this._EMDFLOW.length;
    }

}
