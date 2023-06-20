import { DDGPictoLogProbs } from "../auxelements/DDGPictoProb";
import { DDGVertexEMDTrace } from "./DDGVertexEMDTrace";
import { DDGVertexHierarchy } from "./DDGVertexHierarchy";
import { DDGVertexType } from "./DDGVertexType";

export class DDGVertexFlowSplit extends DDGVertexHierarchy {

  /**
   * Handles to vertices in the left signature---
   * i.e., left EMD trace vertices.
   */
  private _sigLeft: DDGVertexEMDTrace[];

  /**
   * Handles to vertices in the right signature---
   * i.e., right EMD trace vertices.
   */
  private _sigRight: DDGVertexEMDTrace[];

  /**
   * Total height of the left signature (including intra-vertex spacing).
   */
  private _heightLeftSig: number;

  /**
   * Total height of the right signature (including intra-vertex spacing).
   */
  private _heightRightSig: number;

  /**
   * Associated probability in the left log.
   */
  private readonly _probabilityLeft: number;

  /**
   * Associated probability in the right log.
   */
  private readonly _probabilityRight: number;

  /**
   * Probability pictogram.
   */
  private readonly _probPicto: DDGPictoLogProbs;

  public constructor(id: number, idString: string, visLevel: number,
                    probabilityLeft: number, probabilityRight: number) {
    super(id, idString, visLevel, DDGVertexType.FLOWSPLIT)

    this._sigLeft = [];
    this._sigRight = [];
    this._probabilityLeft = probabilityLeft;
    this._probabilityRight = probabilityRight;
    this._heightLeftSig = 0;
    this._heightRightSig = 0;
    this._probPicto = new DDGPictoLogProbs(this.probabilityLeft, this.probabilityRight);
  }

  public override calcMinSize() {
    this._probPicto.calcMinSize();
		this.size.heightMin = this.probPicto.size.heightMin;
		this.size.widthMin = this.probPicto.size.widthMin;
  }

  public override calcAssignSubElementTargetSize(): void {
    this.probPicto.size.widthTarget = this.size.widthTarget || this.size.widthMin;
    this.probPicto.size.heightTarget = this.size.heightTarget || this.size.heightMin;
    // No subelements that need to be balanced with each other
  }

	public override calcSize() : void {
    this.probPicto.calcSize();
		this.size.height = this.probPicto.size.height;
		this.size.width = this.probPicto.size.width;
	}

  public override applyInternalLayout(): void {
    this.probPicto.x = 0;
    this.probPicto.y = 0;
  }



  public override calcLocalHeight(hierChildrenHeight: number): number {
    // no hierarchical children -> hierChildrenHeight should be 0.
    if (hierChildrenHeight > 0) {
      throw new Error("Flowsplit has subhierarchy height > 0. Unexpected.");
    }
    // Shown vertices 
    const sigLeftShown = this.sigLeft.filter(v => v.show) ;
    const sigRightShown = this.sigRight.filter(v => v.show) ;

    this._heightLeftSig = sigLeftShown
      .map(v => v.size.height).reduce((accHeight, curHeight) => accHeight + curHeight, 0) // Accumulated trace vertex height
      + (sigLeftShown.length - 1) * this.subHierarchySpacing;                        // Spacing

    this._heightRightSig = sigRightShown
      .map(v => v.size.height).reduce((accHeight, curHeight) => accHeight + curHeight, 0) // Accumulated trace vertex height
      + (sigRightShown.length - 1) * this.subHierarchySpacing;                       // Spacing

    const emdHeight = Math.max(this._heightLeftSig, this._heightRightSig);

    return Math.max(this.size.height, emdHeight);
  }

  public override recPositionDescdants(levelXBoxes: [number, number][], yUpperLeft: number) {
    this.x = levelXBoxes[this.visLevel][0];

    // Position this vertx in the mid of the space covered by the
    // entire subhierarchy
    this.y = yUpperLeft + (this.heightHierarchy - this.size.height) / 2.0;


    // Shown vertices 
    const sigLeftShown = this.sigLeft.filter(v => v.show) ;
    const sigRightShown = this.sigRight.filter(v => v.show) ;
    ////////////////////
    // Position vertices in left signature
    ////////////////////
    let yLeft = yUpperLeft + (this.heightHierarchy - this._heightLeftSig) / 2.0;
    for (let vLeft of sigLeftShown) {
      vLeft.x = levelXBoxes[vLeft.visLevel][0] + levelXBoxes[vLeft.visLevel][1] - vLeft.size.width;
      vLeft.y = yLeft;
      yLeft += vLeft.size.height + this.subHierarchySpacing;
    }

    ////////////////////
    // Position vertices in right signature
    ////////////////////
    let yRight = yUpperLeft + (this.heightHierarchy - this._heightRightSig) / 2.0;
    for (let vRight of sigRightShown) {
      vRight.x = levelXBoxes[vRight.visLevel][0];
      vRight.y = yRight;
      yRight += vRight.size.height + this.subHierarchySpacing;
    }

  }

  /**
   * Re-order vertices in the signature access array to minimize crossings in EMD.
   */
  public reorderSigMimimizeEMDCrossings(): void {
    this.sigLeft.sort((u, v) => u.intraLevelSortKey - v.intraLevelSortKey);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Getter and Setter
  ////////////////////////////////////////////////////////////////////////////////
  public get probPicto(): DDGPictoLogProbs {
    return this._probPicto;
  }

  public get sigLeft(): DDGVertexEMDTrace[] {
    return this._sigLeft;
  }

  public set sigLeft(value: DDGVertexEMDTrace[]) {
    this._sigLeft = value;
  }

  public get sigRight(): DDGVertexEMDTrace[] {
    return this._sigRight;
  }
  public set sigRight(value: DDGVertexEMDTrace[]) {
    this._sigRight = value;
  }

  public get probabilityLeft(): number {
    return this._probabilityLeft;
  }

  public get probabilityRight(): number {
    return this._probabilityRight;
  }

}
