package org.processmining.emdapplications.hfdd.algorithm.measure;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewIdentifier;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ContextAwareEmptyTraceBalancedTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ScalingContext;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.CSGraphResidualLogTransformer;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigConstantProvider;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigProvider;
import org.processmining.emdapplications.hfdd.data.csgraph.CSMeasurementTypes;
import org.processmining.emdapplications.hfdd.data.csgraph.PerspectiveCSResidual;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraph;

public class CSVertexResidualMeasurer<T extends CVariant> extends CSVertexMeasurerBase<T> {
	
	private final static Logger logger = LogManager.getLogger( CSVertexResidualMeasurer.class );
	
	private final static PerspectiveDescriptor pDesc = new PerspectiveCSResidual();
	
	/**
	 * Activity distance pair that will be used to transform the variant log into
	 * an EMD problem.
	 */
	private final DescriptorDistancePair trDescDist;
	
	public CSVertexResidualMeasurer(CSGraph csGraph, DescriptorDistancePair trDescDist) {
		super(csGraph);
		this.trDescDist = trDescDist;
	}
	
	@Override
	public PerspectiveDescriptor getMeasurementDescription() {
		return pDesc;
	}

	@Override
	protected HFDDVertexMeasurerImpl<T> getMeasurer(BiComparisonDataSource<? extends T> biCompDS) {
		// Create language transformer
		Window2OrderedStochLangTransformer langTransformer;
		try {
			langTransformer = new ContextAwareEmptyTraceBalancedTransformer(biCompDS.getDataSourceLeftBase().getVariantLog().sizeLog(), 
					biCompDS.getDataSourceRightBase().getVariantLog().sizeLog(), ScalingContext.GLOBAL);
		} catch (SLDSTransformationError e) {
			logger.error("Failed to instantiate the measurement executor because the size of the context log could not be determined. "
					+ "Fallback to standard normalization!");
			langTransformer = new Window2SimpleNormOrdStochLangTransformer();
		}

		// Distance + Trace descriptor factory + transformer => view
		ViewConfig viewConfig = new ViewConfig(langTransformer, this.trDescDist,
				new ViewIdentifier(this.trDescDist.getShortDescription() 
						+ " - " + langTransformer.getShortDescription()));
		
		////////////////////////////////////////
		// Setup Measurer
		////////////////////////////////////////
		// External context log transformer
		HFDDVertexLogTransformerOuterContext<T> residualTransformer = 
				new CSGraphResidualLogTransformer<T>(getCsGraph());
		List<HFDDVertexLogTransformerOuterContext<T>> transformersOuterContext = 
				Collections.singletonList(residualTransformer);
		
		// View provider
		ViewConfigProvider<T> viewProvider = new ViewConfigConstantProvider<T>(viewConfig);
		
		HFDDVertexMeasurerImpl<T> measurer = new HFDDVertexMeasurerImpl<>(
				new PerspectiveCSResidual(), 
				viewProvider,
				Optional.of(transformersOuterContext));
		// TODO Auto-generated method stub
		return measurer;
	}

	@Override
	protected CSMeasurementTypes getMeasurementType() {
		return CSMeasurementTypes.RESIDUAL;
	}
}
