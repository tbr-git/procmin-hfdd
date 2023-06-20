package org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.prefixspan;

import java.util.Arrays;

/**
 * @author brockhoff
 *
 * @param <T>
 */
public class WeightedSequence {

	private int[] sequence;
	
	private double weight;
	
	public WeightedSequence(int[] sequence, double weight) {
		this.sequence = sequence;
		this.weight = weight;
	}
	
	public int[] getSequence() {
		return sequence;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		String s = Arrays.toString(sequence);
		s += " (" + weight + ")";
		return s;
	}

}
