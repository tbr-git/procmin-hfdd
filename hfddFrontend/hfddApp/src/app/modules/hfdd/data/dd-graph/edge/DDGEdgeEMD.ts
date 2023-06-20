import { DDGEdge } from "./DDGEdge";
import { DDGEdgeType } from "./DDGEdgeType";
import { DDGVertex } from "../vertex/DDGVertex";

export class DDGEdgeEMD extends DDGEdge {
    
    /**
     * Associated probability flow.
     */
    private readonly _probFlow: number;

    /**
     * Trace distance;
     */
    private readonly _cost: number;

    /**
     * Cost contribution in LP.
     * Equals to probability * ground distance.
     */
    private readonly _lpCostContrib: number;

    /**
     * Is the add shown at all.
     * In contrast to visible edges, it is not plotted at at.
     */
    private _show: boolean;

    public constructor(id: number, u: DDGVertex, v: DDGVertex, probFlow: number, cost: number, lpCostContrib: number) {
        super(id, DDGEdgeType.EMDFLOW, u, v);
        this._probFlow = probFlow;
        this._cost = cost;
        this._lpCostContrib = lpCostContrib;
        this._show = true;

        if (Math.abs(this.lpCostContrib - (this.probFlow * this.cost)) > Number.EPSILON) {
            throw new Error("LP cost contribution should be equal to the product of probability flow and cost!");
        }
    } 

    public calcWidth(edgeUnitProbability: number): void {
        super.width = Math.max(2, this._probFlow * edgeUnitProbability);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////

    public get probFlow(): number {
        return this._probFlow;
    }

    public get cost(): number {
        return this._cost;
    }

    public get lpCostContrib(): number {
        return this._lpCostContrib;
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