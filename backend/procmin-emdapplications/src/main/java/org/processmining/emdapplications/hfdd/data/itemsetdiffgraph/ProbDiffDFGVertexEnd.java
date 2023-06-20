package org.processmining.emdapplications.hfdd.data.itemsetdiffgraph;

/**
 * Artificial trace end vertex
 * @author brockhoff
 *
 */
public final class ProbDiffDFGVertexEnd extends ProbDiffDFGVertex {

	/**
	 */
	public ProbDiffDFGVertexEnd(int freeCatCode) {
		super(freeCatCode, "|", 1, 1);
	}

}
