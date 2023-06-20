package org.processmining.emdapplications.hfdd.algorithm.iteration;

import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraphBuilderTimeLimited;

public class HFDDIterationManagementBuilderTimeout extends HFDDIterationManagementBuilder {
	
	/**
	 * Frequent activity itemset mining time.
	 */
	private Integer freqActMiningTimeMs = 20000;

	/**
	 * Target itemset number.
	 */
	private Integer targetActISNbr = 1000;

	/**
	 * Target itemset margin.
	 */
	private Double targetActISMargin = 0.1; 
	
	public HFDDIterationManagementBuilderTimeout() {
		
	}

	@Override
	protected HFDDGraph mineHFDDGraph(BiComparisonDataSource<? extends CVariant> biCompDS)
			throws HFDDIterationManagementBuildingException {
		// Create HFDD Graph
		HFDDGraph graph = null;
		HFDDGraphBuilderTimeLimited graphBuilder = new HFDDGraphBuilderTimeLimited();
		graphBuilder.setFreqActMiningTimeMs(freqActMiningTimeMs)
			.setTargetActISNbr(targetActISNbr)
			.setTargetActISMargin(targetActISMargin);
		try {
			graph = graphBuilder.buildBaseHFDDGraph(biCompDS);
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			logger.error("Failed to create the HFDD Graph");
			throw new HFDDIterationManagementBuildingException("Failed to mine the HFDD Graph: " + e.getMessage(), e);
		}
		return graph;
	}
	
	public HFDDIterationManagementBuilderTimeout setTargetItemsetNumber(int targetNumber) {
		this.targetActISNbr = targetNumber;
		return this;
	}

	public HFDDIterationManagementBuilderTimeout setTargetItemsetMargin(double margin) {
		this.targetActISMargin = margin;
		return this;
	}
	
	public HFDDIterationManagementBuilderTimeout setMaxMiningTime(int time) {
		this.freqActMiningTimeMs = time;
		return this;
	}
	
}
