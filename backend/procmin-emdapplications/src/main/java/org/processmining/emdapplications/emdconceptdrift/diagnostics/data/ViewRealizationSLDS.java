package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.EMDSolver;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.RealizabilityChecker;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.RealizabilityInfo;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.RealizationProblemType;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewRealizationMeta;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;

public class ViewRealizationSLDS extends ViewRealization {

	private final static Logger logger = LogManager.getLogger( ViewRealizationSLDS.class );
	
	private final Window2OrderedStochLangTransformer langTransformer;
	
	private final DescriptorDistancePair descDistPair;
	
	private final BiComparisonDataSource<? extends CVariant> biDataSource;

	private final RealizabilityInfo viewRealInfo;
	
	
	public ViewRealizationSLDS(ViewRealizationMeta viewDescription, DescriptorDistancePair descDistPair, 
			Window2OrderedStochLangTransformer langTransformer, BiComparisonDataSource<? extends CVariant> biDataSource) {
		super(viewDescription);

		this.langTransformer = langTransformer;
		this.descDistPair = descDistPair;
		this.biDataSource = biDataSource;
		this.viewRealInfo = RealizabilityChecker.checkRealizability(viewDescription, descDistPair, 
				langTransformer, biDataSource);

	}

	@Override
	public boolean isRealizable() {
		return viewRealInfo.isRealizable();
	}
	
	public RealizabilityInfo getRealizibilityInfo() {
		return viewRealInfo;
	}

	@Override
	public void populate() throws ViewDataException {
		if(viewRealInfo.isRealizable()) {
			if(!emdSol.isPresent() && viewRealInfo.isRealizable()) {
				//TODO
				try {
					emdSol = Optional.of(EMDSolver.getLPSolution(biDataSource.getDataSourceLeft().getVariantLog(), biDataSource.getDataSourceRight().getVariantLog(), 
							descDistPair.getDescriptorFactory(), descDistPair.getDistance(), langTransformer));
				} catch (SLDSTransformationError e) {
					logger.error("A variant transformation error occured. Cannot populate view");
					String errorMessage = "Querying data from the data source failed. " 
							+ "There has been an error during the transformation: " + e.getMessage();
					ViewDataException viewDataException = new ViewDataException(new RealizabilityInfo(RealizationProblemType.DATASOURCE_BROKEN, errorMessage));
					viewDataException.setStackTrace(e.getStackTrace());
					throw viewDataException;
				}
			}
		}
		else {
			throw new ViewDataException(viewRealInfo);
		}
	}

}
