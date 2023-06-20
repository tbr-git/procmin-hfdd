package org.processmining.emdapplications.hfdd.util;

import java.util.stream.StreamSupport;

import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguageIterator;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;

public class EMDSolAnalyzer {
	
	public static double flow2EmptyCost(EMDSolContainer emdSol) {
	
		int indexEmptyTraceLeft = getIndexEmptyTrace(emdSol.getLanguageLeft());
		int indexEmptyTraceRight = getIndexEmptyTrace(emdSol.getLanguageRight());
		
		if (indexEmptyTraceLeft == -1 && indexEmptyTraceRight == -1) {
			return 0;
		}
		
		
		double flow2EmptyCost = StreamSupport.stream(emdSol.getNonZeroFlows().spliterator(), false)
			.filter(t -> (t.getLeft() == indexEmptyTraceLeft || t.getMiddle() == indexEmptyTraceRight))
			.mapToDouble(t -> emdSol.getCost(t.getLeft(), t.getMiddle()) * t.getRight())
			.sum();
			
		return flow2EmptyCost;
	}
	
	private static int getIndexEmptyTrace(OrderedStochasticLanguage stochLang) {
		int indexEmptyTrace = -1;
		int i = 0;
		StochasticLanguageIterator  itL = stochLang.iterator();
		while (itL.hasNext() && indexEmptyTrace == -1) {
			if(itL.next().length() == 0) {
				indexEmptyTrace = i;
			}
			i++;
		}
		
		return indexEmptyTrace;
	}
}
