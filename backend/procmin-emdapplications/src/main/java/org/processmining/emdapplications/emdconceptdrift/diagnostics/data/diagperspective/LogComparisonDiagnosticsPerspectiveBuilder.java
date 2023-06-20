package org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;

public class LogComparisonDiagnosticsPerspectiveBuilder {
	private final static Logger logger = LogManager.getLogger( LogComparisonDiagnosticsPerspectiveBuilder.class.getName() );
	
	private XLog xlogL;
	
	private XLog xlogR;
	
	private WindowDiagnosticsData data;
	
	private MultiViewConfig multiViewConfig;
	
	private boolean isDescProjectionInvarianceEnsured = false;
	
	private PerspectiveDescriptor description = null;
	
	public LogComparisonDiagnosticsPerspectiveBuilder() {
		
	}
	
	public LogComparisonDiagnosticsPerspectiveBuilder setLogLeft(XLog xlogL) {
		this.xlogL = xlogL;
		return this;
	}

	public LogComparisonDiagnosticsPerspectiveBuilder setLogRight(XLog xlogR) {
		this.xlogR = xlogR;
		return this;
	}
	
	public LogComparisonDiagnosticsPerspectiveBuilder setWindowData(WindowDiagnosticsData data) {
		this.data = data;
		return this;
	}

	public LogComparisonDiagnosticsPerspectiveBuilder setDescription(PerspectiveDescriptor description) {
		this.description = description;
		return this;
	}

	public LogComparisonDiagnosticsPerspectiveBuilder setMultiViewConfiguration(MultiViewConfig viewConfig) {
		this.multiViewConfig = viewConfig;
		return this;
	}
	
	public DiagnosticsPerspective build() throws ViewDataException {
		if(description == null) {
			logger.error("No perspective description given!");
		}
		if(data == null) {
			data = new WindowDiagnosticsData(xlogL, xlogR);
		}

		if(isDescProjectionInvarianceEnsured) {
			multiViewConfig.getViewIterator().forEachRemaining(
					v -> data.enhanceLogByDescMetaData(v.getDescDistPair().getDescriptorFactory()));
		}
		data.clearLifeCycleStarts();
		DiagnosticsPerspective p = new DiagnosticsPerspective(data, multiViewConfig, description);
		p.prepareMainView();
		return p;
	}
	
	public boolean isDescProjectionInvarianceEnsured() {
		return isDescProjectionInvarianceEnsured;
	}

	public void setDescProjectionInvarianceEnsured(boolean isDescProjectionInvariant) {
		this.isDescProjectionInvarianceEnsured = isDescProjectionInvariant;
	}

}
