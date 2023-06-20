import { DDGElSize } from "./DDGElSize";

export abstract class DDGEl {

    /**
     * X position of the upper left corner.
     * Edges depend on this coordinate, therefore, it should be absolut.
     */
    private _x: number;

    /**
     * Y position of the upper left corner.
     * Edges depend on this coordinate, therefore, it should be absolut.
     */
    private _y: number;

    /**
     * Size information of the vertex.
     */
    private _size: DDGElSize;

    public constructor() {
        this._x = 0;
        this._y = 0;
        this._size = {
            heightMin: 0,
            heightTarget: undefined,
            height: 0,
            widthMin: 0,
            widthTarget: undefined,
            width: 0
        };
    }

    /**
     * Calculated the minimum size this of this graphical (node-like) element.
     */
    public abstract calcMinSize(): void;

    /**
     * Calculated the target size for sub-elements
     */
    public abstract calcAssignSubElementTargetSize(): void;

    /**
     * Calculate the actual size. Inbetween the call to calcAssignSubElementTargetSize
     * and this call, the target size of the subcomponents can also be externally modified.
     */
    public abstract calcSize(): void;

    /**
     * Calculate positions for nested internal components
     * based on the minimum and target vertex size.
     */
    public applyInternalLayout(): void {
        // No internal elements -> default, nothing is done
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Getter and Setter
    ////////////////////////////////////////////////////////////////////////////////

    public get size(): DDGElSize {
        return this._size;
    }

    public get x(): number {
        return this._x;
    }

    public set x(x: number) {
        this._x = x;
    }

    public get y(): number {
        return this._y;
    }

    public set y(y: number) {
        this._y = y;
    }

}