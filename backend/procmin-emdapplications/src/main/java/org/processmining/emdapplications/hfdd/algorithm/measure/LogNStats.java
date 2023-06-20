package org.processmining.emdapplications.hfdd.algorithm.measure;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;

/**
 * Simple data class that wraps the filtered log and some statistics on it. 
 * @author brockhoff
 *
 * @param <E> Variant type
 */
public record LogNStats<E extends CVariant>(BiComparisonDataSource<? extends E> biCompDS, 
		double probNonEmptyLeft, double probNonEmptyRight, boolean change) {
}