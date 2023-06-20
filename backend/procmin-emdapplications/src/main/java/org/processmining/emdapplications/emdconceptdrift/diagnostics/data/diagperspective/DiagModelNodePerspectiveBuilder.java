package org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsDataWindowBuilder;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfigAdapter;

public class DiagModelNodePerspectiveBuilder {

	private final static Logger logger = LogManager.getLogger( DiagModelNodePerspectiveBuilder.class );
	
	private Collection<XTrace> tracesL;

	private Collection<XTrace> tracesR;
	
	private DiagnosticsPerspective parentPersp;
	
	private PerspectiveDescriptor description;
	
	public DiagModelNodePerspectiveBuilder() {
		tracesL = null;
		tracesR = null;
		parentPersp = null;
		description = null;
		
	}
	
	public DiagModelNodePerspectiveBuilder setTracesLeft(Collection<XTrace> tracesL) {
		this.tracesL = tracesL;
		return this;
	}
	
	public DiagModelNodePerspectiveBuilder setTracesRight(Collection<XTrace> tracesR) {
		this.tracesR = tracesR;
		return this;
	}
	
	public DiagModelNodePerspectiveBuilder setParentPerspective(DiagnosticsPerspective parentPersp) {
		this.parentPersp = parentPersp;
		return this;
	}

	public DiagModelNodePerspectiveBuilder setDescription(PerspectiveDescriptor description) {
		this.description = description;
		return this;
	}
	
	public LightWeightDiagnosticsPerspective build() throws ViewDataException {
		//TODO Projection invariance handling
		if(tracesL == null) {
			logger.error("Error during building the perspective: No left traces given!");
		}
		if(tracesR == null) {
			logger.error("Error during building the perspective: No right traces given!");
		}
		if(parentPersp == null) {
			logger.error("Error during building the perspective: No parent perspective given!");
		}
		if(description == null) {
			logger.error("Error during building the perspective: No description given!");
		}

		MultiViewConfig viewConfig = new MultiViewConfig(parentPersp.getViewConfig());
		
		ViewConfigAdapter configAdapter = new ViewConfigAdapter();
		configAdapter.setFocusLogSizeLeft(parentPersp.getWindowData().getXLogLeft().size()).setFocusLogSizeRight(parentPersp.getWindowData().getXLogRight().size());
		configAdapter.adaptConfig(viewConfig);
		WindowDiagnosticsData data = new WindowDiagnosticsDataWindowBuilder().addTracesLeft(tracesL).addTracesRight(tracesR).build();	
		LightWeightDiagnosticsPerspective p = new LightWeightDiagnosticsPerspective(data, viewConfig, description);
		p.prepareMainView();
		return p;
		
	}

}
