package org.processmining.emdapplications.data.stochlangdatasource.transform.selection;

import java.util.BitSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;

public class SLDSFilterVariantsIffContainsAllCategoriesBuilder<E extends CVariant> extends SLDSAbstractTransformerFactory<E> { 
	private final static Logger logger = LogManager.getLogger( SLDSFilterVariantsContainCategoryFactory.class );
	
	/**
	 * Used event classifier.
	 */
	private XEventClassifier classifier;

	/**
	 * Activities that a trace should / should not contain (at least one)
	 */
	private BitSet activitiesCodes;
	
	/**
	 * Keep the traces that contain any activity (true) or drop them (false).
	 * Defaults to true
	 */
	private boolean keep;

	/**
	 * Category mapper required when using 
	 */
	private CategoryMapper categoryMapper;
	
	public SLDSFilterVariantsIffContainsAllCategoriesBuilder() {
		super();
		classifier = null;
		activitiesCodes = null;
		keep = true;
	}

	/**
	 * Set the <b>activity codes</b> from which <b>none or any</b> are contained.
	 * 
	 * @param activitCodes Activity codes 
	 * @return
	 */
	public SLDSFilterVariantsIffContainsAllCategoriesBuilder<E> setActivities(BitSet activityCodes) {
		this.activitiesCodes = activityCodes;
		return this;
	}

	/**
	 * Set the {@link this#classifier}.
	 * @param classifier Event classifier to be used.
	 * @return Factory
	 */
	public SLDSFilterVariantsIffContainsAllCategoriesBuilder<E> setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	/**
	 * Set the {@link this#keep}.
	 * @param keep Keep traces (true); or drop them (false)
	 * @return Factory
	 */
	public SLDSFilterVariantsIffContainsAllCategoriesBuilder<E> keepTraces(boolean keep) {
		this.keep = keep;
		return this;
	}


	@Override
	public StochasticLanguageDataSource<E> build() throws SLDSTransformerBuildingException {
		if(classifier == null || this.parentDataSource == null || this.categoryMapper == null) {
			logger.error("Missing mandatory parameters.");
			throw new SLDSTransformerBuildingException("Could not instantiate the variant contains activity (category) filter "
					+ "due to misssing arguments");
		}
		if(activitiesCodes.size() == 0) {
			logger.warn("Setting up a trace/variant contains any activity filter with empty activity set");
		}
		int[] arrActivityCodes = this.activitiesCodes.stream().toArray();
		return new SLDSFilterVariantsIffContainsAllCategories<>(this.parentDataSource, arrActivityCodes, 
				this.classifier, this.categoryMapper, this.keep);
	}

	/**
	 * Set the category mapper.
	 * @param categoryMapper
	 * @return
	 */
	public SLDSFilterVariantsIffContainsAllCategoriesBuilder<E> setCategoryMapper(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
		return this;
	}
}
