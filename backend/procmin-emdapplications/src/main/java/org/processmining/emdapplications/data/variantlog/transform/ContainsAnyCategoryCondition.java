package org.processmining.emdapplications.data.variantlog.transform;

import org.processmining.emdapplications.data.variantlog.base.CVariant;

public class ContainsAnyCategoryCondition<T extends CVariant> implements CVariantCondition<T> {

	private final int[] categories;
	
	public ContainsAnyCategoryCondition(int[] categories) {
		this.categories = categories;
	}
	
	@Override
	public boolean satisfies(T variant) {
		return variant.containsAnyCategory(categories);
	}

}
