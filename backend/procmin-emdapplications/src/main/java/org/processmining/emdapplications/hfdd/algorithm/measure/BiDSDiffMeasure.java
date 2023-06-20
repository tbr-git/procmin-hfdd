package org.processmining.emdapplications.hfdd.algorithm.measure;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewRealizationSLDS;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewIdentifier;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ContextAwareEmptyTraceBalancedTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ScalingContext;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;

public class BiDSDiffMeasure {
	private final static Logger logger = LogManager.getLogger( BiDSDiffMeasure.class );

	
	public static Optional<EMDSolContainer> measureEMDBaseLogContext(
			BiComparisonDataSource<? extends CVariant> biCompDS, DescriptorDistancePair trDescDist, 
			PerspectiveDescriptor perspectiveDescription) throws ViewDataException {
		int sizeLogLeft;
		int sizeLogRight;
		try {
			sizeLogLeft = biCompDS.getDataSourceLeftBase().getVariantLog().sizeLog();
			sizeLogRight = biCompDS.getDataSourceRightBase().getVariantLog().sizeLog();
		} catch (SLDSTransformationError e) {
			// That should never happen
			e.printStackTrace();
			throw new IllegalStateException("Base data source is brocken!");
		}
		
		return measureEMDConditional(biCompDS, sizeLogLeft, sizeLogRight, trDescDist, perspectiveDescription);
	}
	
	/**
	 * Measure EMD on conditioned distributions.
	 * 
	 * @param biCompDSEventIntersection Variant logs that satisfy both Events (P(A and B)).
	 * @param sizeLogLeft P(B) for left log
	 * @param sizeLogRight P(B) for right log
	 * @param trDescDist Trace descriptor and trace distance
	 * @param perspectiveDescription Descriptor for the perspective on the data that is created by the measurement
	 * @return EMD Solution for conditional problem
	 * @throws ViewDataException 
	 */
	public static Optional<EMDSolContainer> measureEMDConditional(
			BiComparisonDataSource<? extends CVariant> biCompDSEventIntersection, int sizeLogLeft, int sizeLogRight,
			DescriptorDistancePair trDescDist, PerspectiveDescriptor perspectiveDescription) throws ViewDataException {
		
		Window2OrderedStochLangTransformer langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
					sizeLogLeft, sizeLogRight, ScalingContext.GLOBAL);
		
		ViewConfig viewConfig = new ViewConfig(langTransformer, trDescDist, 
				new ViewIdentifier(trDescDist.getShortDescription() + " - " + langTransformer.getShortDescription()));
		
		return measureEMD(biCompDSEventIntersection, viewConfig, perspectiveDescription);

	}

	/**
	 * Compare the two data sources using EMD and the provided config
	 * @param biCompDS
	 * @param viewConfig View configuration (distance and transformation into stochastic language)
	 * @param perspectiveDescription Descriptor for the perspective on the data that is created by the measurement
	 * @return EMD Solution
	 * @throws ViewDataException 
	 */
	public static Optional<EMDSolContainer> measureEMD(BiComparisonDataSource<? extends CVariant> biCompDS, 
			ViewConfig viewConfig, PerspectiveDescriptor perspectiveDescription) throws ViewDataException {
		
		////////////////////
		// Realization Sanity Check
		////////////////////
		ViewRealizationSLDS real = viewConfig.createViewOnData(biCompDS, perspectiveDescription);
		if(hasRealizabilityProblems(real)) { 		// Check realizability
			return Optional.empty();
		}

		////////////////////
		// EMD
		////////////////////
		EMDSolContainer emdSol = null;
		real.populate();
		emdSol = real.getEMDSol();
		return Optional.of(emdSol);
	}

	/**
	 * Check for realizibility problems and do some basic logging.
	 * @param real
	 * @return
	 */
	private static boolean hasRealizabilityProblems(ViewRealizationSLDS real) {
		if(!real.isRealizable()) {
			switch(real.getRealizibilityInfo().getProblemType()) {
				case DATASOURCE_BROKEN:
				case DATA_SPEC_ERROR:
					logger.error("Data source problem during measurment execution: {}!", real.getRealizibilityInfo().getInfo());
				case LITTLE_SUPPORT_LEFT:
				case LITTLE_SUPPORT_LEFTRIGHT:
				case LITTLE_SUPPORT_RIGHT:
				case NO_SUPPORT_LEFT:
				case NO_SUPPORT_LEFTRIGHT:
				case NO_SUPPORT_RIGHT:
					return true;
				default:
					logger.error("Unknown problem during measurment execution!");
					return true;
			}
		}
		return false;
	}
}
