export interface DDGVertexProbabilityInfo {
    /**
     * Non-conditioned probability left
     */
	probNonCondLeft: number; 			  
    
    /**
     * Non-conditioned probability right
     */
	probNonCondRight: number;

    /**
     * Residual Non-conditioned probability left
     */
	probNonCondResLeft: number;

    /**
     * Residual Non-conditioned probability right
     */
	probNonCondResRight: number;

    /**
     * Conditioned probability left
     */
	probCondLeft: number | undefined;

    /**
     * Conditioned probability right
     */
	probCondRight: number | undefined;

    /**
     * Residual conditioned probability left
     */
	probCondResLeft: number | undefined;

    /**
     * Residual conditioned probability right
     */
    probCondResRight: number | undefined;
}