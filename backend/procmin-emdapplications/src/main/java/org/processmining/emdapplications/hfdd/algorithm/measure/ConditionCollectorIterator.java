package org.processmining.emdapplications.hfdd.algorithm.measure;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public class ConditionCollectorIterator<V extends HFDDVertex, E> extends BreadthFirstIterator<V, E>{
	
	private Map<HFDDVertex, Double> supportLoss;
	
	private final double maxSupportLoss;

	public ConditionCollectorIterator(Graph<V, E> g, V source, double maxSupportLoss) {
		super(g, source);
		this.supportLoss = new HashMap<>();
		this.maxSupportLoss = maxSupportLoss;

	}

	@Override
	protected void encounterVertex(V vertex, E edge) {
		V bfsParent = Graphs.getOppositeVertex(graph, edge, vertex);

		//TODO
		super.encounterVertex(vertex, edge);
	}
	
	
	
	

}
