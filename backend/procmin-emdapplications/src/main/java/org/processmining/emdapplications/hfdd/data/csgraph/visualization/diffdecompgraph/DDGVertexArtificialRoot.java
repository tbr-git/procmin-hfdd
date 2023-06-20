package org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph;

public final class DDGVertexArtificialRoot extends DDGVertex {

	
	public DDGVertexArtificialRoot(int id) {
		super(id, "ddg-artRoot", null);
	}

	@Override
	public DDGVertexType getVertexType() {
		return DDGVertexType.ARTROOT;
	}

}
