package org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph;

import java.util.Collection;
import java.util.Optional;

import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraphVertex;

public final class DDGVertexSetSupport extends DDGVertexSet {

	public DDGVertexSetSupport(int id, DDGVertexProbInfo probabilityInfo, 
			Collection<DDGActivity> activities, Optional<Collection<DDGActivity>> conditionActivities, 
			CSGraphVertex csGraphVertex) {
		super(id, probabilityInfo, 
				activities, conditionActivities, csGraphVertex);
	}

	@Override
	public DDGVertexType getVertexType() {
		return DDGVertexType.INTERSET;
	}
}
