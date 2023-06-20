import { DDGActivityTrace } from "../activity/DDGActivityTrace";
import { LogSide } from "../base/LogSide";
import { DDGVertex } from "./DDGVertex";
import { DDGVertexType } from "./DDGVertexType";

export class DDGVertexEMDTrace extends DDGVertex {

    /**
	 * Probability mass assigned to this trace variant
	 */
	private readonly _probability: number;
	/**
	 * Convenience information indicating to which log is belongs (left, right).
	 */
	private readonly _logSide: LogSide;

	/**
	 * Activity names assigned to this trace
	 */
	private readonly _activities: DDGActivityTrace[];

	/*
	* Spacing between activity within the chevron row.
	*/
	private _interActivitySpace: number;

	/**
	 * Ranking based on probability on its side of the signature.
	 */
	private _probabilityRank: number|undefined;

	private _show: boolean; 

	public constructor(id: number, idString: string, visLevel: number,
						probability: number, logSide: LogSide,
						activities: DDGActivityTrace[],
						probabilityRank: number|undefined) {
		super(id, idString, logSide === LogSide.LEFT ? DDGVertexType.EMDLEFT : DDGVertexType.EMDRIGHT, visLevel);
		this._probability = probability;
		this._logSide = logSide;
		this._activities = activities;
		this._interActivitySpace = Math.min(3, this.sizeUnitProb / 8);
		this._probabilityRank = probabilityRank;
		this._show = true;
	}

	public override calcMinSize() {
		if (this.show) {
			////////////////////
			// Init (based on text size)
			////////////////////
			// Init with desired size
			// Particularly relevant to accomodate the text
			this.activities.forEach(a => a.calcMinSize());

			// ----------------------
			// >>>>(activityMinWidth) - interActivitySpace - >>>>>(activityMinWidth) - interActivitySpace - >>>>>(activityMinWidth) ..
			// ----------------------
			// Min height: Minimim height over activities
			// Min width: Sum over activity widths
			[this.size.heightMin, this.size.widthMin] = this.activities
				.map(a => [a.size.heightMin, a.size.widthMin])
				.reduce(([accHeight, accWidth]: number[], [curHeight, curWidth]: number[]) =>
					[Math.max(accHeight, curHeight), accWidth + curWidth]);

			// Add some inter chevron spacing
			this.size.widthMin += Math.max(0, (this._activities.length - 1) * this.interActivitySpace)
		}
		else {
			this.size.widthMin = 0;
			this.size.heightMin = 0;
		}
	}

	public override calcAssignSubElementTargetSize(): void {
		if (this.show) {
			// Get maximim activity min height and min width
			const [maxMinActHeight, maxMinActWidth] = this.activities
				.map(a => [a.size.heightMin, a.size.widthMin])
				.reduce(([accHeight, accWidth]: number[], [curHeight, curWidth]: number[]) =>
					[Math.max(accHeight, curHeight), Math.max(accWidth, curWidth)]);

			// Best scale with probability but fit the text
			const targetActHeight = Math.max(this.probability * this.sizeUnitProb, maxMinActHeight);
			// Fit the text or minimum width
			const targetActWidth = maxMinActWidth;

			// Assign target sizes to activities
			this.activities.forEach(a => {
				a.size.heightTarget = targetActHeight;
				a.size.widthTarget = targetActWidth;
			});
		}
	}

	public override calcSize(): void {
		if (this.show) {
			this.activities.forEach(a => a.calcSize());
			// Hight: Maximum chevron target / Min width
			this.size.height = this._activities
				.map(a => a.size.heightTarget || a.size.heightMin)
				.reduce((aggHeight, curHeight) => Math.max(aggHeight, curHeight));

			// Width: Sum of all chevrons + inter chevron spacing
			this.size.width = this._activities
				.map(a => a.size.widthTarget || a.size.widthMin)
				.reduce((aggWidth, curWidth) => aggWidth + curWidth)
				+ Math.max(0, (this._activities.length - 1) * this.interActivitySpace);
		}
		else {
			this.size.width = 0;
			this.size.height = 0;
		}

	}

	public override applyInternalLayout(): void {
		if (this.show) {
			this.activities.forEach(a => a.applyInternalLayout());

			// Cumsum over activity width + spacing for each element
			let activityPos = this.activities
				.map(a => a.size.width)
				.map((accWidth => (width => accWidth += (width + this.interActivitySpace)))(0));
			// First activity should start at 0 and second after width of first activity + spacing
			activityPos.unshift(0);

			// Assign coordinates
			this.activities.forEach((a, i) => {
				a.x = activityPos[i];
				a.y = 0;
			});
		}
	}

    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////
	public get probability(): number {
		return this._probability;
	}

	public get activities(): DDGActivityTrace[] {
		return this._activities;
	}

	public get logSide(): LogSide {
		return this._logSide;
	}

	public get interActivitySpace(): number {
		return this._interActivitySpace;
	}

	public set interActivitySpace(value: number) {
		this._interActivitySpace = value;
	}

	public get probabilityRank(): number | undefined {
		return this._probabilityRank;
	}

	public set probabilityRank(value: number | undefined) {
		this._probabilityRank = value;
	}

	public get show(): boolean {
		return this._show;
	}

	public set show(value: boolean) {
		this._show = value;
	}

}
