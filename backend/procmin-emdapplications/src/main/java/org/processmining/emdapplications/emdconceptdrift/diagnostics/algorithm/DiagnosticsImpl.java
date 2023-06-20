package org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm;

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
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective.DiagnosticsPerspective;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective.LogComparisonDiagnosticsPerspectiveBuilder;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.util.FilterUtil;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewSpecFactory;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfigAdapter;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptionLog.LogType;
import org.processmining.emdapplications.emdconceptdrift.util.backgroundwork.CachedBackgroundTaskService;

public class DiagnosticsImpl {
	private final static Logger logger = LogManager.getLogger( DiagnosticsImpl.class );
	
	private XEventClassifier classifier;
	
	private final WindowDiagnosticsData dataInitial;
	
	private final MultiViewConfig baseConfig;
	
	private DiagnosticsPerspective contextPerspective;
	
	private DiagnosticsPerspective focusPerspective;
	
	public DiagnosticsImpl(WindowDiagnosticsData dataInitial, XEventClassifier classifier, MultiViewConfig baseConfig, DiagnosticsPerspective basePerspective) {
		super();
		this.classifier = classifier;
		this.baseConfig = baseConfig;
		this.dataInitial = dataInitial;
		this.contextPerspective = basePerspective;
		this.focusPerspective = null;
	}

	
	public DiagnosticsPerspective updateContextPerspective(List<XEventClass> lSelectedActivities) throws ViewDataException {
		logger.debug("Updating the context perspective.");
		// Clear focus perspective
		focusPerspective = null;

		XLog xlogL = dataInitial.getXLogLeft();
		XLog xlogR = dataInitial.getXLogRight();
		
		XEventClassifier classifier = getClassifier();
		Future<XLog> futureContextLogL = CachedBackgroundTaskService.getInstance().submit(new Callable<XLog>() {

			@Override
			public XLog call() throws Exception {
				return FilterUtil.filterActivities(xlogL, lSelectedActivities, classifier);
			}
		});
		Future<XLog> futureContextLogR = CachedBackgroundTaskService.getInstance().submit(new Callable<XLog>() {

			@Override
			public XLog call() throws Exception {
				return FilterUtil.filterActivities(xlogR, lSelectedActivities, classifier);
			}
		});
		XLog contextLogL = null;
		XLog contextLogR = null;
		try {
			contextLogL = futureContextLogL.get();
			contextLogR = futureContextLogR.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return contextPerspective;
		} catch (ExecutionException e) {
			e.printStackTrace();
		} 
		WindowDiagnosticsData data = new WindowDiagnosticsData(contextLogL, contextLogR);
		
		MultiViewConfig viewConfig = new MultiViewConfig(baseConfig);
		ViewConfigAdapter configAdapter = new ViewConfigAdapter();
		configAdapter.setContextLogSizeLeft(contextLogL.size());
		configAdapter.setContextLogSizeRight(contextLogR.size());
		
		configAdapter.adaptConfig(viewConfig);
		logger.info("Configuring new context view configuration");
		MultiViewSpecFactory.configureComponentsUsed(viewConfig, data.getXLog(), true);
		
		LogComparisonDiagnosticsPerspectiveBuilder contextBuilder = new LogComparisonDiagnosticsPerspectiveBuilder();
		contextBuilder.setDescProjectionInvarianceEnsured(true);
		contextBuilder.setWindowData(data).setMultiViewConfiguration(viewConfig);
		contextBuilder.setDescription(new PerspectiveDescriptionLog(LogType.CONTEXT));
		
		this.contextPerspective = contextBuilder.build();
		this.contextPerspective.prepareMainView();

		return this.contextPerspective;
	}
	
	public DiagnosticsPerspective getContextPerspective() {
		return contextPerspective;
	}
	
	public DiagnosticsPerspective getFocusPerspective() {
		return focusPerspective;
	}
	
	public DiagnosticsPerspective updateFocusPerspective(List<XEventClass> lSelectedActivities) throws ViewDataException {
		this.focusPerspective = contextPerspective.buildFocusPerspByFiltering(lSelectedActivities);
		return this.focusPerspective;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public WindowDiagnosticsData getDataInitial() {
		return dataInitial;
	}

	public MultiViewConfig getBaseConfig() {
		return baseConfig;
	}
	
}
