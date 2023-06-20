package org.processmining.emdapplications.hfdd.algorithm.measure.logtransform;

import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public interface HFDDVertexLogTransformerOuterContext<T extends CVariant> {
	
	/**
	 * Transform the provided log based on some context information that is "outside" of the vertex v.
	 * For example, residual flow (only traces that do not intersect with a different vertex).
	 * @param v
	 * @param biCompDS
	 * @return
	 * @throws SLDSTransformerBuildingException 
	 */
	public HFDDLogTransformStep<T> getDataSourceOuterContext(HFDDVertex v, BiComparisonDataSource<T> biCompDS) throws SLDSTransformerBuildingException;

}
