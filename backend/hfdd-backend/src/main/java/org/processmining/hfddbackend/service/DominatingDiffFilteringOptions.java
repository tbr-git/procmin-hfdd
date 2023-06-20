package org.processmining.hfddbackend.service;

/**
 * Record that specifies the parameter for determining the dominating vertices in a HFDD comparison iteration.
 * 
 * @param metricThreshold Threshold of the metric values if the itemset is considered (has to exceed the threshold)
 * @param metricSurpriseThreshold A superset is surprising even though children are interesting and uninteresting 
 * 	if is metric exceeds the largest child score by this margin
 * @param backwardDominationThreshold A superset dominates its subset if its metric exceeds its interesting subset's metric 
 * @author brockhoff
 *
 */
public record DominatingDiffFilteringOptions(double metricThreshold, 
		double metricSurpriseThreshold, double backwardDominationThreshold) {

	public DominatingDiffFilteringOptions(double metricThreshold) {
		this(metricThreshold, 0.95, 0.95);
	}

	public DominatingDiffFilteringOptions() {
		this(0.2, 0.95, 0.95);
	}

}