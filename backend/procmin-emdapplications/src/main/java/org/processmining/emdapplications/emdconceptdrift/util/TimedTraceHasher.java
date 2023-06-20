package org.processmining.emdapplications.emdconceptdrift.util;

import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimedTrace;

import gnu.trove.strategy.HashingStrategy;

public class TimedTraceHasher implements HashingStrategy<TimedTrace> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bTimedHash;
	
	public TimedTraceHasher(boolean bTimedHash) {
		this.bTimedHash = bTimedHash;
	}

	@Override
	public int computeHashCode(TimedTrace timeTrace) {
		if(bTimedHash) {
			return timeTrace.hashCode();
		}
		else {
			return timeTrace.hashCodeTrace();
		}
	}

	@Override
	public boolean equals(TimedTrace o1, TimedTrace o2) {
		return o1.equals(o2);
	}

}
