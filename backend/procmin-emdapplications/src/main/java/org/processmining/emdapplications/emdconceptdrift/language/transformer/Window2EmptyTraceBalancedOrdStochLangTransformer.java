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

public class Window2EmptyTraceBalancedOrdStochLangTransformer implements Window2OrderedStochLangTransformer {
	private final static Logger logger = LogManager.getLogger( Window2EmptyTraceBalancedOrdStochLangTransformer.class );
	
	public class Builder {
		private double preScaleLeft;
	
		private double preScaleRight;
		
		private ScalingContext scalingContext;
		
		public Builder() {
			preScaleLeft = 1;
			preScaleRight = 1;
			
		}

		public Builder(Window2EmptyTraceBalancedOrdStochLangTransformer t) {
			preScaleLeft = t.getPreScaleLeft();
			preScaleRight = t.getPreScaleRight();
			
		}
		
		public Builder setPreScaleLeft(double preScaleLeft) {
			this.preScaleLeft = preScaleLeft;
			return this;
		}

		public Builder setPreScaleRight(double preScaleRight) {
			this.preScaleRight = preScaleRight;
			return this;
		}
		
		public Builder setScalingContext(ScalingContext scalingContext) {
			this.scalingContext = scalingContext;
			return this;
		}
		
		public Window2EmptyTraceBalancedOrdStochLangTransformer build() {
			return new Window2EmptyTraceBalancedOrdStochLangTransformer(preScaleLeft, preScaleRight, scalingContext); 
		}
		
	}
	
	private double preScaleLeft;
	
	private double preScaleRight;
	
	private ScalingContext scalingContext;
	
	public Window2EmptyTraceBalancedOrdStochLangTransformer(double preScaleLeft, double preScaleRight, ScalingContext scalingContext) {
		this.preScaleLeft = preScaleLeft;
		this.preScaleRight = preScaleRight;
		this.scalingContext = scalingContext;
	}

	public Window2EmptyTraceBalancedOrdStochLangTransformer(Window2EmptyTraceBalancedOrdStochLangTransformer transformer) {
		this.preScaleLeft = transformer.getPreScaleLeft();
		this.preScaleRight = transformer.getPreScaleRight();
	}
	

	@Override
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(Iterator<XTrace> itTracesLeft,
			Iterator<XTrace> itTracesRight, AbstractTraceDescriptorFactory traceDescFac) {
		TObjectFloatMap<TraceDescriptor> sLogL = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		double totalWeightL = 0;
		int nbrTracesL = 0;
		while(itTracesLeft.hasNext()) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(itTracesLeft.next());

			sLogL.adjustOrPutValue(traceDesc, (float) preScaleLeft, (float) preScaleLeft);
			totalWeightL += preScaleLeft;
			nbrTracesL++;
		}

		TObjectFloatMap<TraceDescriptor> sLogR = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		double totalWeightR = 0;
		int nbrTracesR = 0;
		while(itTracesRight.hasNext()) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(itTracesRight.next());

			sLogR.adjustOrPutValue(traceDesc, (float) preScaleRight, (float) preScaleRight);
			totalWeightR += preScaleRight;
			nbrTracesR++;
		}
		
		if(Double.compare(totalWeightL,  totalWeightR) < 0) {
			float diff = (float) (totalWeightR - totalWeightL);
			sLogL.adjustOrPutValue(traceDescFac.getEmptyTrace(), diff, diff);
			totalWeightL += diff;
		}
		else if(Double.compare(totalWeightL,  totalWeightR) > 0) {
			float diff = (float) (totalWeightL - totalWeightR);
			sLogR.adjustOrPutValue(traceDescFac.getEmptyTrace(), diff, diff);
			totalWeightR += diff;
		}
		
		
		OrderedStochasticLanguage languageL = new OrderedFreqBasedStochLanguageImpl(
				sLogL, totalWeightL, nbrTracesL, traceDescFac);

		OrderedStochasticLanguage languageR = new OrderedFreqBasedStochLanguageImpl(
				sLogR, totalWeightR, nbrTracesR, traceDescFac);
		
		return Pair.of(languageL, languageR);
	}
	

	
	public double getPreScaleLeft() {
		return preScaleLeft;
	}

	public double getPreScaleRight() {
		return preScaleRight;
	}
	
	public ScalingContext getScalingContext() {
		return scalingContext;
	}
	
	public void setPreScaleLeft(double preScaleLeft) {
		this.preScaleLeft = preScaleLeft;
	}
	
	public void setPreScaleRight(double preScaleRight) {
		this.preScaleRight = preScaleRight;
	}

	@Override
	public String getShortDescription() {
		return "Balanced by empty traces (" + scalingContext + ")";
	}

	@Override
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(
			CVariantLog<? extends CVariant> logLeft, CVariantLog<? extends CVariant> logRight,
			AbstractTraceDescriptorFactory traceDescFactory) {

		TObjectFloatMap<TraceDescriptor> sLogL = new TObjectFloatCustomHashMap<>(traceDescFactory.getHashingStrat(), 100, 0.5f, 0);
		double totalWeightL = 0;
		int nbrTracesL = 0;
		for(CVariant variant : logLeft) {
			Pair<TraceDescriptor, Integer> traceDescNSupport = traceDescFactory.getTraceDescriptor(variant, logLeft);

			float addedWeight = (float) (((double) traceDescNSupport.getRight()) / preScaleLeft);
			sLogL.adjustOrPutValue(traceDescNSupport.getLeft(), addedWeight, addedWeight);

			totalWeightL += addedWeight;
			nbrTracesL += traceDescNSupport.getRight();
		}

		TObjectFloatMap<TraceDescriptor> sLogR = new TObjectFloatCustomHashMap<>(traceDescFactory.getHashingStrat(), 100, 0.5f, 0);
		double totalWeightR = 0;
		int nbrTracesR = 0;
		for(CVariant variant : logRight) {
			Pair<TraceDescriptor, Integer> traceDescNSupport = traceDescFactory.getTraceDescriptor(variant, logRight);

			float addedWeight = (float) (((double) traceDescNSupport.getRight()) / preScaleRight);
			sLogL.adjustOrPutValue(traceDescNSupport.getLeft(), addedWeight, addedWeight);

			totalWeightR += addedWeight;
			nbrTracesR += traceDescNSupport.getRight();
		}
		
		if(Double.compare(totalWeightL,  totalWeightR) < 0) {
			float diff = (float) (totalWeightR - totalWeightL);
			sLogL.adjustOrPutValue(traceDescFactory.getEmptyCVariant(logLeft), diff, diff);
			totalWeightL += diff;
		}
		else if(Double.compare(totalWeightL,  totalWeightR) > 0) {
			float diff = (float) (totalWeightL - totalWeightR);
			sLogR.adjustOrPutValue(traceDescFactory.getEmptyCVariant(logRight), diff, diff);
			totalWeightR += diff;
		}
		
		
		OrderedStochasticLanguage languageL = new OrderedFreqBasedStochLanguageImpl(
				sLogL, totalWeightL, nbrTracesL, traceDescFactory);

		OrderedStochasticLanguage languageR = new OrderedFreqBasedStochLanguageImpl(
				sLogR, totalWeightR, nbrTracesR, traceDescFactory);
		
		return Pair.of(languageL, languageR);
	}
	
	@Override
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft,
			CVariantLog<? extends CVariant> tracesRight) {
		double probNonEmptyLeft = ((double) tracesLeft.sizeLog()) / this.preScaleLeft;
		double probNonEmptyRight = ((double) tracesRight.sizeLog()) / this.preScaleRight;
		
		probNonEmptyLeft = probNonEmptyLeft / Math.max(probNonEmptyLeft, probNonEmptyRight);
		probNonEmptyRight = probNonEmptyRight / Math.max(probNonEmptyLeft, probNonEmptyRight);
		// TODO Auto-generated method stub

		return new ProbMassNonEmptyTrace(probNonEmptyLeft, probNonEmptyRight, 
				((tracesLeft.sizeLog() == 0) && (tracesRight.sizeLog() == 0)));
	}
	
}
