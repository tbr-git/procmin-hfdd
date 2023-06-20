package org.processmining.emdapplications.data.variantlog.transform;

import org.processmining.emdapplications.data.variantlog.base.CVariant;

public interface CVariantTransformer<T extends CVariant> {
	//TODO CVariantTransformer<T, U> -> changes type
	
	public boolean requiresDuplicateDetection();
	

	public T apply(T variant, boolean inplace);

	public default T apply(T variant) {
		return apply(variant, false);
	}

}
