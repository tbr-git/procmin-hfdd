package org.processmining.emdapplications.emdconceptdrift.helperclasses;

import org.jfree.data.Range;

public class UIUtilities {
	
		public static Range getRange(MultiDimSlidingEMDOutput mdsemd) {
		double min = 1000;
		double max = -1;
		
		for(SlidingEMDOutput semd : mdsemd) {
			for(double v : semd.getEmdVals()) {
				if(v < min)
					min = v;
				else if(v > max)
					max = v;
			}
		}
		
		return new Range(min, max);
	}

}
