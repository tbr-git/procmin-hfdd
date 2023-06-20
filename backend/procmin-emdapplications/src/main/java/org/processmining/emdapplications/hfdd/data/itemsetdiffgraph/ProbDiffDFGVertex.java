package org.processmining.emdapplications.hfdd.data.itemsetdiffgraph;

public abstract sealed class ProbDiffDFGVertex permits ProbDiffDFGVertexActivity, ProbDiffDFGVertexStart, ProbDiffDFGVertexEnd {
	
	/**
	 * Category code of the associated activity.
	 */
	private final int categoryCode;
	
	/**
	 * Probability in left log
	 */
	float probLeft;
	
	/**
	 * Probability in right log
	 */
	float probRight;
	
	/**
	 * Name (label) of the vertex
	 */
	private final String name;

	public ProbDiffDFGVertex(int categoryCode, String name, float probLeft, float probRight) {
		super();
		this.categoryCode = categoryCode;
		this.probLeft = probLeft;
		this.probRight = probRight;
		this.name = name;
	}

	public int getCategoryCode() {
		return categoryCode;
	}

	public float getProbLeft() {
		return probLeft;
	}

	public float getProbRight() {
		return probRight;
	}

	public String getName() {
		return name;
	}

}
