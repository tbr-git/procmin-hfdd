package org.processmining.emdapplications.data.variantlog.transform;

import org.processmining.emdapplications.data.variantlog.base.CVariant;

public class CVariantPrefixTruncator<T extends CVariant> implements CVariantTransformer<T> {
	
	/**
	 * Length of the prefix to which the variant will be truncated. 
	 */
	private final int prefixLength;

	public CVariantPrefixTruncator(int prefixLength) {
		this.prefixLength = prefixLength;
	}
	
	@Override
	public boolean requiresDuplicateDetection() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T apply(T variant, boolean inplace) {
		return (T) variant.extractSubtrace(0, prefixLength, inplace);
	}

	public int getPrefixLength() {
		return prefixLength;
	}

}
