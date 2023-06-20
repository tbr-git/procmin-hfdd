package org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.serialization;

import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.ProbDiffDFG;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ProbDiffDFGFrontendInfo {
	
	private final String dotString;
	
	@JsonSerialize(using = ProbDiffDFGAttributeInfoSerializer.class)
	private final ProbDiffDFG diffDFG;

	public ProbDiffDFGFrontendInfo(ProbDiffDFG diffDFG) {
		super();
		this.diffDFG = diffDFG;
		this.dotString = diffDFG.getDotString();
	}

	public ProbDiffDFG getDiffDFG() {
		return diffDFG;
	}

	public String getDotString() {
		return dotString;
	}
}
