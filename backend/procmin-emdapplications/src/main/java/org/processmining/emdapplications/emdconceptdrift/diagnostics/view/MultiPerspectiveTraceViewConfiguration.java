package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MultiPerspectiveTraceViewConfiguration {
	
	private final DescriptorDetailedDistancePair governingView;
	
	private List<DescriptorDistancePair> subViews;
	
	public MultiPerspectiveTraceViewConfiguration(DescriptorDetailedDistancePair governingView) {
		this.governingView = governingView;
		subViews = new LinkedList<>();
	}
	
	public void addSubView(DescriptorDistancePair p) {
		subViews.add(p);
	}
	

	public DescriptorDetailedDistancePair getGoverningDescDistPair() {
		return this.governingView;
	}

	public Collection<DescriptorDistancePair> getFocusDescDistPairs() {
		return subViews;
	}
	
}
