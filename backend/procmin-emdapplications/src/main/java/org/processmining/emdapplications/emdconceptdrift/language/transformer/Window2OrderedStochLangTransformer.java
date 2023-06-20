package org.processmining.emdapplications.emdconceptdrift.language.transformer;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

/*
 * Implementations of this interface must define a copy constructor that takes an argument of the same type or supertype of their class. 
 * This constructor must make a deep copy of the argument so that manipulation of this object does not lead to manipulation of the returned one and vice versa.
 */
public interface Window2OrderedStochLangTransformer {
	
	public default Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(Collection<XTrace> tracesLeft, Collection<XTrace> tracesRight, 
			AbstractTraceDescriptorFactory descFactory) {
		return transformWindow(tracesLeft.iterator(), tracesRight.iterator(), descFactory);
	};
	
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(Iterator<XTrace> tracesLeft, Iterator<XTrace> tracesRight, 
			AbstractTraceDescriptorFactory descFactory);
	
	public Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> transformWindow(CVariantLog<? extends CVariant> tracesLeft, 
			CVariantLog<? extends CVariant> tracesRight, AbstractTraceDescriptorFactory descFactory);
	
	public ProbMassNonEmptyTrace probabilityMassNonEmptyTraces(CVariantLog<? extends CVariant> tracesLeft, 
			CVariantLog<? extends CVariant> tracesRight);
	
	public String getShortDescription();
	
}