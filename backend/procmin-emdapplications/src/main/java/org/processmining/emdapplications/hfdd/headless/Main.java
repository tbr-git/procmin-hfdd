package org.processmining.emdapplications.hfdd.headless;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.stochlangdatasource.SLDSPipelineBuildingUtil;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.base.CCCLogImplFactory;
import org.processmining.emdapplications.data.variantlog.base.CCCVariantImpl;
import org.processmining.emdapplications.data.variantlog.base.CVariantLogFactory;
import org.processmining.emdapplications.data.variantlog.util.LogBuildingException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.util.backgroundwork.CachedBackgroundTaskService;
import org.processmining.emdapplications.hfdd.algorithm.PerspectiveIteration;
import org.processmining.emdapplications.hfdd.algorithm.measure.HFDDVertexMeasurer;
import org.processmining.emdapplications.hfdd.algorithm.measure.HFDDVertexMeasurerFactory;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraphBuilderByMinSupport;
import org.processmining.emdapplications.util.LogLoader;


public class Main {
	
	private final static String pathLogL = "C:/temp/dataset/RTFM/RTFM_l_50.xes";

	private final static String pathLogR = "C:/temp/dataset/RTFM/RTFM_ge_50.xes";

	private static XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;

	private final static Logger logger = LogManager.getLogger( Main.class );
	//
	public static void main(String[] args) throws SLDSTransformerBuildingException, LogBuildingException, SLDSTransformationError {
		XLog logL = LogLoader.loadLog(pathLogL);
		XLog logR = LogLoader.loadLog(pathLogR);
		// Create Data Source
		CVariantLogFactory<CCCVariantImpl> variantLogFactory = new CCCLogImplFactory();
		variantLogFactory.setClassifier(classifier);
		List<StochasticLanguageDataSource<CCCVariantImpl>> lDataSources = null;
		lDataSources = SLDSPipelineBuildingUtil.buildPipelineFrom2XLogs(logL, logR, variantLogFactory);
		BiComparisonDataSource<CCCVariantImpl> biCompDS = new BiComparisonDataSource<CCCVariantImpl>(lDataSources.get(0), lDataSources.get(1));

		// Create HFDD Graph
		HFDDGraphBuilderByMinSupport hfddGraphBuilder = new HFDDGraphBuilderByMinSupport();
		hfddGraphBuilder.setMinRelativeSupport(0.05);
		HFDDGraph graph = hfddGraphBuilder.buildBaseHFDDGraph(biCompDS);
		assertNotNull(graph);
		
		
		// Perspective descriptor
		PerspectiveDescriptor pDesc = new PerspectiveIteration(1);
		
		HFDDVertexMeasurer<CCCVariantImpl> mExec = 
				HFDDVertexMeasurerFactory.getBaseMeasuresWithoutContext(biCompDS, pDesc);
		
		graph.applyMeasure(mExec, biCompDS, true);
		
		logger.info("Done!");
		logger.info("Sleeping...");
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Woke up!");
		
		CachedBackgroundTaskService.getInstance().shutdown();

		return;
		
	}
}
