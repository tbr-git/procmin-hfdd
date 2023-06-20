package org.processmining.emdapplications.emdconceptdrift.helperclasses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;

public class MultiDimSlidingEMDOutput implements Iterable<SlidingEMDOutput> {
	private final List<SlidingEMDOutput> multDimEMDVals;
	
	private boolean padded2CommonLength = false;

	public MultiDimSlidingEMDOutput() {
		this.multDimEMDVals = new ArrayList<>();
	}
	
	public void addResult(SlidingEMDOutput outEMDVal) {
		this.multDimEMDVals.add(outEMDVal);
	}
	
	public int sizeDim() {
		return multDimEMDVals.size();
	}
	
	public void sortDimensions() {
		this.multDimEMDVals.sort((final SlidingEMDOutput o1, final SlidingEMDOutput o2) -> Integer.compare(o1.getWinSize(), o2.getWinSize()));
	}
	
	public void sortAndPad() {
		this.sortDimensions();
		int max_steps = multDimEMDVals.get(0).getEmdVals().size();
		
		for(SlidingEMDOutput o : multDimEMDVals) {
			ArrayList<Double> emdVals = o.getEmdVals();
			int padding = (max_steps - emdVals.size()) / 2;
			for(int i = 0; i < padding; i++) {
				emdVals.add(0, 0.0);
				emdVals.add(0.0);
			}
			if(emdVals.size() != max_steps) {
				throw new RuntimeException("Padding failed to reach expected size.");
			}
		}
		
		padded2CommonLength = true;
		
	}
	
	public SlidingEMDOutput getOutput(int index) {
		return multDimEMDVals.get(index);
	}
	
	public int getMaxLen() {
		int max = -1;
		for(SlidingEMDOutput semd : multDimEMDVals) {
			max = Math.max(max, semd.getSize());
		}
		return max;
	}

	@Override
	public Iterator<SlidingEMDOutput> iterator() {
		return multDimEMDVals.iterator();
	}
	
	public int getTraceOffset(int index) {
		if(padded2CommonLength) {
			return multDimEMDVals.get(0).getWinSize();
		}
		else {
			return multDimEMDVals.get(index).getWinSize();
		}
		
	}
	
	public String toJson() {
		return new Gson().toJson(this);
	}
	
}
