import { DDGEl } from "../base/DDGEl";
import { DDGActivitySet } from "../activity/DDGActivitySet";

export class DDGElSetOverview extends DDGEl {

    /**
     * Activity set.
    */
    private readonly _activitySet: DDGActivitySet[];

    private _spaceInterSetPicto: number;

    public constructor(activitySet: DDGActivitySet[]) {
        super();
        this._activitySet = activitySet;
        this._spaceInterSetPicto = 5;
    }

    public override calcMinSize(): void {
        ////////////////////
        // Activity Set Size
        ////////////////////
        // Init
        this.activitySet.forEach(a => a.calcMinSize());
        const [actSetHeight, actSetWidth] = 
        this.activitySet
        .map(a => [a.size.heightMin, a.size.widthMin])
        .reduce(([aggHeight, aggWidth]: number[], [h, w]: number[]) => 
            [aggHeight + h, Math.max(aggWidth, w)]
        );

        // Min height: Activities + spaces
        this.size.heightMin = actSetHeight + 
            + Math.max(0, this.activitySet.length) * this.spaceInterSetPicto; // Seperators between items

        this.size.widthMin = actSetWidth;
    }

    public override calcAssignSubElementTargetSize(): void {
        const [maxActHeight, maxActWidth] = this.activitySet
            .map(a => [a.size.heightMin, a.size.widthMin])
            .reduce(([aggHeight, aggWidth]: number[], [h, w]: number[]) => 
                [Math.max(aggHeight, h), Math.max(aggWidth, w)]
            );

        // All activities same height and width
        this.activitySet.forEach(a => {
            a.size.heightTarget = maxActHeight;
            a.size.widthTarget = maxActWidth;
        });
    }

    public override calcSize() : void {
        ////////////////////
        // Activity Set Size
        ////////////////////
        // Init Final Height and Width for each item
        this.activitySet.forEach(a => a.calcSize());
        // Total height and final width
        const [actSetHeight, actSetWidth] = 
        this.activitySet
            .map(a => [a.size.heightTarget || a.size.heightMin, a.size.widthTarget || a.size.widthMin])
            .reduce(([aggHeight, aggWidth]: number[], [h, w]: number[]) => 
                [aggHeight + h, Math.max(aggWidth, w)]
            );

        this.size.height = actSetHeight // Rows of activities
            + Math.max(0, this.activitySet.length) * this.spaceInterSetPicto; // Seperators between items

        this.size.width = actSetWidth;
    }


    public override applyInternalLayout(): void {
        ////////////////////////////////////////
        // Position activity set
        // Assumption: All will be contained in a group 
        //  (i.e., coordinates are relative to upper left group corner)
        // Layout:
        // 1 row: activity 1 
        // small space
        // 2 row: activity 2
        // ...
        ////////////////////////////////////////
		this.activitySet.forEach(a => a.applyInternalLayout());
        // Cumsum over activity height + spacing for each element
        let activitySetPos = this.activitySet
            .map(a => a.size.height)
                .map((accHeight => (h => accHeight += (h + this._spaceInterSetPicto)))(0));
        // First activity should start at 0 and second after width of first activity + spacing
        activitySetPos.unshift(0);

        // Assign coordinates
        this.activitySet.forEach((a, i) => {
            a.x = 0;
            a.y = activitySetPos[i];
        });
    }
  
    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////
    public get activitySet(): DDGActivitySet[] {
        return this._activitySet;
    }

    public get spaceInterSetPicto(): number {
        return this._spaceInterSetPicto;
    }

    public set spaceInterSetPicto(value: number) {
        this._spaceInterSetPicto = value;
    }
}