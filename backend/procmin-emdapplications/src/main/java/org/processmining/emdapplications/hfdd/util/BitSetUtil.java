package org.processmining.emdapplications.hfdd.util;

import java.util.BitSet;

public class BitSetUtil {
	
	public static boolean isSubset(BitSet subset, BitSet superset) {
		
		if (subset.cardinality() > superset.cardinality()) {
			return false;
		}
		else { 
			for (int i = subset.nextSetBit(0); i >= 0; i = subset.nextSetBit(i + 1)) {
				if (!superset.get(i)) { 
					return false; 
				} 
			}
		}
		return true;
	}
}
