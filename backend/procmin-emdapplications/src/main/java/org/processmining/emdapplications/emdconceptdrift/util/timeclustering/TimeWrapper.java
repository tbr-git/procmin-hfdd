package org.processmining.emdapplications.emdconceptdrift.util.timeclustering;

import org.apache.commons.math3.ml.clustering.Clusterable;

//wrapper class
public class TimeWrapper implements Clusterable {
	private double t; 

	public TimeWrapper(double t) {
		this.t = t;
	}

	public double getTime() {
		return t;
	}

	public double[] getPoint() {
		return new double[]{t};
	}
}
