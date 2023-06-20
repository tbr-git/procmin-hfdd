import { DDGEdge } from "./DDGEdge";
import { DDGEdgeType } from "./DDGEdgeType";
import { DDGVertex } from "../vertex/DDGVertex";

export class DDGEdgeFlowsplit extends DDGEdge {
    
    /**
     * Probability flow between the flowsplit and the trace (left log).
     */
    private readonly _probability: number;

    /**
     * Is the add shown at all.
     * In contrast to visible edges, it is not plotted at at.
     */
    private _show: boolean;

    public constructor(id: number, u: DDGVertex, v: DDGVertex, probability: number) {
        super(id, DDGEdgeType.FLOWSPLIT, u, v);
        this._probability = probability;
        this._show = true;
    }

    public calcWidth(edgeUnitProbability: number): void {
        super.width = Math.max(2, this._probability * edgeUnitProbability);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////

    public get probability(): number {
        return this._probability;
    }

    public get show(): boolean {
        return this._show;
    }

    public set show(value: boolean) {
        this._show = value;
    }

    public override get width(): number {
        if (this.show)  {
            return super.width;
        }
        else {
            return 0;
        }
    }
}