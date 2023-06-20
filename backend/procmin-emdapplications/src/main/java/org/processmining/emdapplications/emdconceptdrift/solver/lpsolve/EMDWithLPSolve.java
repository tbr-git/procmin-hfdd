package org.processmining.emdapplications.emdconceptdrift.solver.lpsolve;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.SlidingStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguageIterator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class EMDWithLPSolve {
	private final static Logger logger = LogManager.getLogger( EMDWithLPSolve.class );
	
	public EMDWithLPSolve() {
		
	}
	
	public static double calculateEMD(StochasticLanguage Ll, 
			StochasticLanguage Lr, TraceDescDistCalculator trDescDist) {
		
			LpSolve lp;
			int status_solve;
			try {
				lp = formulateLP(Ll, Lr, trDescDist);
				status_solve = lp.solve();
				logger.debug(String.format("Run %d iterations", lp.getTotalIter()));
			} catch (LpSolveException e) {
				e.printStackTrace();
				return Double.NEGATIVE_INFINITY;
			}
			if(status_solve != 0) {
				logger.error("Could not optimally solve the LP!");
			}
			try {
				return lp.getObjective();
			} catch (LpSolveException e) {
				e.printStackTrace();
				return Double.NEGATIVE_INFINITY;
			}
	}
	
	
	/**
	 * Formulate EMD comparison between trace distributions (comp. {@link org.processmining.earthmoversstochasticconformancechecking.algorithms.EarthMoversStochasticConformanceNormalisedLevenshtein#makeProblem(StochasticLanguage, StochasticPathLanguage)}. 
	 * @param Lr Left stochastic language (sources)
	 * @param Ll Right stochastic language (targets)
	 * @return LP for the EMD Comparison
	 * @throws LpSolveException 
	 */
	private static LpSolve formulateLP(StochasticLanguage Ll, 
			StochasticLanguage Lr, TraceDescDistCalculator trDescDist) throws LpSolveException {
	    int nSrc = Ll.getNumberOfTraceVariants();
	    int nTar = Lr.getNumberOfTraceVariants();
	    logger.debug(String.format("Formulating LP with %d sources and %d targets", nSrc, nTar));
		int nbrVars = nSrc * nTar;
		LpSolve solver = LpSolve.makeLp(0, nbrVars);

		solver.setDebug(false);
		solver.setVerbose(0);

		solver.setAddRowmode(true);

		//set the objective function
		double[] objectiveFunction = makeObjectiveLV(Ll, Lr, trDescDist);
		solver.setObjFn(objectiveFunction);

		//System.out.println(DistanceMatrix2String.toLatex(L, languageModel, objectiveFunction));

		// Source constraints
		{
			StochasticLanguageIterator itL = Ll.iterator();
			TIntList indices = new TIntArrayList();
			TDoubleList values = new TDoubleArrayList();
			for (int l = 0; l < nSrc; l++) {
				indices.clear();
				values.clear();
				for (int m = 0; m < nTar; m++) {
					indices.add(1 + l * nTar + m);
					values.add(1);
				}
				itL.next();
				//String[] next = itL.next();
				//System.out.println(Arrays.toString(next));
				solver.addConstraintex(indices.size(), values.toArray(), indices.toArray(), LpSolve.EQ,
						itL.getProbability());
			}
		}

		//each column should sum to M's value
		{
			StochasticLanguageIterator itM = Lr.iterator();
			TIntList indices = new TIntArrayList();
			TDoubleList values = new TDoubleArrayList();
			for (int m = 0; m < nTar; m++) {
				indices.clear();
				values.clear();
				for (int l = 0; l < nSrc; l++) {
			        indices.add(1 + l * nTar + m);
			        values.add(1);
				}
				itM.next();

				solver.addConstraintex(indices.size(), values.toArray(), indices.toArray(), LpSolve.GE,
						itM.getProbability());
			}
		}

		solver.setAddRowmode(false);

		return solver;
	}
	
	
	/**
	 * Make objective of EMD between Stochastic Languages (comp. {@link org.processmining.earthmoversstochasticconformancechecking.helperclasses.MakeObjectiveFunction#getNormalisedDistances(StochasticLanguage, StochasticPathLanguage)} 
	 * @param Ll Stochastic Language
	 * @param Lr Stochastic Language
	 * @return Cost Matrix L->M in row-major order
	 */
	private static double[] makeObjectiveLV(StochasticLanguage Ll, StochasticLanguage Lr, TraceDescDistCalculator trDistCalc) {
		double[] objective = new double[Ll.getNumberOfTraceVariants() * Lr.getNumberOfTraceVariants() + 1];

		StochasticLanguageIterator itL = Ll.iterator();
		for (int l = 0; l < Ll.getNumberOfTraceVariants(); l++) {
			TraceDescriptor traceL = itL.next();
			StochasticLanguageIterator itR = Lr.iterator();
			for (int m = 0; m < Lr.getNumberOfTraceVariants(); m++) {
				TraceDescriptor traceR = itR.next();
				objective[l * Lr.getNumberOfTraceVariants() + m + 1] = trDistCalc.get_distance(traceL, traceR);
			}
		}
		return objective;
	}
}
