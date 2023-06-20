package org.processmining.emdapplications.data.stochlangdatasource.transform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLogFactory;
import org.processmining.emdapplications.data.variantlog.util.LogBuildingException;

/**
 * Factory for an XLog injection transformation into an existing pipeline.
 * 
 * Example usage:
 * <p>{@code
 * 
 * }</p>
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSLogInjectionFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSLogInjectionFactory.class );

	/**
	 * Factory to create a variant log from the provided injected log.
	 */
	private CVariantLogFactory<E> factory;
	
	/**
	 * Injected log. Will be used as source for subsequent transformations 
	 * and the variant log
	 */
	private XLog xlog;

	public SLDSLogInjectionFactory() {
		super();
		factory = null;
		xlog = null;
	}
	
	public SLDSLogInjectionFactory<E> setXLog(XLog xlog) {
		this.xlog = xlog;
		return this;
	}

	public SLDSLogInjectionFactory<E> setVariantLogFactory(CVariantLogFactory<E> factory) {
		this.factory = factory;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(factory == null || xlog == null || this.parentDataSource == null) {
			logger.error("Missing mandatory parameters for the log injection.");
		}
		try {
			return new SLDSLogInjection<E>(parentDataSource, factory, xlog);
		} catch (LogBuildingException e) {
			throw new SLDSTransformerBuildingException("Could not build the log injection transformer:\n" + e.getMessage());
		}
	}
	
	
	
}
