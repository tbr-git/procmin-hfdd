package org.processmining.emdapplications.emdconceptdrift.language.transformer;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedFreqBasedStochLanguageImpl;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.custom_hash.TObjectFloatCustomHashMap;

public class ContextAwareEmptyTraceBalancedTransformer implements Window2OrderedStochLangTransformer {
	private final static Logger logger = LogManager.getLogger(ContextAwareEmptyTraceBalancedTransformer.class);

	public class Builder {
		private int contextLogSizeLeft;
	
		private int contextLogSizeRight;
		
		private ScalingContext scalingContext;
		
		public Builder() {
			contextLogSizeLeft = 1;
			contextLogSizeRight = 1;
			
		}

		public Builder(ContextAwareEmptyTraceBalancedTransformer t) {
			contextLogSizeLeft = t.getContextLogSizeLeft();
			contextLogSizeRight = t.getContextLogSizeRight();
			scalingContext = t.getScalingContext();
		}
		
		public Builder setContextLogSizeLeft(int contextLogSizeLeft) {
			this.contextLogSizeLeft = contextLogSizeLeft;
			return this;
		}

		public Builder setContextLogSizeRight(int contextLogSizeRight) {
			this.contextLogSizeLeft = contextLogSizeRight;
			return this;
		}
		
		public Builder setScalingContext(ScalingContext scalingContext) {
			this.scalingContext = scalingContext;
			return this;
		}
		
		public ContextAwareEmptyTraceBalancedTransformer build() {
			return new ContextAwareEmptyTraceBalancedTransformer(contextLogSizeLeft, contextLogSizeRight, scalingContext); 
		}
		
	}

	private int contextLogSizeLeft;

	private int contextLogSizeRight;
	
	private ScalingContext scalingContext;
		
	public ContextAwareEmptyTraceBalancedTransformer(int contextLogSizeLeft, int contextLogSizeRight, ScalingContext scalingContext) {
		this.contextLogSizeLeft = contextLogSizeLeft;
		this.contextLogSizeRight = contextLogSizeRight;
		this.scalingContext = scalingContext;
	}

	public ContextAwareEmptyTraceBalancedTransformer(ContextAwareEmptyTraceBalancedTransformer transformer) {
		this.contextLogSizeLeft = transformer.getContextLogSizeLeft();
		this.contextLogSizeRight = transformer.getContextLogSizeRight();
		this.scalingContext = transformer.getScalingContext();
	}
	

	@Override
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(Iterator<XTrace> itTracesLeft,
			Iterator<XTrace> itTracesRight, AbstractTraceDescriptorFactory traceDescFac) {
		TObjectFloatMap<TraceDescriptor> sLogL = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		int nbrTracesL = 0;
		while(itTracesLeft.hasNext()) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(itTracesLeft.next());
			sLogL.adjustOrPutValue(traceDesc, 1.0f, 1.0f);
			nbrTracesL++;
		}
		if(contextLogSizeLeft - nbrTracesL > 0 ) {
			sLogL.put(traceDescFac.getEmptyTrace(), contextLogSizeLeft - nbrTracesL);
		}

		TObjectFloatMap<TraceDescriptor> sLogR = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		int nbrTracesR = 0;
		while(itTracesRight.hasNext()) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(itTracesRight.next());
			sLogR.adjustOrPutValue(traceDesc, 1.0f, 1.0f);
			nbrTracesR++;
		}
		if(contextLogSizeRight - nbrTracesR > 0 ) {
			sLogR.put(traceDescFac.getEmptyTrace(), contextLogSizeRight - nbrTracesR);
		}
		
		logger.trace("{} transforms window with {}(+{} empty) traces left and {}(+{} empty) traces right", this.toString(), 
				nbrTracesL, contextLogSizeLeft - nbrTracesL, nbrTracesR, contextLogSizeRight - nbrTracesR);
		
		OrderedStochasticLanguage languageL = new OrderedFreqBasedStochLanguageImpl(
				sLogL, contextLogSizeLeft, contextLogSizeLeft, traceDescFac);

		OrderedStochasticLanguage languageR = new OrderedFreqBasedStochLanguageImpl(
				sLogR, contextLogSizeRight, contextLogSizeRight, traceDescFac);

		if(languageL.getNumberOfTraceVariants() > 10 || languageR.getNumberOfTraceVariants() > 10) {
			logger.trace(() -> "Created stochastic languages: .... (Too big to display properly)");
		}
		else {
			logger.trace(() -> String.format("Created stochastic languages: %s \n____________________\n%s", languageL.toString(), languageR.toString()));
		}
		
		return Pair.of(languageL, languageR);
	}

	@Override
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(
			CVariantLog<? extends CVariant> logLeft, CVariantLog<? extends CVariant> logRight,
			AbstractTraceDescriptorFactory traceDescFactory) {

		TObjectFloatMap<TraceDescriptor> sLogL = new TObjectFloatCustomHashMap<>(traceDescFactory.getHashingStrat(), 100, 0.5f, 0);
		int nbrTracesL = 0;

		for(CVariant variant : logLeft) {
			Pair<TraceDescriptor, Integer> traceDescNSupport = traceDescFactory.getTraceDescriptor(variant, logLeft);

			int addedSupport = traceDescNSupport.getRight();
			sLogL.adjustOrPutValue(traceDescNSupport.getLeft(), addedSupport, addedSupport);

			nbrTracesL += addedSupport;
		}

		if(contextLogSizeLeft - nbrTracesL > 0 ) {
			sLogL.put(traceDescFactory.getEmptyCVariant(logLeft), contextLogSizeLeft - nbrTracesL);
		}

		TObjectFloatMap<TraceDescriptor> sLogR = new TObjectFloatCustomHashMap<>(traceDescFactory.getHashingStrat(), 100, 0.5f, 0);
		int nbrTracesR = 0;
		for(CVariant variant : logRight) {
			Pair<TraceDescriptor, Integer> traceDescNSupport = traceDescFactory.getTraceDescriptor(variant, logRight);

			int addedSupport = traceDescNSupport.getRight();
			sLogR.adjustOrPutValue(traceDescNSupport.getLeft(), addedSupport, addedSupport);

			nbrTracesR += addedSupport;
		}
		if(contextLogSizeRight - nbrTracesR > 0 ) {
			sLogR.put(traceDescFactory.getEmptyCVariant(logRight), contextLogSizeRight - nbrTracesR);
		}
		
		logger.trace("{} transforms window with {}(+{} empty) traces left and {}(+{} empty) traces right", this.toString(), 
				nbrTracesL, contextLogSizeLeft - nbrTracesL, nbrTracesR, contextLogSizeRight - nbrTracesR);
		
		OrderedStochasticLanguage languageL = new OrderedFreqBasedStochLanguageImpl(
				sLogL, contextLogSizeLeft, contextLogSizeLeft, traceDescFactory);

		OrderedStochasticLanguage languageR = new OrderedFreqBasedStochLanguageImpl(
				sLogR, contextLogSizeRight, contextLogSizeRight, traceDescFactory);

		if(languageL.getNumberOfTraceVariants() > 10 || languageR.getNumberOfTraceVariants() > 10) {
			logger.trace(() -> "Created stochastic languages: .... (Too big to display properly)");
		}
		else {
			logger.trace(() -> String.format("Created stochastic languages: %s \n____________________\n%s", languageL.toString(), languageR.toString()));
		}
		
		return Pair.of(languageL, languageR);
	}
	
	@Override
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft,
			CVariantLog<? extends CVariant> tracesRight) {
		double probNonEmptyLeft = ((double) tracesLeft.sizeLog()) / this.contextLogSizeLeft;
		double probNonEmptyRight = ((double) tracesRight.sizeLog()) / this.contextLogSizeRight;
		// TODO Auto-generated method stub
		return new ProbMassNonEmptyTrace(probNonEmptyLeft, probNonEmptyRight, 
				((tracesLeft.sizeLog() == 0) && (tracesRight.sizeLog() == 0)));
	}
	
	public int getContextLogSizeLeft() {
		return contextLogSizeLeft;
	}

	public int getContextLogSizeRight() {
		return contextLogSizeRight;
	}
	
	public ScalingContext getScalingContext() {
		return scalingContext;
	}
	
	public void setContextLogSizeLeft(int contextLogSizeLeft) {
		this.contextLogSizeLeft = contextLogSizeLeft;
	}
	
	public void setContextLogSizeRight(int contextLogSizeRight) {
		this.contextLogSizeRight = contextLogSizeRight;
	}

	@Override
	public String getShortDescription() {
		return "Balanced by empty traces (" + scalingContext + ")";
	}

	@Override
	public String toString() {
		return "ContextAwareEmptyTraceBalancedTransformer in mode " + scalingContext.toString() + 
		" with size left/right " + contextLogSizeLeft + "/" + contextLogSizeRight;
	}



	
	
}
