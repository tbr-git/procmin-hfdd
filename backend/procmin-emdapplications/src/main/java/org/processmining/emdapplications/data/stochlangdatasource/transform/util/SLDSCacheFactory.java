package org.processmining.emdapplications.data.stochlangdatasource.transform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import org.processmining.emdapplications.data.variantlog.base.CVariant;

public class SLDSCacheFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSCacheFactory.class );

	public SLDSCacheFactory() {
		super();
	}

	@Override
	public StochasticLanguageDataSource<E> build() {
		if(this.parentDataSource == null) {
			logger.error("Missing parent data source.");
		}
		return new StochLangDataSourceCache<>(this.parentDataSource);
	}

}
