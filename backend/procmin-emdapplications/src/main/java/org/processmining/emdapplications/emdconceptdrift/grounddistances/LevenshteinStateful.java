package org.processmining.emdapplications.emdconceptdrift.grounddistances;

import java.util.Arrays;

import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTrace;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceCC;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class LevenshteinStateful implements TraceDescDistCalculator {
	
	private Table<Integer, Integer, Double> lvDistLookup = null; 
	  
	
	public LevenshteinStateful() {
		lvDistLookup = HashBasedTable.create();
	}

	@Override
	public double get_distance(TraceDescriptor t1, TraceDescriptor t2) {
		if(t1 instanceof BasicTrace && t2 instanceof BasicTrace) {
			BasicTrace t1Basic = (BasicTrace) t1;
			BasicTrace t2Basic = (BasicTrace) t2;
			return calcNormLevDist(t1Basic.getsTrace(), t2Basic.getsTrace());
		}
		else if(t1 instanceof BasicTraceCC && t2 instanceof BasicTraceCC) {
			BasicTraceCC t1Casted = (BasicTraceCC) t1;
			BasicTraceCC t2Casted = (BasicTraceCC) t2;
			return calcNormLevDist(t1Casted.getTraceCategories(), t2Casted.getTraceCategories());
		}
		else {
			throw new RuntimeException("LevenshteinStateful is not defined for descriptors: " + t1.getClass() + " - " + t2.getClass());
		}
		
	}
	

	public double calcNormLevDist(String[] s1, String[] s2) {
		int h1 = Arrays.hashCode(s1);
		int h2 = Arrays.hashCode(s2);
		
		Double d = lvDistLookup.get(h1, h2);
		if(d != null) {
			return d;
		}
		else {
			d = Levenshtein.getNormalisedDistance(s1, s2);
			lvDistLookup.put(h1, h2, d);
			lvDistLookup.put(h2, h1, d);
			return d;
		}
	}

	public double calcNormLevDist(int[] t1, int[] t2) {
		int h1 = Arrays.hashCode(t1);
		int h2 = Arrays.hashCode(t2);
		
		Double d = lvDistLookup.get(h1, h2);
		if(d != null) {
			return d;
		}
		else {
			d = Levenshtein.getNormalisedDistance(t1, t2);
			lvDistLookup.put(h1, h2, d);
			lvDistLookup.put(h2, h1, d);
			return d;
		}
	}

	@Override
	public String toString() {
		return "LevenshteinStateful []";
	}


	@Override
	public String getShortDescription() {
		return "LVS";
	}


}
