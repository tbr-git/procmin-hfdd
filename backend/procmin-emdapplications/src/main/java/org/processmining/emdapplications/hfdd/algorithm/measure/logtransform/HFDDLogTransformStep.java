package org.processmining.emdapplications.hfdd.algorithm.measure.logtransform;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;

public record HFDDLogTransformStep<T extends CVariant>(BiComparisonDataSource<T> biCompDS, FilterTag filterTag) {

}
