package org.processmining.emdapplications.emdconceptdrift.language;

import org.json.JSONObject;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public interface StochasticLanguage {

	public int getNumberOfTraceVariants();
	
	public int getAbsoluteNbrOfTraces();
	
	public StochasticLanguageIterator iterator(); 

	public JSONObject toJson();
	
	public double getTotalWeight();
	
	public double getProbability(TraceDescriptor traceDesc);
	
	public boolean contains(TraceDescriptor traceDescriptor);
}
