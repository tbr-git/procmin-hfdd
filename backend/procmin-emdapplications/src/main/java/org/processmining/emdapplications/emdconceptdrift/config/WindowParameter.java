package org.processmining.emdapplications.emdconceptdrift.config;

public class WindowParameter {
	/**
	 * Window size
	 */
	private final int winSize;
	
	/**
	 * Sliding window stride sizes
	 */
	private final int strideSize;
	
	public WindowParameter(int winSize, int strideSize) {
		this.winSize = winSize;
		this.strideSize = strideSize;
	}

	public int getWinSize() {
		return winSize;
	}

	public int getStrideSize() {
		return strideSize;
	}

	@Override
	public String toString() {
		return "WindowParameter [winSize=" + winSize + ", strideSize=" + strideSize + "]";
	}
	
}
