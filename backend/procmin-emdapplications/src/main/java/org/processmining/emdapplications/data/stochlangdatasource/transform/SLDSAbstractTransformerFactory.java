package org.processmining.emdapplications.data.stochlangdatasource.transform;

import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.variantlog.base.CVariant;

public abstract class SLDSAbstractTransformerFactory<E extends CVariant> implements SLDSTransformerFactory<E> {

	/**
	 * Parent data source.
	 */
	protected StochasticLanguageDataSource<E> parentDataSource;


	public SLDSAbstractTransformerFactory() {
		parentDataSource = null;
	}
	
	@Override
	public SLDSAbstractTransformerFactory<E> setParentDataSource(StochasticLanguageDataSource<E> parentDataSource) {
		this.parentDataSource = parentDataSource;
		return this;
	}
}
