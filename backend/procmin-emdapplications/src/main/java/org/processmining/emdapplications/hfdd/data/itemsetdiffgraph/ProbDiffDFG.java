package org.processmining.emdapplications.hfdd.data.itemsetdiffgraph;

import java.text.DecimalFormat;

import org.jgrapht.Graph;
import org.python.modules.math_erf;

public class ProbDiffDFG {

	/**
	 * Handle to the graph structure that actually holds the data
	 */
	private final Graph<ProbDiffDFGVertex, ProbDiffDFGEdge> probDFG;

	public ProbDiffDFG(Graph<ProbDiffDFGVertex, ProbDiffDFGEdge> probDFG) {
		super();
		this.probDFG = probDFG;
	}
	
	public Graph<ProbDiffDFGVertex, ProbDiffDFGEdge> getGraph() {
		return probDFG;
	}
	
	public String getDotString() {
		final float maxLineWidth = 5f;
		// Format used to display probabilities in the picture (avoid excessive number of digits)
		DecimalFormat formatProbDisplay = new DecimalFormat("#.###");
		StringBuilder builder = new StringBuilder();
		builder.append("digraph{ ");
		builder.append("rankdir=\"LR\";");
		for (ProbDiffDFGVertex v: probDFG.vertexSet()) {
			builder.append("n" + v.getCategoryCode());
			builder.append(" [");
			builder.append("id=" + v.getCategoryCode());
			builder.append(", label=\"" + v.getName() + "(" + 
					formatProbDisplay.format(v.probLeft)  + " | " + formatProbDisplay.format(v.probRight) + ")\"");
			builder.append(", shape=box");
			//builder.append(", probLeft=" + v.probLeft);
			//builder.append(", probRight=" + v.probRight);
			builder.append(" ];");
		}
		
		for (ProbDiffDFGEdge e : probDFG.edgeSet()) {
			builder.append("n" + probDFG.getEdgeSource(e).getCategoryCode());
			builder.append(" -> ");
			builder.append("n" + probDFG.getEdgeTarget(e).getCategoryCode());
			builder.append(" [");
			builder.append("id=" + e.getId());
			builder.append(", label=\"(" + 
					formatProbDisplay.format(e.probLeft)  + " | " + formatProbDisplay.format(e.probRight) + ")\"");
			//builder.append(", probLeft=" + e.probLeft);
			//builder.append(", probRight=" + e.probRight);
			builder.append(", penwidth=" + Math.max(Math.max(e.probLeft, e.probRight) * maxLineWidth, 0.001));
			builder.append(" ];");
		}
		builder.append("}");
		
		return builder.toString();
	}
}
