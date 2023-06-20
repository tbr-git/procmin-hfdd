package org.processmining.hfddbackend.dto;

import java.util.LinkedList;
import java.util.List;

public class CSGraphSpecDTO {
	
	private List<Integer> cornerstoneVertices;
	
	private int conditionIteration;

	public CSGraphSpecDTO() {
		this.cornerstoneVertices = new LinkedList<>();
		this.conditionIteration = -1;
	}

	public List<Integer> getCornerstoneVertices() {
		return cornerstoneVertices;
	}

	public void setCornerstoneVertices(List<Integer> cornerstoneVertices) {
		this.cornerstoneVertices = cornerstoneVertices;
	}

	public int getConditionIteration() {
		return conditionIteration;
	}

	public void setConditionIteration(int conditionIteration) {
		this.conditionIteration = conditionIteration;
	}

	@Override
	public String toString() {
		return "CSGraphSpecDTO [cornerstoneVertices=" + cornerstoneVertices + ", conditionIteration="
				+ conditionIteration + "]";
	}
	

}
