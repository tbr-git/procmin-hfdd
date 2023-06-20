package org.processmining.emdapplications.hfdd.data.csgraph;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog.LogType;

public class PerspectiveCSResidual extends PerspectiveDescriptor {

	public PerspectiveCSResidual() {
		super();
	}

	@Override
	public String getID() {
		return "Cornerstone Graph Residual Perspective";
	}

	@Override
	public int hashCode() {
		int hash = LogType.FOCUS.hashCode();
		//TODO
		return hash;
	}

}
