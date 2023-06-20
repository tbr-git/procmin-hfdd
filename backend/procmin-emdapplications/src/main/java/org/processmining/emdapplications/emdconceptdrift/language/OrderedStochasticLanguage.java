package org.processmining.emdapplications.emdconceptdrift.language;

import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public interface OrderedStochasticLanguage extends StochasticLanguage {
	
	public TraceDescriptor get(int index);
	
}
