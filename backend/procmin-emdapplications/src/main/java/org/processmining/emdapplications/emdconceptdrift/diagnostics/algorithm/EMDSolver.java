package org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;
import org.processmining.emdapplications.emdconceptdrift.solver.akp.core.AKPFactoryArraySimple;
import org.processmining.emdapplications.emdconceptdrift.solver.akp.core.AKPSolverArraySimple;

public class EMDSolver {

	/**
	 * 
	 * @param xlog Sorted event log
	 * @param traceIndex
	 * @param w_size Window size
	 */
	public static EMDSolContainer getLPSolution(Iterator<XTrace> itL, Iterator<XTrace> itR,  
			AbstractTraceDescriptorFactory langFac, TraceDescDistCalculator trDescDist, Window2OrderedStochLangTransformer langTransformer) {
		
		Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> langs = langTransformer.transformWindow(itL, itR, langFac);

		EMDSolContainer.Builder emdSolBuilder = new EMDSolContainer.Builder();
		emdSolBuilder.addLangLeft(langs.getLeft()).addLangRight(langs.getRight());

		AKPFactoryArraySimple fac = new AKPFactoryArraySimple(trDescDist);
		fac.setupNewSolver(langs.getLeft(), langs.getRight(), emdSolBuilder);
		AKPSolverArraySimple solver = fac.getSolver();
		double emd = solver.solve();

		return  emdSolBuilder.addEMD(emd).addNonZeroFlows(solver.getNonZeroFlows()).build();
	}

	public static EMDSolContainer getLPSolution(CVariantLog<? extends CVariant> variantLogLeft, 
			CVariantLog<? extends CVariant> variantLogRight, AbstractTraceDescriptorFactory langFac, 
			TraceDescDistCalculator trDescDist, Window2OrderedStochLangTransformer langTransformer) {
		
		Pair<OrderedStochasticLanguage, OrderedStochasticLanguage> langs = langTransformer.transformWindow(variantLogLeft, variantLogRight, langFac);

		EMDSolContainer.Builder emdSolBuilder = new EMDSolContainer.Builder();
		emdSolBuilder.addLangLeft(langs.getLeft()).addLangRight(langs.getRight());

		AKPFactoryArraySimple fac = new AKPFactoryArraySimple(trDescDist);
		fac.setupNewSolver(langs.getLeft(), langs.getRight(), emdSolBuilder);
		AKPSolverArraySimple solver = fac.getSolver();
		double emd = solver.solve();

		return  emdSolBuilder.addEMD(emd).addNonZeroFlows(solver.getNonZeroFlows()).build();
	}

}
