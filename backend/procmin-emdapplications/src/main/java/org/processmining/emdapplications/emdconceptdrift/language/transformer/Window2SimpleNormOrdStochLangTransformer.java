package org.processmining.emdapplications.emdconceptdrift.language.transformer;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedFreqBasedStochLanguageImpl;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

public class Window2SimpleNormOrdStochLangTransformer implements Window2OrderedStochLangTransformer {
	
	public Window2SimpleNormOrdStochLangTransformer() {
		
	}

	public Window2SimpleNormOrdStochLangTransformer(Window2SimpleNormOrdStochLangTransformer t) {
	}

	@Override
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(Iterator<XTrace> itTracesLeft,
			Iterator<XTrace> itTracesRight, AbstractTraceDescriptorFactory traceDescFac) {
		OrderedStochasticLanguage langL = OrderedFreqBasedStochLanguageImpl.convert(itTracesLeft, null, traceDescFac);
		OrderedStochasticLanguage langR = OrderedFreqBasedStochLanguageImpl.convert(itTracesRight, null, traceDescFac);
		return Pair.of(langL, langR);
	}

	@Override
	public String getShortDescription() {
		return "Normalized";
	}

	@Override
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(
			CVariantLog<? extends CVariant> tracesLeft, CVariantLog<? extends CVariant> tracesRight,
			AbstractTraceDescriptorFactory descFactory) {
		OrderedStochasticLanguage langL = OrderedFreqBasedStochLanguageImpl.convert(tracesLeft, null, descFactory);
		OrderedStochasticLanguage langR = OrderedFreqBasedStochLanguageImpl.convert(tracesRight, null, descFactory);
		return Pair.of(langL, langR);
	}
	
	@Override
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft,
			CVariantLog<? extends CVariant> tracesRight) {
		boolean isEmpty = (tracesLeft.sizeLog() == 0) && (tracesRight.sizeLog() == 0);
		return new ProbMassNonEmptyTrace(isEmpty ? 0. : 1.0, isEmpty ? 0. : 1.0, isEmpty);
	}
	
}
