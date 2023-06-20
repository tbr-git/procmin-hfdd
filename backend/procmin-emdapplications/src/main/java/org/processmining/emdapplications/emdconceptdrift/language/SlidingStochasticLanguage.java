package org.processmining.emdapplications.emdconceptdrift.language;

import java.util.Iterator;

import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

import gnu.trove.iterator.TObjectLongIterator;

// Cannot extends Sliding Window because we need a StochasticLanguageIterator 
// that handles TraceDescriptors instead of String[] only
public interface SlidingStochasticLanguage extends StochasticLanguage {
	
	/**
	 * 
	 * @param traces
	 */
	public void slideOut(XTrace trace);
	
	public default void slideOut(Iterable<XTrace> traces) {
		for(XTrace trace : traces) {
			slideOut(trace);
		}
	}
	
	public void slideIn(XTrace trace);
	
	public default void slideIn(Iterator<XTrace> traces) {
		while(traces.hasNext()) {
			slideIn(traces.next());
		}
	}
	
}
