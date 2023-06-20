package org.processmining.emdapplications.emdconceptdrift.helperclasses;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.processmining.emdapplications.emdconceptdrift.config.WindowParameter;

import com.google.gson.Gson;

public class SlidingEMDOutput {
	
	private final int winSize;
	
	private final int strideSize;
	
	private final ArrayList<Double> outEMDVals;

	public SlidingEMDOutput(WindowParameter wPara, ArrayList<Double> outEMDVals) {
		this.winSize = wPara.getWinSize();
		this.strideSize = wPara.getStrideSize();
		this.outEMDVals = outEMDVals;
	}
	
	public ArrayList<Double> getEmdVals() {
		return outEMDVals;
	}

	public int getWinSize() {
		return winSize;
	}
	
	public int getSize() {
		return outEMDVals.size();
	}

	public int getStrideSize() {
		return strideSize;
	}
	
	public String toJson() {
		JSONObject jo = new JSONObject();
		
		jo.put("Window Size", this.winSize);
		jo.put("Window Stride", this.strideSize);
		jo.put("EMDs", new Gson().toJson(outEMDVals));
		return jo.toString();
	}
	
}
