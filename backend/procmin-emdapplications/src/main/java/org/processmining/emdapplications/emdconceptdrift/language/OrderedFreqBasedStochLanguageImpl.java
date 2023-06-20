package org.processmining.emdapplications.emdconceptdrift.language;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
import org.processmining.framework.plugin.ProMCanceller;

import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.custom_hash.TObjectFloatCustomHashMap;

public class OrderedFreqBasedStochLanguageImpl extends StochasticLanguageImpl implements OrderedStochasticLanguage {
	/**
	 */
	protected final TraceDescriptor[] variantsOrder;
	
	public OrderedFreqBasedStochLanguageImpl(TObjectFloatMap<TraceDescriptor> sLog, double totalWeight, int aboluteNbrTraces, AbstractTraceDescriptorFactory traceDescFac) {
		super(sLog, totalWeight, aboluteNbrTraces, traceDescFac);
		variantsOrder = new TraceDescriptor[sLog.size()];
		TObjectFloatIterator<TraceDescriptor> it = sLog.iterator();
		int i = 0;
		while(it.hasNext()) {
			it.advance();
			variantsOrder[i] = it.key();
			i++;
		}
	}
	
	public static OrderedStochasticLanguage convert(Iterator<XTrace> log, ProMCanceller canceller, 
			AbstractTraceDescriptorFactory traceDescFac) {
		TObjectFloatMap<TraceDescriptor> sLog = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		int nbrTraces = 0;
		while(log.hasNext()) {
			TraceDescriptor traceDesc = traceDescFac.getTraceDescriptor(log.next());

			if (canceller != null && canceller.isCancelled()) {
				return null;
			}

			sLog.adjustOrPutValue(traceDesc, 1, 1);
			nbrTraces++;
		}

		OrderedStochasticLanguage language = new OrderedFreqBasedStochLanguageImpl(sLog, nbrTraces, nbrTraces, traceDescFac);
		
		return language;
	}	

	public static OrderedStochasticLanguage convert(CVariantLog<? extends CVariant> log, ProMCanceller canceller, 
			AbstractTraceDescriptorFactory traceDescFac) {
		TObjectFloatMap<TraceDescriptor> sLog = new TObjectFloatCustomHashMap<>(traceDescFac.getHashingStrat(), 10, 0.5f, 0);
		int nbrTraces = 0;
		for(CVariant variant : log) {
			Pair<TraceDescriptor, Integer> traceDescNSupport = traceDescFac.getTraceDescriptor(variant, log);

			if (canceller != null && canceller.isCancelled()) {
				return null;
			}

			sLog.adjustOrPutValue(traceDescNSupport.getLeft(), traceDescNSupport.getRight(), traceDescNSupport.getRight());
			nbrTraces += traceDescNSupport.getRight();
		}

		OrderedStochasticLanguage language = new OrderedFreqBasedStochLanguageImpl(sLog, nbrTraces, nbrTraces, traceDescFac);
		
		return language;
	}	
	
	public TraceDescriptor get(int index) {
		return variantsOrder[index];
	}
	
	@Override
	public StochasticLanguageIterator iterator() {
		double normalizationWeight = this.getTotalWeight();
		return new StochasticLanguageIterator() {
			
			private int i = -1;

			public TraceDescriptor next() {
				i++;
				return variantsOrder[i];
			}

			public boolean hasNext() {
				return i < variantsOrder.length - 1;
			}

			public double getProbability() {
				return sLog.get(variantsOrder[i]) / normalizationWeight;
			}
		};
	}
}
