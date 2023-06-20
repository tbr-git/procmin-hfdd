import { DDGActStateData, DDGActivityTextState } from '../activity/DDGActivityTextMeasures'

export class TextSizeComputer {
    /**
     * Context that can be used to measure text.
     */
    private readonly _measureContext: CanvasRenderingContext2D | null;

    /**
     * Save the text measurements for all activities to re-use them.
     */
    private _activityTextMeasures: Map<string, Readonly<DDGActStateData<[number, number]>>>;

    /**
     * Text sizes for auxiliary texts.
     * Used for text that cannot be abbreviated.
     */
    private _auxTextMeasures: Map<string, Readonly<[number, number]>>;

    /**
     * Current font used to measure text.
     */
    private _font: string = 'Roboto';

    /**
     * Current font size used to measure text.
     */
    private _fontsize: number = 12;

    /*
    * Measures for a sink activity. 
    */
    public static _activitySinkTextMeasures: Readonly<DDGActStateData<[number, number]>> = {
        [DDGActivityTextState.NONE]:  [0, 0],
        [DDGActivityTextState.SUPERSHORT]:  [12, 5],
        [DDGActivityTextState.SHORT]:  [12, 5],
        [DDGActivityTextState.NORMAL]:  [12, 5]
        };

    /**
     * Measures for an activity that cam be used to denote the empty set -> No activity.
     */
    public static activityEmptySetMeasures: Readonly<DDGActStateData<[number, number]>> = {
        [DDGActivityTextState.NONE]:  [0, 0],
        [DDGActivityTextState.SUPERSHORT]:  [12, 5],
        [DDGActivityTextState.SHORT]:  [12, 5],
        [DDGActivityTextState.NORMAL]:  [12, 5]
        };


    public constructor(measureContext: CanvasRenderingContext2D | null) {
        this._measureContext = measureContext;
        this._activityTextMeasures = new Map<string, Readonly<DDGActStateData<[number, number]>>>();
        this._auxTextMeasures = new Map<string, Readonly<[number, number]>>();
    }

    public getMeasures4Activity(actLabel: string, actLabelAbbrev: string, actLabelSuperShort: string): Readonly<DDGActStateData<[number, number]>> {
        let actTextMeasures : Readonly<DDGActStateData<[number, number]>>;
        const query = `${this.font}:${this.fontsize}:${actLabel}`
        // Already computed
        if (this._activityTextMeasures.has(query)) {
            actTextMeasures = this._activityTextMeasures.get(query)!;
        }
        else {
        // Can measure on canvas
        if (this._measureContext !== null) {
            this._measureContext.font = this._fontsize + "pt " + this._font;
            actTextMeasures = {
            [DDGActivityTextState.NONE]:  [0, 0],
            [DDGActivityTextState.SUPERSHORT]:  [this._fontsize, this._measureContext.measureText(actLabelSuperShort).width],
            [DDGActivityTextState.SHORT]:  [this._fontsize, this._measureContext.measureText(actLabelAbbrev).width],
            [DDGActivityTextState.NORMAL]:  [this._fontsize, this._measureContext.measureText(actLabel).width]
            }
        }
        else {
            // Default
            actTextMeasures = {
            [DDGActivityTextState.NONE]:  [0, 0],
            [DDGActivityTextState.SUPERSHORT]:  [this._fontsize, 20],
            [DDGActivityTextState.SHORT]:  [this._fontsize, 30],
            [DDGActivityTextState.NORMAL]:  [this._fontsize, 40]
            }
        }
        this._activityTextMeasures.set(query, actTextMeasures);
        }

        return actTextMeasures;
    }

    /**
     * Measure the text size [height, width] of the provided text.
     * 
     * If the measurement canvas could not be instantiated [fontsize, 20] is returned as default value.
     * @param text Text to be measured.
     * @returns Size (height, width) for the text.
     */
    public measureAuxText(text: string) : Readonly<[number, number]> {
        let textMeasure: Readonly<[number, number]>;
        // Query string
        const query = `${this.font}:${this.fontsize}:${text}`
        // Already computed
        if (this._auxTextMeasures.has(query)) {
            textMeasure = this._auxTextMeasures.get(query)!;
        }
        else {
            // Can measure on canvas
            if (this._measureContext !== null) {
                this._measureContext.font = this._fontsize + "pt " + this._font;
                textMeasure = [this._fontsize, this._measureContext.measureText(text).width];
            }
            else {
                // Default
                textMeasure = [this._fontsize, 20];
            }
            this._auxTextMeasures.set(query, textMeasure);
        }

        return textMeasure;
    }
    ////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////

    public static getActivityMeasuresForSink(): DDGActStateData<[number, number]> {
        return this._activitySinkTextMeasures;
    }
    public get fontsize(): number {
        return this._fontsize;
    }

    public set fontsize(value: number) {
        this._fontsize = value;
    }

    public get font(): string {
        return this._font;
    }
    
    public set font(value: string) {
        this._font = value;
    }

}