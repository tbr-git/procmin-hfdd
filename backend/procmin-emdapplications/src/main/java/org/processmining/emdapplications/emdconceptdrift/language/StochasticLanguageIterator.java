package org.processmining.emdapplications.emdconceptdrift.language;

import java.util.Iterator;

import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public interface StochasticLanguageIterator extends Iterator<TraceDescriptor>{
	public double getProbability();
}
