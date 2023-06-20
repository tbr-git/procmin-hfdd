package org.processmining.emdapplications.hfdd.algorithm.measure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ProbMassNonEmptyTrace;
import org.processmining.emdapplications.hfdd.data.csgraph.CSMeasurementTypes;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraphVertex;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraphVertexCS;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurementEMDSol;

public abstract class CSVertexMeasurerBase<T extends CVariant> implements CSVertexMeasurer<T> {

	private final static Logger logger = LogManager.getLogger( CSVertexMeasurerBase.class );

	/**
	 * Handle to CS Graph that defines the neighborhood context.
	 */
	private final CSGraph csGraph;
	
	/**
	 * Save measurer.
	 */
	private HFDDVertexMeasurerImpl<T> measurer;
	
	/**
	 * Measure only probability.
	 */
	private boolean probabilityOnly;
	
	public CSVertexMeasurerBase(CSGraph csGraph) {
		super();
		this.csGraph = csGraph;
		measurer = null;
		this.probabilityOnly = false;
	} 

	public CSVertexMeasurerBase(CSGraph csGraph, boolean probabilityOnly) {
		super();
		this.csGraph = csGraph;
		measurer = null;
		this.probabilityOnly = probabilityOnly;
	} 
	
	@Override
	public boolean processVertex(CSGraphVertex v, BiComparisonDataSource<T> biCompDS) {
	
		// Instantiate if not yet instantiated
		if (measurer == null) {
			synchronized(this) {
				if (measurer == null) {
					measurer = getMeasurer(biCompDS);	
				}
			}
		}
		
		////////////////////////////////////////
		// Run Measurement
		////////////////////////////////////////
		//////////////////////////////
		// Add EMD Solution
		//////////////////////////////
		if (!probabilityOnly && (v instanceof CSGraphVertexCS vCS)) {
			////////////////////
			// EMD
			////////////////////
			HFDDMeasurementEMDSol m = measurer.measureVertexDetails(v.getHfddVertexRef(), biCompDS, false);
			//TODO Store probability in HFDD measurement properly
			vCS.setProbabilityMassInfo(getMeasurementType(), new ProbMassNonEmptyTrace(
					m.getProbLeftNonEmpty(), m.getProbRightNonEmpty(), m.isProbabilityZero()));
			vCS.setMeasurement(getMeasurementType(), m);
			return true;
		}
		else {
			// Only probabilities
			ProbMassNonEmptyTrace p = measurer.getProbabilityMassNonEmpty(v.getHfddVertexRef(), biCompDS);
			v.setProbabilityMassInfo(getMeasurementType(), p);
			return true;
		}
	}
	
	protected abstract HFDDVertexMeasurerImpl<T> getMeasurer(BiComparisonDataSource<? extends T> biCompDS);
	
	protected abstract CSMeasurementTypes getMeasurementType();

	public CSGraph getCsGraph() {
		return csGraph;
	}

	public boolean isProbabilityOnly() {
		return probabilityOnly;
	}

	public void setProbabilityOnly(boolean probabilityOnly) {
		this.probabilityOnly = probabilityOnly;
	}
	

}
