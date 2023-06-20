package org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewIdentifier;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDistFreeTraceDelInsWrapper;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ContextAwareEmptyTraceBalancedTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ScalingContext;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2SimpleNormOrdStochLangTransformer;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexCondition;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexConditionType;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.FilterTag;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDLogTransformStep;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public class ViewConfigAbstFreeExtConditionedProvider<T extends CVariant> implements ViewConfigProvider<T> {
	private final static Logger logger = LogManager.getLogger( ViewConfigAbstFreeExtConditionedProvider.class );

	private final DescriptorDistancePair descDistPair;
	
	private final Optional<ArrayList<Set<VertexCondition>>> aggDataCBase;

	public ViewConfigAbstFreeExtConditionedProvider(
			Optional<ArrayList<Set<VertexCondition>>> aggDataCBase, 
			DescriptorDistancePair descDistPair) {
		
		this.descDistPair = descDistPair;
		this.aggDataCBase = aggDataCBase;
		
	}

	@Override
	public ViewConfig provideViewConfig(HFDDVertex v, List<HFDDLogTransformStep<? extends T>> vertexLogFilterStack) {
		////////////////////
		// Find Condition Step 
		////////////////////
		Optional<HFDDLogTransformStep<? extends T>> filterStepCond = vertexLogFilterStack.stream()
				.filter(s -> ((s.filterTag() == FilterTag.CONDITION) 
						|| (s.filterTag() == FilterTag.CONDITION_NONE) 
						|| (s.filterTag() == FilterTag.IN)))
				.findFirst();
		if (filterStepCond.isEmpty()) {
			throw new IllegalArgumentException("The filter stack does not contain any condition filter step nor a base input");
		}

		// Create language transformer
		Window2OrderedStochLangTransformer langTransformer;
		try {
			// Don't take the base log but the transformed one!
			langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
					filterStepCond.get().biCompDS().getDataSourceLeft().getVariantLog().sizeLog(), 
					filterStepCond.get().biCompDS().getDataSourceRight().getVariantLog().sizeLog(), 
					ScalingContext.GLOBAL);
		} catch (SLDSTransformationError e) {
			logger.error("Failed to instantiate the measurement executor because the size of the context log could not be determined. "
					+ "Fallback to standard normalization!");
			langTransformer = new Window2SimpleNormOrdStochLangTransformer();
		}
		
		// If for this vertex, we allow for free trace delete / insert,
		// create a local wrapper around the trace distance, that enables that for all trace descriptors. 
		// -> We can enable this even if the context is not directly visible from the trace descriptors.
		// (For example, data-based subprocess dependencies)
		
		DescriptorDistancePair localDescDistPair = this.descDistPair;
		if (aggDataCBase.isPresent() && aggDataCBase.get().get(v.getId()) != null) {
			Set<VertexCondition> s = aggDataCBase.get().get(v.getId());
			
			boolean freeLeftDelete = s.stream()
					.anyMatch(vCond -> vCond.type() == VertexConditionType.CONDFREQIGNORELEFT);
			boolean freeRightInsert = s.stream()
					.anyMatch(vCond -> vCond.type() == VertexConditionType.CONDFREQIGNORERIGHT);
			
			if (freeLeftDelete || freeRightInsert) {
				TraceDistFreeTraceDelInsWrapper vertexLocalDistWrapper = 
						new TraceDistFreeTraceDelInsWrapper(this.descDistPair.getDistance());
				vertexLocalDistWrapper.setFreeLeftTraceDelete(freeLeftDelete);
				vertexLocalDistWrapper.setFreeRightTraceInsert(freeRightInsert);
				localDescDistPair = new DescriptorDistancePair(vertexLocalDistWrapper, 
						this.descDistPair.getDescriptorFactory());
			}
		}

		
		ViewConfig viewConfig = new ViewConfig(langTransformer, localDescDistPair,
				new ViewIdentifier(this.descDistPair.getShortDescription() 
						+ " - " + langTransformer.getShortDescription()));
		return viewConfig;
	}

}
