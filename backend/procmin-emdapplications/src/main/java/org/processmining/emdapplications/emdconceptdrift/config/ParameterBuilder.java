package org.processmining.emdapplications.emdconceptdrift.config;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParameterBuilder {
	private final static Logger logger = LogManager.getLogger( ParameterBuilder.class );

	private ArrayList<WindowParameter> windows;
	
	private EMDTraceCompParamBuilder traceCompBuilder;

	public ParameterBuilder() {
		windows = new ArrayList<>();
		traceCompBuilder = new EMDTraceCompParamBuilder();
	}

	public ParameterBuilder addWindow(int winSize, int strideSize) {
		this.windows.add(new WindowParameter(winSize, strideSize));
		return this;
	}
	
	public EMDTraceCompParamBuilder getTraceComparisonParamBuilder() {
		return traceCompBuilder;
	}

	public EMDConceptDriftParameters build() {
		if(windows.size() == 0) {
			logger.info("Using default window: Size 200, Stride 50");
			this.windows.add(new WindowParameter(200, 50));
		}
		return new EMDConceptDriftParameters(windows, traceCompBuilder.build());
	}

	public ArrayList<WindowParameter> getWindows() {
		return windows;
	}
}