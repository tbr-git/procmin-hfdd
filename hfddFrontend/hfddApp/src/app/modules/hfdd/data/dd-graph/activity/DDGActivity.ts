import { DDGActStateData, DDGActivityTextState } from "../activity/DDGActivityTextMeasures"; 
import { DDGTextLabelEl } from "../auxelements/DDGTextLabelEl";
import { DDGEl } from "../base/DDGEl";

export abstract class DDGActivity extends DDGEl {

	// TODO Generate ids
	private static idCounter: number = 0;

	/**
	 * Activity as human-readable string
	 */
	private readonly _activity: string;

	/**
	 * Activity code (optional)
	 */
	private readonly _activityCode: number | undefined;

	/**
	 * Activity abbreviation (optional)
	 */
	private readonly _activityAbbrev: string | undefined;

	/**
	 * Mode how the activitiy is displayed.
	 * Specifically, how the text is diplayed (short, supershort, or long).
	 * 
	 */
	private displayMode: DDGActivityTextState;

	/**
	 * Text labels for each display mode (i.e., super short, short, and normal).
	 */
	private readonly _textLabels: DDGActStateData<DDGTextLabelEl>;

	public constructor(activity: string, activityCode: number|undefined, 
				textLabels: DDGActStateData<DDGTextLabelEl>, displayMode: DDGActivityTextState|undefined) {
		super();
		this._activity = activity;
		this._activityCode = activityCode;
		if (displayMode !== undefined) {
			this.displayMode = displayMode;
		}
		else {
			this.displayMode = DDGActivityTextState.SUPERSHORT;
		}
		
		this._textLabels = textLabels;
	}


	public abstract getPath(): void;

	public override calcMinSize(): void {
		this.textLabel.calcMinSize();
		this.size.heightMin = this.textLabel.size.heightMin;
		this.size.widthMin = this.textLabel.size.widthMin;
	}

	public override calcAssignSubElementTargetSize(): void {
		// Do nothing.
		// It does not have any 
	}

	public override calcSize(): void {
		this.textLabel.calcSize();
		this.size.height = this.size.heightTarget || this.size.heightMin;
		this.size.width = this.size.widthTarget || this.size.widthMin;
	}

	public override applyInternalLayout() {
		const xTextLabel = (this.size.width - this.textLabel.size.width) / 2;
		const yTextLabel = (this.size.height - this.textLabel.size.height) / 2;
		this.textLabel.x = xTextLabel;
		this.textLabel.y = yTextLabel;
		this.textLabel.applyInternalLayout();
	}

	////////////////////////////////////////////////////////////////////////////////
	// Getter and Setter
	////////////////////////////////////////////////////////////////////////////////
	public get activity(): string {
		return this._activity;
	}

	public get activityCode(): number | undefined {
		return this._activityCode;
	}

	public get activityAbbrev(): string | undefined {
		return this._activityAbbrev;
	}

	public get textLabels(): DDGActStateData<DDGTextLabelEl> {
		return this._textLabels;
	}

	public get textLabel(): DDGTextLabelEl {
		return this._textLabels[this.displayMode];
	}

}

