export interface DDGElSize {
    /**
     * Minimum width
     */
    widthMin: number;
    
    /**
     * Target width 
     * */ 
    widthTarget: number | undefined;
    
    /**
     * Current, actual width
     */
    width: number;

    /**
     * Minimum height
     */
    heightMin: number;

    /**
     * Target height 
     * */ 
    heightTarget: number | undefined;
    
    /**
     * Current, actual height
     */
    height: number;
}