package org.processmining.emdapplications.hfdd.algorithm;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog.LogType;

public class PerspectiveIteration extends PerspectiveDescriptor {

	private final int iteration;

	public PerspectiveIteration(int iteration) {
		super();
		this.iteration = iteration;
	}

	@Override
	public String getID() {
		return "Focus Iteration " + this.iteration;
	}

	@Override
	public int hashCode() {
		int hash = LogType.FOCUS.hashCode();
		hash = 31 * hash + iteration;
		return hash;
	}

	public int getIteration() {
		return iteration;
	}

}
