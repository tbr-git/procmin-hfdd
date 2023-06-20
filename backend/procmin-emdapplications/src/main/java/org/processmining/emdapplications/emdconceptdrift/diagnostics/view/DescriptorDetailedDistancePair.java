package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDistEditDiagnose;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

public class DescriptorDetailedDistancePair extends DescriptorDistancePair {
	
	private final TraceDistEditDiagnose detailedDist;

	public DescriptorDetailedDistancePair(TraceDistEditDiagnose dist, AbstractTraceDescriptorFactory descFac) {
		super(dist, descFac);
		detailedDist = dist;
	}
	
	public TraceDistEditDiagnose getDetailedDistance() {
		return detailedDist; 
	}

}
