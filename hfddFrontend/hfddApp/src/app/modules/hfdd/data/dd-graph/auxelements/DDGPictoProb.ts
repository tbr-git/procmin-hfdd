import { DDGEl } from '../base/DDGEl';

export class DDGPictoLogProbs extends DDGEl {

    private readonly _probLeftLog: DDGLogProbDonut;
    
    private readonly _probRightLog: DDGLogProbDonut;

    public constructor(probISLeft: number, probISRight: number) {
        super();
        // Entries for the probability circle pictograms
        this._probLeftLog = {
            outerR: 20,
            innerR: 15,
            entries: [
                {probability: probISLeft, type: DDGPictoProbType.COVERED, startAngle: 0, endAngle: probISLeft * 2 * Math.PI}, // Covered left
                {probability: 1 - probISLeft, type: DDGPictoProbType.UNCOVERED, startAngle: probISLeft * 2 * Math.PI, endAngle: 2 * Math.PI} // Uncovered left
            ]           
        };

        this._probRightLog = {
            outerR: 13,
            innerR: 8,
            entries: [
                {probability: probISRight, type: DDGPictoProbType.COVERED, startAngle: 0, endAngle: probISRight * 2 * Math.PI}, // Covered right
                {probability: 1 - probISRight, type: DDGPictoProbType.UNCOVERED, startAngle: probISRight * 2 * Math.PI, endAngle: 2 * Math.PI} // Uncovered right
            ]
        };
    }

    ////////////////////////////////////////////////////////////
    // Override
    ////////////////////////////////////////////////////////////
    public calcMinSize(): void {
        this.size.heightMin = 2 * Math.max(this._probLeftLog.outerR, this._probRightLog.outerR);
        this.size.widthMin = this.size.heightMin;
    }

    public calcAssignSubElementTargetSize(): void {
        // No real subelements. Nothing neeeds to be done
    }

    public calcSize(): void {
        this.size.height = this.size.heightTarget || this.size.heightMin;
        this.size.width = this.size.widthTarget || this.size.widthMin;
    }

    ////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////
    public get probLeftLog(): DDGLogProbDonut {
        return this._probLeftLog;
    }

    public get probRightLog(): DDGLogProbDonut {
        return this._probRightLog;
    }
}

export interface DDGLogProbDonut {
    /**
     * Outer radius
     */
    outerR: number;

    /**
     * Innter radius
     */
    innerR: number;

    /**
     * Entries
     */
    entries: DDGPictoProbEntry[];


}

export interface DDGPictoProbEntry {
    /**
     * Probability for the pictrogram entry.
     */
    probability: number;
    
    /**
     * "Probability"Type of the.
     */
    type: DDGPictoProbType;

    /**
     * Start angle for an arc-based visualization
     */
    startAngle: number;

    /**
     * End angle for an arc-based visualization
     */
    endAngle: number;
}

export enum DDGPictoProbType {
    // Itemset covers fraction of the log (i.e., percentage of traces that contain this itemset)
    COVERED = "Covered",
    // Itemset does not cover this fraction of the log
    UNCOVERED = "Uncovered"
}