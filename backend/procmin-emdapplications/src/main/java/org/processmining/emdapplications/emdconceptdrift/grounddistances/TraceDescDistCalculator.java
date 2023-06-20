package org.processmining.emdapplications.emdconceptdrift.grounddistances;

import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public interface TraceDescDistCalculator {

	/**
	 * Calculate the distance between the two provided trace descriptions.
	 * 
	 * Usually int [0, 1].
	 * 0 means equality, 1 means completely different
	 * @param t1 Left descriptor
	 * @param t2 Right descriptor
	 * @return Distance usually [0, 1]
	 */
	public double get_distance(TraceDescriptor t1, TraceDescriptor t2);
	
	/**
	 * Get a short description string for this distance.
	 * @return Description string
	 */
	public String getShortDescription();
	
}
