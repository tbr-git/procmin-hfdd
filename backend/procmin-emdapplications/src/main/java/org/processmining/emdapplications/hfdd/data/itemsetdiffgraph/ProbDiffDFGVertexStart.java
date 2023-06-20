package org.processmining.emdapplications.hfdd.data.itemsetdiffgraph;

/**
 * Artificial trace start vertex
 * @author brockhoff
 *
 */
public final class ProbDiffDFGVertexStart extends ProbDiffDFGVertex {

	public ProbDiffDFGVertexStart(int freeCatCode) {
		super(freeCatCode, "->", 1, 1);
	}

}
