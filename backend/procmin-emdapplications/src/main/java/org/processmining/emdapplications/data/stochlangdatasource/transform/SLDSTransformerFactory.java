package org.processmining.emdapplications.data.stochlangdatasource.transform;

import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.variantlog.base.CVariant;

public interface SLDSTransformerFactory<E extends CVariant> {
	
	/**
	 * Build the transformer.
	 * @return Extended pipeline/decorated data source
	 * @throws SLDSTransformerBuildingException 
	 */
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException;

	/**
	 * Set the pipeline end that will be extended/decorated by this factory.
	 * @param parentDataSource
	 * @return Factory instance
	 */
	public SLDSTransformerFactory<E> setParentDataSource(StochasticLanguageDataSource<E> parentDataSource);
}
