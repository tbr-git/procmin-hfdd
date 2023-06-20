package org.processmining.emdapplications.hfdd.algorithm.measure.viewsetup;

import java.util.List;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.hfdd.algorithm.measure.logtransform.HFDDLogTransformStep;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public class ViewConfigConstantProvider<T extends CVariant> implements ViewConfigProvider<T> {
	
	private final ViewConfig viewConfig;
	
	public ViewConfigConstantProvider(ViewConfig viewConfig) {
		this.viewConfig = viewConfig;
	}

	@Override
	public ViewConfig provideViewConfig(HFDDVertex v, List<HFDDLogTransformStep<? extends T>> vertexLogFilterStack) {
		return viewConfig;
	}


}
