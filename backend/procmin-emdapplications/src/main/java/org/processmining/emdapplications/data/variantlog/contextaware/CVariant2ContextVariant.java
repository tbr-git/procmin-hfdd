package org.processmining.emdapplications.data.variantlog.contextaware;

import java.util.BitSet;

import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.transform.VariantConverter;
import org.python.bouncycastle.util.Arrays;

public class CVariant2ContextVariant<S extends CVariant> implements 
	VariantConverter<S, CVariantCatContSetImpl> {

	@Override
	public CVariantCatContSetImpl convert(S variant) {
		int[] variantCat = variant.getTraceCategories();
		int[] catCopy = Arrays.copyOf(variantCat, variantCat.length);
		
		return new CVariantCatContSetImpl(catCopy, new BitSet(), 
				variant.getSupport());
	}
	
}
