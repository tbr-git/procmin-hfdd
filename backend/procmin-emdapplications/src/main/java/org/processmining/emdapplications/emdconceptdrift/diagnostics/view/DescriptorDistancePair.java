package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

public class DescriptorDistancePair {
	private final TraceDescDistCalculator dist;
	
	private final AbstractTraceDescriptorFactory descFac;

	public DescriptorDistancePair(TraceDescDistCalculator dist, AbstractTraceDescriptorFactory descFac) {
		super();
		this.dist = dist;
		this.descFac = descFac;
	}
	
	public TraceDescDistCalculator getDistance() {
		return dist;
	}
	
	public AbstractTraceDescriptorFactory getDescriptorFactory() {
		return descFac;
	}
	
	public String getShortDescription() {
		return descFac.getShortDescription() + " - " + dist.getShortDescription();
		
	}

}
