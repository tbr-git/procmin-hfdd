package org.processmining.emdapplications.hfdd.data.csgraph.graph;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSkEdge;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSkItemsetVertex;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSkVertex;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public final class CSGraphVertexSupport extends CSGraphVertex{

	public CSGraphVertexSupport(HFDDVertex hfddVertexRef) {
		super(hfddVertexRef);
	}

	@Override
	public Pair<? extends CSSkVertex, ? extends CSSkVertex> createAndAddSankeySubgraphs(Graph<CSSkVertex, CSSkEdge> g,
			Iterator<Integer> idGenerator) {
		// Left itemset Sankey vertex
		CSSkItemsetVertex vertexLeft = new CSSkItemsetVertex(idGenerator.next(), true, this);
		// Right itemset Sankey vertex
		CSSkItemsetVertex vertexRight = new CSSkItemsetVertex(idGenerator.next(), false, this);
		// Add vertices to graph
		g.addVertex(vertexLeft);
		g.addVertex(vertexRight);
		
		return Pair.of(vertexLeft, vertexRight);
	}

}
