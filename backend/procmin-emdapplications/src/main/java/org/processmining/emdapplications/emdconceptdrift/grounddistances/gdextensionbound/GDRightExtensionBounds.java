package org.processmining.emdapplications.emdconceptdrift.grounddistances.gdextensionbound;

import org.apache.commons.lang3.tuple.Triple;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public interface GDRightExtensionBounds extends TraceDescDistCalculator {
	
	/**
	 * Compute the trace descriptor distance + lower and upper bounds on extension of the right discriptor.
	 * @param tl
	 * @param tr
	 * @return (distance between traces without extension, 
	 * 	lower bound on distance for all possible extension of the right trace,
	 * 	upper bound on distance for all possible extension of the right trace)
	 */
	public Triple<Double, Double, Double> getRightExtBoundedDistance(TraceDescriptor tl, TraceDescriptor tr);
	
	/**
	 * Configure which bound {@link TraceDescDistCalculator#get_distance(TraceDescriptor, TraceDescriptor)} 
	 * should return.
	 * 
	 * @param boundType Bound type;
	 */
	public void configureReturnedDistance(DistanceBoundType boundType);
	
}
