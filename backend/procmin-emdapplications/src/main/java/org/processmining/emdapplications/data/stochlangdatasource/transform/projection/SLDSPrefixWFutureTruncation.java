package org.processmining.emdapplications.data.stochlangdatasource.transform.projection;

import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.contextaware.CVariantCatContSet;
import org.processmining.emdapplications.data.variantlog.transform.CVariantPrefixWithFutureTruncator;
import org.processmining.emdapplications.data.variantlog.transform.CVariantTransformer;

public class SLDSPrefixWFutureTruncation <E extends CVariantCatContSet> extends 
		StochasticLangDataSourceTransformer<E> {
	
	/**
	 * Length of the prefix.
	 */
	private final int prefixLength;
	
	public SLDSPrefixWFutureTruncation(StochasticLanguageDataSource<E> stochLangDataSource, int prefixLength) {
		super(stochLangDataSource);
		this.prefixLength = prefixLength;
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		throw new RuntimeException("Not implemented XLog: prefix truncation");
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		CVariantLog<E> log = super.getVariantLog();
		CVariantTransformer<E> transformer = new CVariantPrefixWithFutureTruncator<>(prefixLength);
		
		return log.applyVariantTransformer(transformer, false);
	}

}
