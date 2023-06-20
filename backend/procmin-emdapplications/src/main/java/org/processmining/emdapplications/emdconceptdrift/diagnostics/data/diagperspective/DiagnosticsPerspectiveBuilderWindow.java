package org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsDataLogBuilder;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog.LogType;

public class DiagnosticsPerspectiveBuilderWindow {
	private final static Logger logger = LogManager.getLogger( DiagnosticsPerspectiveBuilderWindow.class.getName() );
	
	private XLog xlog;
	
	private int w_size;
	
	private int traceIndex;
	
	private MultiViewConfig multiViewConfig;
	
	private boolean isDescProjectionInvarianceEnsured = false;
	
	public DiagnosticsPerspectiveBuilderWindow() {
		
	}
	
	public DiagnosticsPerspectiveBuilderWindow setLog(XLog xlog) {
		this.xlog = xlog;
		return this;
	}

	public DiagnosticsPerspectiveBuilderWindow setWindowSize(int w_size) {
		this.w_size = w_size;
		return this;
	}

	public DiagnosticsPerspectiveBuilderWindow setTraceIndex(int traceIndex) {
		this.traceIndex = traceIndex;
		return this;
	}
		
	public DiagnosticsPerspectiveBuilderWindow setMultiViewConfiguration(MultiViewConfig viewConfig) {
		this.multiViewConfig = viewConfig;
		return this;
	}
	
	public DiagnosticsPerspective build() throws ViewDataException {
		WindowDiagnosticsData data = new WindowDiagnosticsDataLogBuilder().addTracesFromLog(xlog, traceIndex, w_size).build();
		if(isDescProjectionInvarianceEnsured) {
			multiViewConfig.getViewIterator().forEachRemaining(
					v -> data.enhanceLogByDescMetaData(v.getDescDistPair().getDescriptorFactory()));
		}
		data.clearLifeCycleStarts();
		DiagnosticsPerspective p = new DiagnosticsPerspective(data, multiViewConfig, new PerspectiveDescriptionLog(LogType.CONTEXT));
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
