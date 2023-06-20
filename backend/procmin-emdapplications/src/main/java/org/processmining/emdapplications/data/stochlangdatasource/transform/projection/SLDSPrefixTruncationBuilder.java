package org.processmining.emdapplications.data.stochlangdatasource.transform.projection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.base.CVariant;

public class SLDSPrefixTruncationBuilder<E extends CVariant> extends SLDSAbstractTransformerFactory<E> {
	private final static Logger logger = LogManager.getLogger( SLDSPrefixTruncationBuilder.class );
	
	private int prefixLength;
	
	public SLDSPrefixTruncationBuilder() {
		prefixLength = 0;
	}
	
	public SLDSPrefixTruncationBuilder<E> setPrefixLength(int prefixLength) {
		this.prefixLength = prefixLength;
		return this;
	}

	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(this.parentDataSource == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the prefix "
					+ "truncation transformer due to misssing arguments");
		}
		
		return new SLDSPrefixTruncation<>(this.parentDataSource, this.prefixLength);
	}
}
