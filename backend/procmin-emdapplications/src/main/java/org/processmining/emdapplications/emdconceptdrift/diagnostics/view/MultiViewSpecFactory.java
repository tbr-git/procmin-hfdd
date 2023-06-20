package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDistEditDiagnose;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ContextAwareEmptyTraceBalancedTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ScalingContext;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2SimpleNormOrdStochLangTransformer;

public class MultiViewSpecFactory {
	private final static Logger logger = LogManager.getLogger( MultiViewSpecFactory.class.getName());

	public static MultiViewConfig createConfigureMultiViewHierarchy(AbstractTraceDescriptorFactory langFac, TraceDistEditDiagnose trDescDist, 
			int logSizeL, int logSizeR, XLog xlog) { 
		MultiViewConfig viewConfig = createMultiViewHierarchy(langFac, trDescDist, logSizeL, logSizeR);
		configureComponentsUsed(viewConfig, xlog, false);
		return viewConfig;
	}
	
	public static MultiViewConfig createMultiViewHierarchy(AbstractTraceDescriptorFactory langFac, TraceDistEditDiagnose trDescDist, int logSizeL, int logSizeR) {
//		Window2OrderedStochLangTransformer tFreqBalancedModel = new Window2EmptyTraceBalancedOrdStochLangTransformer(1.0 / logSizeL, 1.0 / logSizeR, ScalingContext.MODEL);
//		Window2OrderedStochLangTransformer tFreqBalancedGlobal = new Window2EmptyTraceBalancedOrdStochLangTransformer(1.0 / logSizeL, 1.0 / logSizeR, ScalingContext.GLOABAL);
		ContextAwareEmptyTraceBalancedTransformer tFreqBalancedModel = new ContextAwareEmptyTraceBalancedTransformer(logSizeL, logSizeR, ScalingContext.MODEL);
		ContextAwareEmptyTraceBalancedTransformer tFreqBalancedGlobal = new ContextAwareEmptyTraceBalancedTransformer(logSizeL, logSizeR, ScalingContext.GLOBAL);
		Window2OrderedStochLangTransformer tSimpNorm = new Window2SimpleNormOrdStochLangTransformer();
		
		DescriptorDetailedDistancePair pTop = new DescriptorDetailedDistancePair(trDescDist, langFac);

		DetailedViewConfig topViewBalancedGlobal = new DetailedViewConfig(tFreqBalancedGlobal, pTop, 
				new ViewIdentifier(pTop.getShortDescription() + " - " + tFreqBalancedGlobal.getShortDescription()));
		DetailedViewConfig topViewBalancedModel = new DetailedViewConfig(tFreqBalancedModel, pTop, 
				new ViewIdentifier(pTop.getShortDescription() + " - " + tFreqBalancedModel.getShortDescription()));
		DetailedViewConfig topViewSimpNorm = new DetailedViewConfig(tSimpNorm, pTop,
				new ViewIdentifier(pTop.getShortDescription() + " - " + tSimpNorm.getShortDescription()));
		
		MultiViewConfig viewConfig = new MultiViewConfig();
		viewConfig.setTopLevelView(topViewBalancedGlobal);
		viewConfig.addSubView(topViewBalancedModel);
		viewConfig.addSubView(topViewSimpNorm);
		
		for(DescriptorDistancePair p : DescriptorDistancePairFactory.availableSubViewPairsFor(pTop)) {
			viewConfig.addSubView(new ViewConfig(tFreqBalancedGlobal, p, 
					new ViewIdentifier(p.getShortDescription() + " - " + tFreqBalancedGlobal.getShortDescription())));
			viewConfig.addSubView(new ViewConfig(tFreqBalancedModel, p, 
					new ViewIdentifier(p.getShortDescription() + " - " + tFreqBalancedModel.getShortDescription())));
			viewConfig.addSubView(new ViewConfig(tSimpNorm, p,
					new ViewIdentifier(p.getShortDescription() + " - " + tSimpNorm.getShortDescription())));
		}
		
		return viewConfig;

	}
	
	public static void configureComponentsUsed(MultiViewConfig viewConfig, XLog xlog, boolean overwrite) {
		Iterator<ViewConfig> itView = viewConfig.getViewIterator();
		while(itView.hasNext()) {
			ViewConfig v = itView.next();
			logger.debug("Configuring View: \n " + v.toString());
			v.getDescDistPair().getDescriptorFactory().init(xlog, overwrite);
		}
			
	}
}
