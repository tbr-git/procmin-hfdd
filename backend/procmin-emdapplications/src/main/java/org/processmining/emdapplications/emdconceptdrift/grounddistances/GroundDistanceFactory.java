package org.processmining.emdapplications.emdconceptdrift.grounddistances;

import org.processmining.emdapplications.emdconceptdrift.ui.ParameterDiaglogTraceComparison;

public class GroundDistanceFactory {
	
	public static TraceDescDistCalculator getDistanceFromParam(GroundDistances distType, ParameterDiaglogTraceComparison dialog) {
		switch(distType) {
		case LEVENSTHEIN:
			return new LVSStatefullWithEdit();
		case TIMEBINNEDLVS:
			return new TimeBinnedWLVSWithEdit(dialog.getPanelBinnedLVS().getNbrBins());
		case TWED:
			return new TWDStateful(dialog.getPanelTEWD().getNu(), dialog.getPanelTEWD().getLambda());
		default:
			return null;
		
		}
	}

	public static TraceDescDistCalculator getDefaultDistance(GroundDistances distType) {
		switch(distType) {
		case LEVENSTHEIN:
			return new LVSStatefullWithEdit();
		case TIMEBINNEDLVS:
			return new TimeBinnedWeightedLevenshteinStateful(3);
		case TWED:
			return new TWDStateful(0.001, 1);
		default:
			return null;
		
		}
	}

}
