package org.processmining.emdapplications.data.stochlangdatasource.transform.util;

import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.CVariantLogFactory;
import org.processmining.emdapplications.data.variantlog.util.LogBuildingException;

/**
 * Transformer that can be used to inject a particular {@link XLog}.
 * 
 * The log injection log is considered a transformation and, therefore, all calls
 * to transformed logs and variant logs will start on the given log. 
 * 
 * Therefore, this class automatically cleans all caches in the provided data source.
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSLogInjection<E extends CVariant> extends StochasticLangDataSourceTransformer<E> { 

	/**
	 * Factory to create a variant log from the provided injected log.
	 */
	private CVariantLogFactory<E> factory;
	
	/**
	 * Injected log. Will be used as source for subsequent transformations 
	 * and the variant log
	 */
	private XLog xlog;

	/**
	 * Variant log built over the injected log.
	 */
	private final CVariantLog<E> variants;
	
	/**
	 * Initialize the log injection transformation.
	 * 
	 * <b>This will clean all caches in the provided parent data source!</b>
	 * @param stochLangDataSource
	 * @param factory
	 * @param xlog
	 * @throws LogBuildingException
	 */
	public SLDSLogInjection(StochasticLanguageDataSource<E> stochLangDataSource, CVariantLogFactory<E> factory, XLog xlog) throws LogBuildingException {
		super(stochLangDataSource);
		this.factory = factory;
		this.xlog = xlog;
		factory.setLog(this.xlog);
		this.variants = factory.build();
		
		// Since the injected log will be the start of all subsequent transformation,
		// we can clear all caches
		stochLangDataSource.clearCaches();
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		// We consider the injected log as transformed log
		return xlog;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		return this.variants;
	}

	@Override
	public CVariantLogFactory<E> getVariantLogFactory() {
		return factory;
	}

}
