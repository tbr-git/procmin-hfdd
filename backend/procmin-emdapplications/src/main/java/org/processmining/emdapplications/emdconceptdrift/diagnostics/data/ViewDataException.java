package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.RealizabilityInfo;

public class ViewDataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3284131689617827093L;
	
	public ViewDataException(RealizabilityInfo realInfo) {
		super(realInfo.getProblemType().toString() + ": " + realInfo.getInfo());
	}

}
