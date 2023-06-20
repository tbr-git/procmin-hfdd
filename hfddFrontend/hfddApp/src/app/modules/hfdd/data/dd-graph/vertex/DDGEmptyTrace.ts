import { DDGActivityTrace } from "../activity/DDGActivityTrace";
import { LogSide } from "../base/LogSide";
import { DDGVertex } from "./DDGVertex";
import { DDGVertexEMDTrace } from "./DDGVertexEMDTrace";
import { DDGVertexType } from "./DDGVertexType";

export class DDGEmptyTrace extends DDGVertexEMDTrace {

    /**
	 * Probability mass assigned to this trace variant
     * if not hiding empty-to-empty mappings
	 */
	private readonly _realProbability: number;

    private _e2eHiddenProbability: number | undefined;

    private _enableE2EHiding: boolean;

    private _hideWhenHidingEnabled: boolean;

	public constructor(id: number, idString: string, visLevel: number,
						probability: number, logSide: LogSide,
						activities: DDGActivityTrace[],
						probabilityRank: number|undefined) {

        super(id, idString, visLevel, probability, logSide, activities, probabilityRank);
        this._realProbability = probability;
        this._enableE2EHiding = false;
        this._hideWhenHidingEnabled = false;

	}

    public get realProbability(): number {
        return this._realProbability;
    }

	public get e2eHiddenProbability(): number | undefined {
		return this._e2eHiddenProbability;
	}

	public set e2eHiddenProbability(value: number | undefined) {
		this._e2eHiddenProbability = value;
	}

    public set enableE2EHiding(value: boolean) {
        this._enableE2EHiding = value;
        this.show = !this.hideWhenHidingEnabled;
    }

    public override get probability(): number {
        if (this._enableE2EHiding && this.e2eHiddenProbability !== undefined) {
            return this.e2eHiddenProbability;
        }
        else {
            return this._realProbability;
        }
    }

    public set hideWhenHidingEnabled(value: boolean) {
        this._hideWhenHidingEnabled = value;
    }

    public get hideWhenHidingEnabled(): boolean{
        return this._hideWhenHidingEnabled;
    }

}

