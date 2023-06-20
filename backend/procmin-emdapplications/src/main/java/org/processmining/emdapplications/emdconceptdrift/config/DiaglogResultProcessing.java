package org.processmining.emdapplications.emdconceptdrift.config;

import org.processmining.emdapplications.emdconceptdrift.grounddistances.GroundDistanceFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimedTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedActDurFactory;
import org.processmining.emdapplications.emdconceptdrift.ui.ParameterDiaglogTraceComparison;
import org.processmining.emdapplications.emdconceptdrift.ui.ParameterDialog;

public class DiaglogResultProcessing {
	
	public static void processDialogResults(EMDTraceCompParamBuilder paramBuilder, ParameterDiaglogTraceComparison dialog) {
		switch(paramBuilder.getDistName()) {
		case LEVENSTHEIN:
			break;
		case TIMEBINNEDLVS:
			paramBuilder.setTimeBinType(dialog.getPanelBinnedLVS().getTimeBinType());
			switch(paramBuilder.getEdgeCalculator()) {
			case PERCENTILE:
				paramBuilder.setBinQuantiles(dialog.getPanelBinnedLVS().getPercentilePanel().getQuantiles());
				break;
			case KMEANS:
				paramBuilder.setNbrClusters(dialog.getPanelBinnedLVS().getKMeansPanel().getK());
				break;
			}
			break;
		case TWED:
			paramBuilder.setTimeBinType(dialog.getPanelTEWD().getTimeBinType());
			break;
		default:
			break;
		
		}
		paramBuilder.setDistanceCalculator(GroundDistanceFactory.getDistanceFromParam(paramBuilder.getDistName(), dialog));
	}
}