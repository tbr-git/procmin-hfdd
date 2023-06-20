package org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.util.FilterUtil;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfigAdapter;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog.LogType;
import org.processmining.emdapplications.emdconceptdrift.util.backgroundwork.CachedBackgroundTaskService;

public class DiagnosticsPerspective extends LightWeightDiagnosticsPerspective {
	private final static Logger logger = LogManager.getLogger( DiagnosticsPerspective.class );

	private final WindowDiagnosticsData data;
	
	public DiagnosticsPerspective(WindowDiagnosticsData data, MultiViewConfig multiViewConfig, PerspectiveDescriptor description) {
		super(data, multiViewConfig, description);
		this.data = data;

	}

	public WindowDiagnosticsData getWindowData() {
		return this.data;
	}
	
	public DiagnosticsPerspective buildFocusPerspByFiltering(List<XEventClass> lSelectedActivities) throws ViewDataException {
//		XLog xlogL = (XLog) fromPersp.getWindowData().getXLogLeft().clone();
//		XLog xlogR = (XLog) fromPersp.getWindowData().getXLogRight().clone();
		// Filtering does not happen in place anymway
		XLog xlogL = data.getXLogLeft();
		XLog xlogR = data.getXLogRight();
		
		if(!getViewConfig().isConsistent4LogProjection(xlogL)) {
				logger.warn("The trace descriptor factory is not projection invariant "
						+ "and the left window log does not contain sufficient information to ensure projection invariance. ");
		}
		if(!getViewConfig().isConsistent4LogProjection(xlogR)) {
				logger.warn("The trace descriptor factory is not projection invariant "
						+ "and the right window log does not contain sufficient information to ensure projection invariance. ");
		}
		
		XEventClassifier classifier = getClassifier();
		Future<XLog> futureFocusLogL = CachedBackgroundTaskService.getInstance().submit(new Callable<XLog>() {

			@Override
			public XLog call() throws Exception {
				return FilterUtil.filterActivities(xlogL, lSelectedActivities, classifier);
			}
		});
		Future<XLog> futureFocusLogR = CachedBackgroundTaskService.getInstance().submit(new Callable<XLog>() {

			@Override
			public XLog call() throws Exception {
				return FilterUtil.filterActivities(xlogR, lSelectedActivities, classifier);
			}
		});
		XLog focusLogL = null;
		XLog focusLogR = null;
		try {
			focusLogL = futureFocusLogL.get();
			focusLogR = futureFocusLogR.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		MultiViewConfig viewConfig = new MultiViewConfig(getViewConfig());

		ViewConfigAdapter configAdapter = new ViewConfigAdapter();
		configAdapter.setFocusLogSizeLeft(focusLogL.size());
		configAdapter.setFocusLogSizeRight(focusLogR.size());
		configAdapter.adaptConfig(viewConfig);

		WindowDiagnosticsData data = new WindowDiagnosticsData(focusLogL, focusLogR);
		DiagnosticsPerspective p = new DiagnosticsPerspective(data, viewConfig, new PerspectiveDescriptionLog(LogType.FOCUS));
		p.prepareMainView();
		return p;
	}
}
