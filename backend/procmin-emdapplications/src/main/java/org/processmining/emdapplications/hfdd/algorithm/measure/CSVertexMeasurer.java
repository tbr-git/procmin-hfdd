package org.processmining.emdapplications.hfdd.algorithm.measure;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraphVertex;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurement;

/**
 * Specifies base functionalities that a class that measures (e.g., using EMD)
 * differences w.r.t. CS Graph vertices.
 * <p>
 * <b>IMPORTANT: </b>
 * Implementing subclasses should be thread-safe.
 * 
 * @author brockhoff
 *
 */
public interface CSVertexMeasurer <T extends CVariant> {
	
	/**
	 * Measure a vertex given the data.
	 * Running <b>must not</b> change the state of the measurer!!! 
	 * @param <T> Type of the variants
	 * @param v Handle to the CSGraph Vertex
	 * @param biCompDS Data source to run the measurement between two logs on
	 * @param safe Add the result of the measurement to the vertex data.  
	 * @return Measurement result
	 */
	public boolean processVertex(CSGraphVertex v, BiComparisonDataSource<T> biCompDS);

	/**
	 * Get the description for this measurement.
	 * @return
	 */
	public PerspectiveDescriptor getMeasurementDescription();

}
