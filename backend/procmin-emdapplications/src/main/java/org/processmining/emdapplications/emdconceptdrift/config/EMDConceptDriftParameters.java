package org.processmining.emdapplications.emdconceptdrift.config;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parameters for the mining of a workshop model from an event log.
 * 
 * @author tbrockhoff
 * 
 */
public class EMDConceptDriftParameters {
	
	private final ArrayList<WindowParameter> windows;
	
	private final EMDTraceComparisonParameters paramTraceComp;

	/**
	 * Create default parameter values.
	 */
	protected EMDConceptDriftParameters(ArrayList<WindowParameter> windows, EMDTraceComparisonParameters paramTraceComp) {
		this.windows = windows;
		this.paramTraceComp = paramTraceComp;
	}
	
	public EMDTraceComparisonParameters getParamTraceComparison() {
		return paramTraceComp;
	}

	public Iterator<WindowParameter> getWindowIterator() {
		return windows.iterator();
	}
	
	public int getNbrWindows() {
		return this.windows.size();
	}
	
}
