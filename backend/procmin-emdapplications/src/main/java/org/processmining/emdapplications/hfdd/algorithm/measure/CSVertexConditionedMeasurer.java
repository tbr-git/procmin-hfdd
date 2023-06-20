package org.processmining.emdapplications.hfdd.algorithm.measure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.VertexConditioningLogTransformer;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigConditionProvider;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigProvider;
import org.processmining.emdapplications.hfdd.data.csgraph.CSMeasurementTypes;
import org.processmining.emdapplications.hfdd.data.csgraph.PerspectiveCSConditioned;
import org.processmining.emdapplications.hfdd.data.csgraph.PerspectiveCSResidual;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraph;

public class CSVertexConditionedMeasurer<T extends CVariant> extends CSVertexMeasurerBase<T> {

	private final ArrayList<Set<VertexCondition>> conditionSets;

	private final static PerspectiveDescriptor pDesc = new PerspectiveCSConditioned();

	/**
	 * Activity distance pair that will be used to transform the variant log into
	 * an EMD problem.
	 */
	private final DescriptorDistancePair trDescDist;
	
	public CSVertexConditionedMeasurer(CSGraph csGraph, 
			DescriptorDistancePair trDescDist,
			ArrayList<Set<VertexCondition>> conditionSets) {
		super(csGraph, false);
		this.trDescDist = trDescDist;
		this.conditionSets = conditionSets;
	}
	
	@Override
	public PerspectiveDescriptor getMeasurementDescription() {
		return pDesc;
	}

	@Override
	protected HFDDVertexMeasurerImpl<T> getMeasurer(BiComparisonDataSource<? extends T> biCompDS) {
		
		////////////////////////////////////////
		// Setup Measurer
		////////////////////////////////////////
		// External context log transformer
		List<HFDDVertexLogTransformerOuterContext<T>> transformersOuterContext = new LinkedList<>();
		// Conditioning
		HFDDVertexLogTransformerOuterContext<T> conditioningTransformer = 
				new VertexConditioningLogTransformer<T>(Optional.of(conditionSets));
		transformersOuterContext.add(conditioningTransformer);
		
		// View provider
		ViewConfigProvider<T> viewProvider = new ViewConfigConditionProvider<T>(trDescDist);
		
		HFDDVertexMeasurerImpl<T> measurer = new HFDDVertexMeasurerImpl<>(
				new PerspectiveCSResidual(), 
				viewProvider,
				Optional.of(transformersOuterContext));
		return measurer;
	}

	@Override
	protected CSMeasurementTypes getMeasurementType() {
		return CSMeasurementTypes.CONDITIONED;
	}
}

