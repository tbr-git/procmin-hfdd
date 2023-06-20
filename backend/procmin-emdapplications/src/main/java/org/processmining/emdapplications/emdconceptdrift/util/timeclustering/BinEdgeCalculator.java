package org.processmining.emdapplications.emdconceptdrift.util.timeclustering;

import java.util.List;

public interface BinEdgeCalculator {
	
	double[] calculateBinEdges(List<Double> lTimes);

}
