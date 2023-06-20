package org.processmining.emdapplications.data.stochlangdatasource;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerFactory;
import org.processmining.emdapplications.data.stochlangdatasource.transform.util.SLDSLogInjectionFactory;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLogFactory;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.data.variantlog.util.CategoricalLogBuildingUtil;
import org.processmining.emdapplications.data.variantlog.util.LogBuildingException;
import org.processmining.emdapplications.data.xlogutil.TraceTransformerInplace;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.XLogLightJoin;


public class SLDSPipelineBuildingUtil {
	private final static Logger logger = LogManager.getLogger( SLDSPipelineBuildingUtil.class );

	/**
	 * Method that extends a transformation pipeline.
	 * 
	 * Given a factory, it sets the parent data source and builds the decorated pipeline
	 * @param <E>
	 * @param dataSource Data source to be extended
	 * @param factory Extension factory that build the transformation decorator
	 * @return Decorated data source
	 * @throws SLDSTransformerBuildingException 
	 */
	public static<E extends CVariant> StochasticLanguageDataSource<E> extendPipeline(
			StochasticLanguageDataSource<E> dataSource, 
			SLDSTransformerFactory<E> factory) throws SLDSTransformerBuildingException {
		factory.setParentDataSource(dataSource);
		return factory.build();
	}

	/**
	 * Method that applies the same extension to each element in a list of data sources.
	 * 
	 * Extends each element applying {@link SLDSPipelineBuildingUtil#extendPipeline(StochasticLanguageDataSource, SLDSTransformerFactory)}.
	 * @param <E>
	 * @param dataSources List of data sources
	 * @param factory Extension factory
	 * @return List of decorated data sources
	 * @throws SLDSTransformerBuildingException
	 */
	public static<E extends CVariant> List<StochasticLanguageDataSource<E>> extendPipelines(
			List<StochasticLanguageDataSource<E>> dataSources, 
			SLDSTransformerFactory<E> factory) throws SLDSTransformerBuildingException {
		// Extended sources
		List<StochasticLanguageDataSource<E>> extendedSources = new LinkedList<>();
		
		// Extend each source
		for(StochasticLanguageDataSource<E> ds : dataSources) {
			extendedSources.add(extendPipeline(ds, factory));
		}
		
		return extendedSources;
	}
	

	/**
	 * 
	 * Build a split stochastic language data source given two event logs.
	 * 
	 * In addition to applying {@link #buildPipelineFrom2XLogs(XLog, XLog, CVariantLogFactory)},
	 * additional inplace log transformers can be applied.
	 * 
	 * If such transformers are provided, the logs are copied and inplace trace transformers are applied.
	 * Afterwards, the datasource is build
	 * @param <E> Variant implementation type
	 * @param xlog1 First log that defines the first endpoint of the split pipeline
	 * @param xlog2 Second log that defines the second endpoint of the split pipeline
	 * @param variantLogFactory Variant Log factory to be applied.
	 * @param preTraceTransformers List of inplace trace transformers.
	 * @return
	 * @throws SLDSTransformerBuildingException
	 * @throws LogBuildingException
	 */
	public static<E extends CVariant> List<StochasticLanguageDataSource<E>> buildPipelineFrom2XLogs(
			XLog xlog1, XLog xlog2, CVariantLogFactory<E> variantLogFactory, 
			List<TraceTransformerInplace> preTraceTransformers) throws SLDSTransformerBuildingException, LogBuildingException {
		if (preTraceTransformers.size() > 0) {
			logger.debug("Copying logs before applying inplace trace transformers");
			xlog1 = (XLog) xlog1.clone();
			xlog2 = (XLog) xlog2.clone();

			// Applying inplace trace transformers
			for (TraceTransformerInplace transformer : preTraceTransformers) {
				logger.debug("Applying in-place trace transformation: {} ...", transformer.getDescription());
				transformer.transform(xlog1, variantLogFactory.getClassifier());
				transformer.transform(xlog2, variantLogFactory.getClassifier());
				logger.debug("In-place transformation done");
			}
		}
		return buildPipelineFrom2XLogs(xlog1, xlog2, variantLogFactory);

	}

	/**
	 * Build a split stochastic language data source given two event logs.
	 * 
	 * This function instantiates an artificial combined data source by a light log join and, then, 
	 * applies two "transformations" that can be seen as if precisely the traces from the provided logs 
	 * are filtered out of the joined log. 
	 * @param <E> Variant implementation type
	 * @param xlog1 First log that defines the first endpoint of the split pipeline
	 * @param xlog2 Second log that defines the second endpoint of the split pipeline
	 * @param variantLogFactory Variant Log factory to be applied.
	 * @return
	 * @throws SLDSTransformerBuildingException
	 * @throws LogBuildingException
	 */
	public static<E extends CVariant> List<StochasticLanguageDataSource<E>> buildPipelineFrom2XLogs(
			XLog xlog1, XLog xlog2, CVariantLogFactory<E> variantLogFactory) 
					throws SLDSTransformerBuildingException, LogBuildingException {
		List<XLog> logs = new LinkedList<>();
		logs.add(xlog1);
		logs.add(xlog2);
		XLogLightJoin joinedLog = new XLogLightJoin(logs);
		
		// If no mapping is provided, we try to derive one from the log union
		// Usually, it does not make sense to use different categorical codes for two variant logs in the same pipeline
		if(variantLogFactory.getCategoryMapper() == null) {
			logger.warn("No acitvity map specified for building a pipeline from two logs." +
						" I expect that both logs should use the same mapping, therefore, I create one from the union.");
			// Without classifier we cannot create the categorical codes 
			if(variantLogFactory.getClassifier() == null) {
				logger.error("Could not create the mapping from the union as not classifier is specified!)");
				throw new LogBuildingException("No classifier specified. Cannot build a pipeline from two logs without classifier and pre-defined mapping!");
			}

			// Derive the categorical codes and use them for the factory
			CategoryMapper catMapper = CategoricalLogBuildingUtil.getActivityMapping(joinedLog, 
					variantLogFactory.getClassifier());
			variantLogFactory.setCategoryMapper(catMapper);
		}
		
		logger.debug("Instantiating base variant log...");
		SLDSConnectorFactory<E> connectorFactory = new SLDSConnectorFactory<E>();
		// Base Source (Lightly Merged logs)
		StochasticLanguageDataSource<E> dataSourceBase = connectorFactory
				.setVariantLogFactory(variantLogFactory).setXLog(joinedLog).build();
		logger.debug("Instantiated base variant logs.");
		// Inject first log
		SLDSLogInjectionFactory<E> injectionFactory1 = new SLDSLogInjectionFactory<>();
		injectionFactory1.setXLog(xlog1).setVariantLogFactory(variantLogFactory).setParentDataSource(dataSourceBase);
		// Inject second log
		SLDSLogInjectionFactory<E> injectionFactory2 = new SLDSLogInjectionFactory<>();
		injectionFactory2.setXLog(xlog2).setVariantLogFactory(variantLogFactory).setParentDataSource(dataSourceBase);
		
		return SLDSSplitter.splitDataStream(dataSourceBase, injectionFactory1, injectionFactory2);
	}
	
}
