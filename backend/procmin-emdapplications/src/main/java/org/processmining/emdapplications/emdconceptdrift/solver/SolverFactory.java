package org.processmining.emdapplications.emdconceptdrift.solver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguageIterator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;

/**
 * An abstract class that contains basic functionality every class
 * that sets up a solver needs. 
 * <p>
 * Building a factory of this type also means to set up a context in
 * which the following solver are build in e.g. a common ground distance and
 * if needed a tolerance for being optimal.
 * Furthermore, common data as a value for additional information to the solver
 * building process is abstracted.
 * @author brockhoff
 *
 * @param <T> The type of solver that is constructed.
 */
public abstract class SolverFactory<T> {
	private final static Logger logger = LogManager.getLogger( SolverFactory.class );
	
	/**
	 * Some additional data for the initialization
	 */
	protected int additionalInfo;
	/**
	 * Ground distance that will be used to compute costs
	 */
	private TraceDescDistCalculator groundDist;
	
	/**
	 * Tolerance for the solver to be optimal if not exact 
	 * anyway
	 */
	private double tol;
		
	/**
	 * Constructor in case that the solver does not 
	 * @param groundDist
	 */
	public SolverFactory(TraceDescDistCalculator groundDist) {
		super();
		this.groundDist = groundDist;
		this.tol = 0;
	}
	
	/**
	 * @param groundDist
	 * @param tol
	 */
	public SolverFactory(TraceDescDistCalculator groundDist, double tol) {
		super();
		this.groundDist = groundDist;
		this.tol = tol;
	}
	
	/**
	 * 
	 * @param groundDist
	 * @param additionalInfo
	 */
	public SolverFactory(TraceDescDistCalculator groundDist, int additionalInfo) {
		super();
		this.additionalInfo = additionalInfo;
		this.groundDist = groundDist;
		this.tol = 0;
	}

	/**
	 * This constructor is used by IPM.
	 * @param additionalInfo
	 * @param groundDist
	 * @param tol
	 * 
	 */
	public SolverFactory(TraceDescDistCalculator groundDist, double tol, int additionalInfo) {
		super();
		this.additionalInfo = additionalInfo;
		this.groundDist = groundDist;
		this.tol = tol;
	}

	/**
	 * Sets up a new solver of the given type.
	 * @param s1 StochasticLanguage "from"
	 * @param s2 StochasticLanguage "to"
	 * @param costs Cost matrix
	 * @param deltaBound "DeltaBound" needed for IM initialization
	 */
	public abstract void setupNewSolver(StochasticLanguage s1, StochasticLanguage s2, 
			EMDSolContainer.Builder emdSolBuilder);
	
	/**
	 * 
	 * @return Current solver instance
	 */
	public abstract T getSolver();

	/**
	 * @return the groundDist
	 */
	public TraceDescDistCalculator getGroundDist() {
		return groundDist;
	}

	/**
	 * @return the tol
	 */
	public double getTol() {
		return tol;
	}
	
	/**
	 * Calculates the distance vector for a given ground distance.
	 * Used as an objective function vector of the pattern
	 * (source 1 to target 1, source 1 to target 2, ..., source 2 to target 1, ...)
	 * @param Ll StochasticLanguage source
	 * @param Lr StochasticLanguage target
	 * @param gd Ground distance
	 * @return ground distance vector
	 */
	public static double[] computeDistanceMatrixAsVector(StochasticLanguage Ll, StochasticLanguage Lr, TraceDescDistCalculator gd) {
		double[] groundDistMatrix = new double[Ll.getNumberOfTraceVariants() * Lr.getNumberOfTraceVariants()];
		//source loop
		int i = 0;
		TraceDescriptor traceL;
		TraceDescriptor traceR;
		StochasticLanguageIterator itL = Ll.iterator();
		StochasticLanguageIterator itR = null;
		while(itL.hasNext()) {
			traceL = itL.next();
			itR = Lr.iterator();
			while(itR.hasNext()) {
				traceR = itR.next();
				groundDistMatrix[i] = gd.get_distance(traceL, traceR);
				i++;
			}
		}
		return groundDistMatrix;
	}
	
	/**
	 * Calculates the distance matrix for a given ground distance.
	 * Entry (from, to) of the matrix is interpreted as flowcost from
	 * source from to target to.
	 * @param Ll StochasticLanguage source
	 * @param f2 StochasticLanguage target
	 * @param gd Ground distance
	 * @return ground distance matrix
	 */
	public static double[][] computeDistanceMatrix(StochasticLanguage Ll, StochasticLanguage Lr, TraceDescDistCalculator gd) {
		double[][] groundDistMatrix = new double[Ll.getNumberOfTraceVariants()][Lr.getNumberOfTraceVariants()];
		//source loop
		StochasticLanguageIterator itL = Ll.iterator();
		StochasticLanguageIterator itR = null;
//		TraceDescriptor traceL;
//		TraceDescriptor traceR;
		for (int i = 0; i < Ll.getNumberOfTraceVariants(); i++) {
			final TraceDescriptor traceL = itL.next();
//			traceL = itL.next();
			itR = Lr.iterator();
			//target loop
			for (int j = 0; j < Lr.getNumberOfTraceVariants(); j++) {
				final TraceDescriptor traceR = itR.next();
//				traceR = itR.next();
				final double d = gd.get_distance( traceL, traceR);
				groundDistMatrix[i][j] = d;
//				logger.trace(() -> "Ground distance fast " + d);
//				logger.trace(() -> EditVisualizer.formatEditArray((TraceDescBinnedActDur) traceL, (TraceDescBinnedActDur) traceR, ((TimeBinnedWeightedLevenshteinStateful) gd).get_distance_op(traceL, traceR)));
			}
		}
		return groundDistMatrix;
	}
	
	/**
	 * Calculates the distance matrix for a given ground distance.
	 * Entry (from, to) of the matrix is interpreted as flowcost from
	 * source from to target to.
	 * @param Ll StochasticLanguage source
	 * @param Lr StochasticLanguage target
	 * @param gd Ground distance
	 * @return ground distance matrix
	 */
	public static double[] computeDistanceMatrixExtremeValues(StochasticLanguage Ll, StochasticLanguage Lr, TraceDescDistCalculator gd, double[][] groundDistMatrix) {
		double dist;
		double maxCost = Double.NEGATIVE_INFINITY;
		double minCost = Double.POSITIVE_INFINITY;

		//source loop
		StochasticLanguageIterator itL = Ll.iterator();
		StochasticLanguageIterator itR = null;
		TraceDescriptor traceL;
		TraceDescriptor traceR;
		for (int i = 0; i < groundDistMatrix.length; i++) {
			traceL = itL.next();
			itR = Lr.iterator();
			//target loop
			for (int j = 0; j < groundDistMatrix[i].length; j++) {
				traceR = itR.next();
				dist = gd.get_distance( traceL, traceR);
				groundDistMatrix[i][j] = dist;
				if(dist > maxCost) {
					maxCost = dist;
				}
				//No else if in case of all costs equal
				if(dist < minCost) {
					minCost = dist;
				}
			}
		}
		return new double[]{minCost, maxCost};
	}

	/**
	 * 
	 * @return The additional information stored
	 */
	public int getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * Set the additional information.
	 * @param additionalInfo The additional information value to store
	 */
	public void setAdditionalInfo(int additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

}
