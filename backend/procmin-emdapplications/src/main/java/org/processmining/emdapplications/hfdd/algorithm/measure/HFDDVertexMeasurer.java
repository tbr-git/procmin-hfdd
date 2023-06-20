package org.processmining.emdapplications.hfdd.algorithm.measure;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ProbMassNonEmptyTrace;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurement;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurementEMDSol;

/**
 * Specifies base functionalities that a class that measures (e.g., using EMD)
 * differences w.r.t. HFDD vertices.
 * <p>
 * <b>IMPORTANT: </b>
 * Implementing subclasses should be thread-safe.
 * 
 * @author brockhoff
 *
 */
public interface HFDDVertexMeasurer<T extends CVariant> {
	
	/**
	 * Measure a vertex given the data.
	 * Running <b>must not</b> change the state of the measurer!!! 
	 * @param <T> Type of the variants
	 * @param v Handle to the HFDD Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @param safe Add the result of the measurement to the vertex data.  
	 * @return Measurement result
	 */
	public HFDDMeasurement measureVertex(HFDDVertex v, BiComparisonDataSource<T> biCompDS, boolean safe);

	/**
	 * Run a detailed measurement on the vertex given the data source.
	 * In contrast to this{@link #measureVertex(HFDDVertex, BiComparisonDataSource, boolean)}, 
	 * the returned {@link HFDDMeasurementEMDSol} object will contain more information, 
	 * in particular, the actual best flow.
	 * Running <b>must not</b> change the state of the measurer!!! 
	 * 
	 * @param <T> Type of the variants
	 * @param v Handle to the HFDD Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @param safe Add the result of the measurement to the vertex data.  
	 * @return Measurement result
	 */
	public HFDDMeasurementEMDSol measureVertexDetails(HFDDVertex v, BiComparisonDataSource<T> biCompDS, boolean safe);

	/**
	 * Get the probability mass of non-empty traces for the provided vertex.
	 * @param <T> Type of the variants
	 * @param v Handle to the HFDD Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @return Non-empty trace probability mass for left and right log.
	 */
	public ProbMassNonEmptyTrace getProbabilityMassNonEmpty(HFDDVertex v, BiComparisonDataSource<T> biCompDS);
	
	/**
	 * Get the description for this measurement.
	 * @return
	 */
	public PerspectiveDescriptor getMeasurementDescription();

}
