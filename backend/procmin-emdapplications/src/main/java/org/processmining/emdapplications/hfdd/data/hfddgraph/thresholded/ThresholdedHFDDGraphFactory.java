package org.processmining.emdapplications.hfdd.data.hfddgraph.thresholded;

import java.util.BitSet;
import java.util.Comparator;

import org.jgrapht.Graph;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurement;
import org.processmining.emdapplications.hfdd.util.BitSetInclusionComparator;
import org.processmining.emdapplications.hfdd.util.GraphConnectionUtil;

public class ThresholdedHFDDGraphFactory {
	
	/**
	 * Build a {@link ThresholdedHFDDGraph} from a given {@link HFDDGraph}.
	 * 
	 * The resulting {@link ThresholdedHFDDGraph} will wrap a graph that is constructed as follows:
	 * 
	 * <p><ol>
	 * <li> Filter all vertices in the provide hfddGraph that have an EMD value 
	 * with the associated perspective that is above the threshold
	 * <li> Connect vertices according to set inclusion
	 * <li> Apply transitive reduction
	 * </ol><p>
	 * 
	 * @param hfddGraph HFDDGraph that should be filtered and reconnected
	 * @param perspective Considered perspective associated with the EMD value to be thresholded 
	 * @param threshold Threshold
	 * @return Filtered graph (where vertices references input vertices but novel edges)
	 */
	public static ThresholdedHFDDGraph buildThresholdedHFDDGraph(HFDDGraph hfddGraph, PerspectiveDescriptor perspective, final double threshold) {
		Graph<HFDDVertex, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

		// Add vertices
		for (HFDDVertex v : hfddGraph.getVertices()) {
			HFDDMeasurement m = v.getVertexInfo().getMeasurements().get(perspective);
			// Add only if metric exceeds threshold
			if (m != null && m.getMetric().get() >= threshold) {
				g.addVertex(v);
			}
		}
		
		// TODO There should be a more efficient way, based on traversal, of doing this
		//================================================================================
		// Generate edges
		//================================================================================
		/*
		 * Generate edges by:
		 * 	1. Generate all edges w.r.t. itemset inclusion
		 * 	2. Apply transitive reduction
		 */
		GraphConnectionUtil.addSPOEdges(g, new Comparator<HFDDVertex>() {

			Comparator<BitSet> activitySetComparator = new BitSetInclusionComparator();

			@Override
			public int compare(HFDDVertex u, HFDDVertex v) {
				return activitySetComparator.compare(
						u.getVertexInfo().getActivities(), 
						v.getVertexInfo().getActivities());
			}
		});
		// Apply transitive reduction to the graph
		TransitiveReduction.INSTANCE.reduce(g);
		
		return new ThresholdedHFDDGraph(g, hfddGraph.getCategoryMapper(), perspective);
		
	}
}
