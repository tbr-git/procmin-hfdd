package org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.frequentpattern.connector;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.prefixspan.AlgoPrefixSpan;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.prefixspan.SequentialPatterns;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDetailedDistancePair;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.NonZeroFlows;

public class MineFreqEditDiffSeqWrapper {
	
	public static List<FreqEditOpReprSequence> mineFreqEditDiffSeqOnNzFlows(NonZeroFlows nzFlows, OrderedStochasticLanguage langL, OrderedStochasticLanguage langR,
			DescriptorDetailedDistancePair descDetailedDistPair) {
		
		EditDifferenceConnectorBuilder builder = new EditDifferenceConnectorBuilder();
		

		for(Triple<Integer, Integer, Double> nzFlowEdge : nzFlows) {
			int indL = nzFlowEdge.getLeft();
			int indR = nzFlowEdge.getMiddle();
			double flow = nzFlowEdge.getRight();
			EditSequence editSeq = descDetailedDistPair.getDetailedDistance().get_distance_op(langL.get(indL), langR.get(indR));
			builder.addToDataset(editSeq, langL.get(indL), langR.get(indR), flow);
		}
		
		EditDifferenceConnection seqMinerConnection = builder.build();	
		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		SequentialPatterns patterns = algo.runAlgorithm(seqMinerConnection.getSeqDatabase(), 0.01);
		
		List<FreqEditOpReprSequence> lFreqEditOp = seqMinerConnection.getFreqEditSequences(patterns);
		
		return lFreqEditOp;
	}

}
