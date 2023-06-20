import { DDGEl } from "../base/DDGEl";
import { Position } from "../base/Position";

export class DDGTextLabelEl extends DDGEl {

    /**
     * The text to be displayed
     */
    private readonly _text: string;    

    /**
     * Position for the anchor of the text.
     * We assum center of text
     */
    private _posTextAnchor: Position;

    // Even though fonts could be better styled via css, they are important
    // for the size computation. Therefore, we explicitly embedd them here.
    /**
     * Font type
     */
    private readonly _font: string;

    /**
     * Font size
     */
    private readonly _fontSize: number;

    public constructor(text: string,  font: string, fontSize: number, 
            textHeight: number, textWidth: number) {
        super();
        this._text = text;
        this.size.heightMin = textHeight;
        this.size.widthMin = textWidth;
        this._posTextAnchor = {x: 0, y: 0};
        this._font = font;
        this._fontSize = fontSize;
    }

    ////////////////////////////////////////////////////////////
    // Override
    ////////////////////////////////////////////////////////////

    public calcMinSize(): void {
        // Already clear from the construction.
    }

    public calcAssignSubElementTargetSize(): void {
        // No subelements nothin needs to be done.
    }

    public calcSize(): void {
        this.size.height = this.size.heightTarget || this.size.heightMin;
        this.size.width = this.size.widthTarget || this.size.widthMin;
    }

    public override applyInternalLayout(): void {
        // By default SVG text will be placed w.r.t. left baseline
        // We try to center the text
        this._posTextAnchor.x = this.size.width / 2;
        this._posTextAnchor.y = this.size.height / 2;
    }

    ////////////////////////////////////////////////////////////
    // Getter
    ////////////////////////////////////////////////////////////

    public get text(): string {
        return this._text;
    }

    public get posTextAnchor(): Position {
        return this._posTextAnchor;
    }

    public get fontSize(): number {
        return this._fontSize;
    }

    public get font(): string {
        return this._font;
    }

} 