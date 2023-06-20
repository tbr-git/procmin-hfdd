package org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup;

import java.util.List;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDLogTransformStep;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public interface ViewConfigProvider<T extends CVariant> {
	
	/**
	 * 
	 * Provide a view configuration based on logs.
	 * 
	 * @param v Vertex for which the view config will be created
	 * @param vertexLogFilterStack Stack of applied log transformations.
	 * @return
	 */
	public ViewConfig provideViewConfig(HFDDVertex v, List<HFDDLogTransformStep<? extends T>> vertexLogFilterStack);

}
