import { DDGActivitySet } from "../activity/DDGActivitySet";
import { DDGVertexHierarchy } from "./DDGVertexHierarchy";
import { DDGVertexType } from "./DDGVertexType";
import { DDGElSetOverview } from "../auxelements/DDGElSetOverview";
import { DDGPictoLogProbs } from "../auxelements/DDGPictoProb";
import { DDGTextLabelEl } from "../auxelements/DDGTextLabelEl";
import { DDGVertexProbabilityInfo } from "./DDGVertexProbabilityInfo";

/*
* Vertex Layout:
*    ---------------------------
*    |SEP
*    |svgGroup: Rows of activities
*    |  Row 1: Activity 1
*    |  Row 2: Activity 2
*    |2*SEP (child)
*    |svgGroup: Probability pictograms | SEP
*    |SEP
*    ---------------------------
*/
export class DDGVertexHSet extends DDGVertexHierarchy {

  /**
   * Title of the activity set container.
   */
  private readonly _actSetOverviewTitle: DDGTextLabelEl;

  /**
   * Activity set container and display element.
   */
  private readonly _actSetOverview: DDGElSetOverview;

  /**
   * Activity set container on which this vertex was conditioned.
   * Can be undefined if there are none.
   */
  private readonly _condActSetOverview:  DDGElSetOverview | undefined;

  /**
   * Title of the probability pictogram.
   */
  private readonly _probPictoTitle: DDGTextLabelEl;

  /**
   * Probability pictogram.
   */
  private readonly _probPicto: DDGPictoLogProbs;

  /**
   * Conditional Probability pictogram if vertex is conditioned.
   */
  private readonly _probCondPicto: DDGPictoLogProbs | undefined;

  /**
   * Is a cornerstone vertex; otherwise it is a support vertex
   */
  private readonly _isCSVertex: boolean;

  /**
   * Probability info considering associated probability masses of non-empty traces.
   * For example, condition / non-conditioned, vertex / vertex-resdiual.
   */
  private readonly _probInfo: DDGVertexProbabilityInfo;
  

  public constructor(id: number, idString: string, visLevel: number, vertexType: DDGVertexType,
                    probInfo: DDGVertexProbabilityInfo,
                    activitySet: DDGActivitySet[], isCSVertex: boolean,
                    actSetOverviewTitle: DDGTextLabelEl, probPictoTitle: DDGTextLabelEl,
                    condActivitySet ?: DDGActivitySet[]) {

    super(id, idString, visLevel, vertexType) ;
    this._probInfo = probInfo;
    this._actSetOverviewTitle = actSetOverviewTitle;
    this._actSetOverview = new DDGElSetOverview(activitySet);
    this._condActSetOverview = (condActivitySet === undefined) ? undefined : new DDGElSetOverview(condActivitySet);
    this._probPictoTitle = probPictoTitle;
    this._isCSVertex = isCSVertex;

    this._probPicto = new DDGPictoLogProbs(this.probInfo.probNonCondLeft, this.probInfo.probNonCondRight);
    if (this._condActSetOverview != undefined 
        && this.probInfo.probCondLeft != undefined 
        && this.probInfo.probCondRight != undefined) {
      this._probCondPicto = new DDGPictoLogProbs(this.probInfo.probCondLeft, 
        this.probInfo.probCondRight);
    }
    else {
      this._probCondPicto = undefined;
    }
  }

  public override calcMinSize(): void {

    ////////////////////
    // Subelements
    ////////////////////
    // Activity Set 
    this._actSetOverviewTitle.calcMinSize();
    this._actSetOverview.calcMinSize();
    this._condActSetOverview?.calcMinSize();
    // Probability Pictogram
    this._probPictoTitle.calcMinSize();
    this._probPicto.calcMinSize();
    this._probCondPicto?.calcMinSize();


    ////////////////////
    // Resulting size of THIS element
    ////////////////////
    this.size.heightMin = 2 * this.innerVertexSep  + 4 * this.childSep // Boarder
      + this._actSetOverviewTitle.size.heightMin // Title  activity sets
      + (Math.max(this._actSetOverview.size.heightMin, this._condActSetOverview?.size.heightMin ?? 0))  // Activity set
      + this._probPictoTitle.size.heightMin // Title probability pictograms
      + (Math.max(this._probPicto.size.heightMin, this._probCondPicto?.size.heightMin ?? 0));  // Probability Pictograms

    this.size.widthMin = 2 * this.innerVertexSep  // Left right inner vertex margin
      + Math.max(
          this._actSetOverviewTitle.size.widthMin,  // Title  activity sets
          this._actSetOverview.size.widthMin + (this._condActSetOverview?.size.widthMin ?? 0)  // Activity set
            + (this._condActSetOverview === undefined ? 0 : this.childSep) ,    // Separator between activities and conditions 
          this._probPictoTitle.size.widthMin, // Title probability pictograms
          this._probPicto.size.widthMin + (this._probCondPicto?.size.widthMin ?? 0)) // Probability Pictograms
  }

  public override calcAssignSubElementTargetSize(): void {
    this.size.heightTarget = this.size.heightTarget || this.size.heightMin;
    this.size.widthTarget = this.size.widthTarget || this.size.widthMin;

    this._actSetOverview.calcAssignSubElementTargetSize();
    this._condActSetOverview?.calcAssignSubElementTargetSize();

    // Titles should stretch over width
    this._actSetOverviewTitle.size.widthTarget = this.size.widthTarget - 2 * this.innerVertexSep;
    this.probPictoTitle.size.widthTarget = this.size.widthTarget - 2 * this.innerVertexSep;

    // Relativley distribute space to probability histograms 
    const weightProbPictoBase = this.probPicto.size.widthMin / (this.probPicto.size.widthMin + (this.probCondPicto?.size.widthMin ?? 0));
    const targetHeightProbPictos = Math.max(this.probPicto.size.heightMin + (this.probCondPicto?.size.heightMin ?? 0))
    this.probPicto.size.widthTarget = weightProbPictoBase * (this.size.widthTarget - 2 * this.innerVertexSep);
    this.probPicto.size.heightTarget = targetHeightProbPictos;
    this.probPicto.calcAssignSubElementTargetSize();

    if (this.probCondPicto != undefined) {
      const weightProbPictoCond = this.probCondPicto.size.widthMin / (this.probPicto.size.widthMin + (this.probCondPicto?.size.widthMin ?? 0));
      this.probCondPicto.size.widthTarget = weightProbPictoCond * (this.size.widthTarget - 2 * this.innerVertexSep);
      this.probCondPicto.size.heightTarget = targetHeightProbPictos;
      this.probCondPicto.calcAssignSubElementTargetSize();
    }
  }

  public override calcSize() : void {
    ////////////////////
    // Subelements
    ////////////////////
    // Activity Set 
    this._actSetOverviewTitle.calcSize();
    this._actSetOverview.calcSize();
    this._condActSetOverview?.calcSize();
    // Probability Pictogram
    this._probPictoTitle.calcSize();
    this._probPicto.calcSize();
    this._probCondPicto?.calcSize();

    ////////////////////
    // Resulting size of THIS element
    ////////////////////
    this.size.height = 2 * this.innerVertexSep  + 4 * this.childSep // Boarder
      + this._actSetOverviewTitle.size.height // Title  activity sets
      + (Math.max(this._actSetOverview.size.height, this._condActSetOverview?.size.height ?? 0))  // Activity set
      + this._probPictoTitle.size.height // Title probability pictograms
      + (Math.max(this._probPicto.size.height, this._probCondPicto?.size.height ?? 0));  // Probability Pictograms

    this.size.width = 2 * this.innerVertexSep  // Left right inner vertex margin
      + Math.max(
          this._actSetOverviewTitle.size.width,  // Title  activity sets
          this._actSetOverview.size.width + (this._condActSetOverview?.size.width ?? 0)  // Activity set
            + (this._condActSetOverview === undefined ? 0 : this.childSep) ,    // Separator between activities and conditions 
          this._probPictoTitle.size.width, // Title probability pictograms
          this._probPicto.size.width + (this._probCondPicto?.size.width ?? 0)); // Probability Pictograms
  }

  public override calcLocalHeight(hierChildrenHeight: number): number {
    return Math.max(this.size.height, hierChildrenHeight);
  }

  public override applyInternalLayout(): void {

    this._actSetOverviewTitle.applyInternalLayout();
    this._actSetOverview.applyInternalLayout();
    this.condActSetOverview?.applyInternalLayout();
    this._probPictoTitle.applyInternalLayout();
    this.probPicto.applyInternalLayout();
    this.probCondPicto?.applyInternalLayout();

    ////////////////////////////////////////
    // Position subelements
    ////////////////////////////////////////
    let curHeight: number = this.innerVertexSep;
    this._actSetOverviewTitle.x = this.innerVertexSep;
    this._actSetOverviewTitle.y = curHeight;
    curHeight += this._actSetOverviewTitle.size.height + this.childSep;

    // Activity Set
    this._actSetOverview.x = this.innerVertexSep;
    this._actSetOverview.y = curHeight;

    // Condition Activities
    if (this._condActSetOverview !== undefined) {
      this._condActSetOverview.x = this.innerVertexSep + this.actSetOverview.size.width + this.childSep;
      this._condActSetOverview.y = curHeight;
    }

    curHeight += Math.max(this._actSetOverview.size.height, this._condActSetOverview?.size.height ?? 0) + 2 * this.childSep;

    this._probPictoTitle.x = this.innerVertexSep;
    this._probPictoTitle.y = curHeight;
    curHeight += this._probPictoTitle.size.height + this.childSep;

    this._probPicto.x = this.innerVertexSep;
    this._probPicto.y = curHeight;

    if (this.probCondPicto != undefined) {
      this.probCondPicto.x = this.innerVertexSep + this.probPicto.size.width + this.childSep;
      this.probCondPicto.y = curHeight;
    }
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Getter and Setter
  ////////////////////////////////////////////////////////////////////////////////
  public get probInfo(): DDGVertexProbabilityInfo {
      return this._probInfo;
    }


  public get isCSVertex(): boolean {
    return this._isCSVertex;
  }

  public get actSetOverview(): DDGElSetOverview {
    return this._actSetOverview;
  }

  public get condActSetOverview(): DDGElSetOverview | undefined {
    return this._condActSetOverview;
  }

  public get probPicto(): DDGPictoLogProbs {
    return this._probPicto;
  }

  public get probCondPicto(): DDGPictoLogProbs | undefined {
    return this._probCondPicto;
  }

  public get probPictoTitle(): DDGTextLabelEl {
    return this._probPictoTitle;
  }

  public get actSetOverviewTitle(): DDGTextLabelEl {
    return this._actSetOverviewTitle;
  }

}

