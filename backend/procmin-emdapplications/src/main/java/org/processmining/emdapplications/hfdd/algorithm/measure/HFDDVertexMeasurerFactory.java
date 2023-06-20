package org.processmining.emdapplications.hfdd.algorithm.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.abstraction.CCCVariantAbstImpl;
import org.processmining.emdapplications.data.variantlog.abstraction.CVariantAbst;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewIdentifier;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.AdaptiveLVS;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.LevenshteinStateful;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstTraceBuilder;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ContextAwareEmptyTraceBalancedTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ScalingContext;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDVertexLogTransformerOuterContext;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.VertexConditioningLogTransformer;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigAbstFreeExtConditionedProvider;
import org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup.ViewConfigConstantProvider;

public class HFDDVertexMeasurerFactory {
	
	public static<T extends CVariant> HFDDVertexMeasurer<T> getBaseMeasuresWithoutContext(
			BiComparisonDataSource<T> biCompDS, PerspectiveDescriptor pDesc) throws SLDSTransformationError {
		//////////////////////////////
		// View
		//////////////////////////////
		// Language transformer
		Window2OrderedStochLangTransformer langTransformer = null;
		langTransformer = new ContextAwareEmptyTraceBalancedTransformer(
				biCompDS.getDataSourceLeft().getVariantLog().sizeLog(), 
				biCompDS.getDataSourceRight().getVariantLog().sizeLog(), ScalingContext.GLOBAL);
		
		// Trace descriptor + distance
		DescriptorDistancePair desDistPair = new DescriptorDistancePair(
				new LevenshteinStateful(), 
				new BasicTraceDescriptorFactory(biCompDS.getClassifier()));
		ViewConfig viewConfig = new ViewConfig(langTransformer, desDistPair,
				new ViewIdentifier(desDistPair.getShortDescription() + " - " + langTransformer.getShortDescription()));
		
		ViewConfigConstantProvider<T> viewConfigProvider = new ViewConfigConstantProvider<>(viewConfig);
		
		HFDDVertexMeasurerImpl<T> measurer = new HFDDVertexMeasurerImpl<>(pDesc, viewConfigProvider, Optional.empty());

		return measurer;
	}
	
	public static HFDDVertexMeasurer<CCCVariantAbstImpl> defaultMeasurerAbst(BiComparisonDataSource<CCCVariantAbstImpl> biCompDS,
			PerspectiveDescriptor pDesc) throws SLDSTransformationError {

		// Create language transformer
		Window2OrderedStochLangTransformer langTransformer;
		langTransformer = new ContextAwareEmptyTraceBalancedTransformer(biCompDS.getDataSourceLeftBase().getVariantLog().sizeLog(), 
				biCompDS.getDataSourceRightBase().getVariantLog().sizeLog(), ScalingContext.GLOBAL);

		// Distance + Trace descriptor factory + transformer => view
		DescriptorDistancePair desDistPair = new DescriptorDistancePair(
				new AdaptiveLVS(), 
				new AbstTraceBuilder(biCompDS.getClassifier()));
		ViewConfig viewConfig = new ViewConfig(langTransformer, desDistPair,
				new ViewIdentifier(desDistPair.getShortDescription() + " - " + langTransformer.getShortDescription()));

		ViewConfigConstantProvider<CCCVariantAbstImpl> viewConfigProvider = new ViewConfigConstantProvider<>(viewConfig);
		
		HFDDVertexMeasurerImpl<CCCVariantAbstImpl> measurer = new HFDDVertexMeasurerImpl<>(pDesc, viewConfigProvider, Optional.empty());
		return measurer;
		
	}
	
	public static<E extends CVariantAbst> HFDDVertexMeasurer<E> createMeasurerAbstContextFreeCond(
			XEventClassifier classifier, 
			AdaptiveLVS aLVS, Optional<ArrayList<Set<VertexCondition>>> condBase, PerspectiveDescriptor pDesc)  {
		DescriptorDistancePair descDistPair = new DescriptorDistancePair(aLVS, 
		new AbstTraceBuilder(classifier));

		ViewConfigAbstFreeExtConditionedProvider<E> viewConfigProvider = 
				new ViewConfigAbstFreeExtConditionedProvider<>(condBase, descDistPair);
	
		HFDDVertexLogTransformerOuterContext<E> condTransformer = new VertexConditioningLogTransformer<E>(condBase);
		List<HFDDVertexLogTransformerOuterContext<E>> transformersOuterContex = 
				Collections.singletonList(condTransformer);

		HFDDVertexMeasurerImpl<E> measurer = new HFDDVertexMeasurerImpl<>(pDesc, 
				viewConfigProvider, 
				Optional.of(transformersOuterContex));
		
		return measurer;
	}

}
