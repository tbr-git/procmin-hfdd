package org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey;

public class CSSkRoot extends CSSkVertex {
	
	public CSSkRoot(int id) {
		super(id, true, 1, -1, null);
	}

	@Override
	public String toString() {
		return "CSSkRoot(id=" + getId() + ", isLeft=true)";
	}

}
