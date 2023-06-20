package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.LevenshteinStateful;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TimeBinnedWLVSWithEdit;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceDescriptorFactory;

public class DescriptorDistancePairFactory {
	
	public static Collection<DescriptorDistancePair> availableSubViewPairsFor (
			DescriptorDistancePair pair) {
		
		//TODO We might add pair twice
		List<DescriptorDistancePair> l = new LinkedList<>();
		if(pair.getDistance() instanceof TimeBinnedWLVSWithEdit) {
			l.add(new DescriptorDistancePair(new LevenshteinStateful(), new BasicTraceDescriptorFactory(XLogInfoImpl.NAME_CLASSIFIER)));
		}
		return l;
	}
	

}
