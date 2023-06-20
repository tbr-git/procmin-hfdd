package org.processmining.emdapplications.data.variantlog.transform;

import org.processmining.emdapplications.data.variantlog.base.CVariant;

public interface CVariantCondition<T extends CVariant> {
	
	public boolean satisfies(T variant);

}
