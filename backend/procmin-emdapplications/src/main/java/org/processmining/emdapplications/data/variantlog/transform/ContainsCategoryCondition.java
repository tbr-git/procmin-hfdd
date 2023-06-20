package org.processmining.emdapplications.data.variantlog.transform;

import org.processmining.emdapplications.data.variantlog.base.CVariant;

public class ContainsCategoryCondition<T extends CVariant> implements CVariantCondition<T> {
	
	private final int category;
	
	public ContainsCategoryCondition(int category) {
		this.category = category;
	}

	@Override
	public boolean satisfies(T variant) {
		return variant.containsCategory(category);
	}

}
