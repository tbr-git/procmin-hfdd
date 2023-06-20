package org.processmining.emdapplications.hfdd.algorithm.measure;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ProbMassNonEmptyTrace;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.FilterTag;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDLogTransformStep;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigProvider;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurement;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurementEMDSol;
import org.processmining.emdapplications.hfdd.util.EMDSolAnalyzer;

public class HFDDVertexMeasurerImpl<T extends CVariant> implements HFDDVertexMeasurer<T> {
	private final static Logger logger = LogManager.getLogger( HFDDVertexMeasurerImpl.class );

	/**
	 * Given the graph data source, prepare it according to the outer context.
	 * For example, if we condition on another vertex, this can reduce the
	 * datasource to the conditioned variants.  
	 */
	private final Optional<List<HFDDVertexLogTransformerOuterContext<T>>> logTransOuterCont;
	
	/**
	 * Name of the perspective that is realized using the provided {@link this#viewConfig}.
	 */
	private final PerspectiveDescriptor perspectiveDescription;
	
	private final ViewConfigProvider<T> viewConfigProvider;

	public HFDDVertexMeasurerImpl(PerspectiveDescriptor perspectiveDescription, 
			ViewConfigProvider<T> viewConfigProvider,
			Optional<List<HFDDVertexLogTransformerOuterContext<T>>> logTransOuterCont) {
		super();
		this.viewConfigProvider = viewConfigProvider;
		this.perspectiveDescription = perspectiveDescription;
		this.logTransOuterCont = logTransOuterCont;
	}
	
	@Override
	public ProbMassNonEmptyTrace getProbabilityMassNonEmpty(HFDDVertex v, BiComparisonDataSource<T> biCompDS) {
		////////////////////
		// Outer Context
		////////////////////
		List<HFDDLogTransformStep<? extends T>> vertexLogFilterStack = new LinkedList<>();
		HFDDLogTransformStep<T> lastStep = new HFDDLogTransformStep<T>(biCompDS, FilterTag.IN);
		vertexLogFilterStack.add(0, lastStep);
		if (logTransOuterCont.isPresent()) {
			// Apply all transformers
			for (HFDDVertexLogTransformerOuterContext<T> transOutC : logTransOuterCont.get()) {
				try {
					lastStep = transOutC.getDataSourceOuterContext(v, lastStep.biCompDS());
					// Push to filter stack
					vertexLogFilterStack.add(0, lastStep);

				} catch (SLDSTransformerBuildingException e1) {
					logger.error("Error during HFDDVertex outer context transformation");
					e1.printStackTrace();
					return null;
				}
			}
		}

		////////////////////
		// Inner Context: Prepare log 
		////////////////////
		BiComparisonDataSource<T> biCompDSVertex;
		try {
			biCompDSVertex = v.getVertexInfo().createVertexLog(lastStep.biCompDS());
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			return null;
		}
		vertexLogFilterStack.add(0, new HFDDLogTransformStep<T>(biCompDSVertex, FilterTag.VERTEX));
		
		////////////////////
		// View Config
		////////////////////
		ViewConfig viewConfig = this.viewConfigProvider.provideViewConfig(v, vertexLogFilterStack);
		
		////////////////////
		// Non-empty Trace Probability Mass
		////////////////////
		// Probability mass non-empty traces
		ProbMassNonEmptyTrace vertexLogPropNonEmpty;
		try {
			vertexLogPropNonEmpty = viewConfig.getLangTransformer().probabilityMassNonEmptyTraces(
					biCompDSVertex.getDataSourceLeft().getVariantLog(),
					biCompDSVertex.getDataSourceRight().getVariantLog());
			return vertexLogPropNonEmpty;
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public HFDDMeasurement measureVertex(HFDDVertex v, BiComparisonDataSource<T> biCompDS, boolean save) {
		////////////////////
		// Outer Context
		////////////////////
		List<HFDDLogTransformStep<? extends T>> vertexLogFilterStack = new LinkedList<>();
		HFDDLogTransformStep<T> lastStep = new HFDDLogTransformStep<T>(biCompDS, FilterTag.IN);
		vertexLogFilterStack.add(0, lastStep);
		if (logTransOuterCont.isPresent()) {
			// Apply all transformers
			for (HFDDVertexLogTransformerOuterContext<T> transOutC : logTransOuterCont.get()) {
				try {
					lastStep = transOutC.getDataSourceOuterContext(v, lastStep.biCompDS());
					// Push to filter stack
					vertexLogFilterStack.add(0, lastStep);

				} catch (SLDSTransformerBuildingException e1) {
					logger.error("Error during HFDDVertex outer context transformation");
					e1.printStackTrace();
					return new HFDDMeasurement(perspectiveDescription, null, 0, 0, false, true);
				}
			}
		}

		////////////////////
		// Inner Context: Prepare log 
		////////////////////
		BiComparisonDataSource<T> biCompDSVertex;
		try {
			biCompDSVertex = v.getVertexInfo().createVertexLog(lastStep.biCompDS());
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			return new HFDDMeasurement(perspectiveDescription, null, 0, 0, false, true);
		}
		vertexLogFilterStack.add(0, new HFDDLogTransformStep<T>(biCompDSVertex, FilterTag.VERTEX));
		
		////////////////////
		// View Config
		////////////////////
		ViewConfig viewConfig = this.viewConfigProvider.provideViewConfig(v, vertexLogFilterStack);
		
		////////////////////
		// EMD
		////////////////////
		Optional<EMDSolContainer> emdSol;
		try {
			emdSol = BiDSDiffMeasure.measureEMD(biCompDSVertex, viewConfig, perspectiveDescription);
		} catch (ViewDataException e) {
			e.printStackTrace();
			return new HFDDMeasurement(perspectiveDescription, viewConfig, 0, 0, false, true);
		}

		if (emdSol.isEmpty()) {
			logger.error("Error during HFDDVertex measurement! Could not realize EMD!");
			return new HFDDMeasurement(perspectiveDescription, viewConfig, 0, 0, false, true);
		}

		// Probability mass non-empty traces
		ProbMassNonEmptyTrace vertexLogPropNonEmpty;
		try {
			vertexLogPropNonEmpty = viewConfig.getLangTransformer().probabilityMassNonEmptyTraces(
					biCompDSVertex.getDataSourceLeft().getVariantLog(),
					biCompDSVertex.getDataSourceRight().getVariantLog());
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			return new HFDDMeasurement(perspectiveDescription, viewConfig, 0, 0, false, true);
		}
		HFDDMeasurement m = new HFDDMeasurement(perspectiveDescription, viewConfig, 
				vertexLogPropNonEmpty.left(), vertexLogPropNonEmpty.right() ,
				emdSol.get().getEMD(), EMDSolAnalyzer.flow2EmptyCost(emdSol.get()), vertexLogPropNonEmpty.allZero());

		// Save
		if (save) {
			v.getVertexInfo().addMeasurement(m);
		}
		return m;
	}

	@Override
	public HFDDMeasurementEMDSol measureVertexDetails(HFDDVertex v, BiComparisonDataSource<T> biCompDS,
			boolean save) {
		
		////////////////////
		// Outer Context
		////////////////////
		List<HFDDLogTransformStep<? extends T>> vertexLogFilterStack = new LinkedList<>();
		HFDDLogTransformStep<T> lastStep = new HFDDLogTransformStep<T>(biCompDS, FilterTag.IN);
		vertexLogFilterStack.add(0, lastStep);
		if (logTransOuterCont.isPresent()) {
			// Apply all transformers
			for (HFDDVertexLogTransformerOuterContext<T> transOutC : logTransOuterCont.get()) {
				try {
					lastStep = transOutC.getDataSourceOuterContext(v, lastStep.biCompDS());
					// Push to filter stack
					vertexLogFilterStack.add(0, lastStep);

				} catch (SLDSTransformerBuildingException e1) {
					logger.error("Error during HFDDVertex outer context transformation");
					e1.printStackTrace();
					return new HFDDMeasurementEMDSol(perspectiveDescription, null, 0, 0, Optional.empty(), false, true);
				}
			}
		}

		////////////////////
		// Inner Context: Prepare log 
		////////////////////
		BiComparisonDataSource<T> biCompDSVertex;
		try {
			biCompDSVertex = v.getVertexInfo().createVertexLog(lastStep.biCompDS());
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			return new HFDDMeasurementEMDSol(perspectiveDescription, null, 0, 0, Optional.empty(), false, true);
		}
		vertexLogFilterStack.add(0, new HFDDLogTransformStep<T>(biCompDSVertex, FilterTag.VERTEX));
		
		////////////////////
		// View Config
		////////////////////
		ViewConfig viewConfig = this.viewConfigProvider.provideViewConfig(v, vertexLogFilterStack);

		////////////////////
		// EMD
		////////////////////
		Optional<EMDSolContainer> emdSol;
		try {
			emdSol = BiDSDiffMeasure.measureEMD(biCompDSVertex, viewConfig, perspectiveDescription);
		} catch (ViewDataException e) {
			e.printStackTrace();
			return new HFDDMeasurementEMDSol(perspectiveDescription, viewConfig, 0, 0, Optional.empty(), false, true);
		}

		if (emdSol.isEmpty()) {
			logger.error("Error during HFDDVertex measurement! Could not realize EMD!");
			return new HFDDMeasurementEMDSol(perspectiveDescription, viewConfig, 0, 0, Optional.empty(), false, true);
		}

		// Probability mass non-empty traces
		ProbMassNonEmptyTrace vertexLogPropNonEmpty;
		try {
			vertexLogPropNonEmpty = viewConfig.getLangTransformer().probabilityMassNonEmptyTraces(
					biCompDSVertex.getDataSourceLeft().getVariantLog(),
					biCompDSVertex.getDataSourceRight().getVariantLog());
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			return new HFDDMeasurementEMDSol(perspectiveDescription, viewConfig, 0, 0, Optional.empty(), false, true);
		}

		HFDDMeasurementEMDSol m = new HFDDMeasurementEMDSol(perspectiveDescription, viewConfig, 
				vertexLogPropNonEmpty.left(), vertexLogPropNonEmpty.right(), Optional.of(emdSol.get()), 
				true, vertexLogPropNonEmpty.allZero());
		if (save) {
			v.getVertexInfo().addMeasurement(m);
		}
		return m;
	}

	@Override
	public PerspectiveDescriptor getMeasurementDescription() {
		return perspectiveDescription;
	}
}

