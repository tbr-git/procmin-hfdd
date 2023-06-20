package org.processmining.emdapplications.hfdd.data.hfddgraph;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.hfdd.algorithm.WeightedActivitySetTransactionsBuilder;
import org.processmining.emdapplications.util.algorithm.fpgrowth.WeightedFPGrowth;
import org.processmining.emdapplications.util.algorithm.fpgrowth.WeightedFrequentPattern;
import org.processmining.emdapplications.util.algorithm.fpgrowth.WeightedTransactionDataSourceImpl;

public class HFDDGraphBuilderTimeLimited extends HFDDGraphBuilder {
	
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
	

	@Override
	protected Set<WeightedFrequentPattern<Integer>> mineActivityItemsets(
			BiComparisonDataSource<? extends CVariant> dataSource) throws SLDSTransformationError {
		// Build transactions
		WeightedTransactionDataSourceImpl<Integer> FISTransactions = 
				WeightedActivitySetTransactionsBuilder.buildFromLog(dataSource);
		//WeightedFPGrowth<Integer> alg = new WeightedFPGrowth<>();
		WeightedFPGrowth<Integer> alg = new WeightedFPGrowth<>((i1, i2) -> Integer.compare(i1, i2));
	
		Set<WeightedFrequentPattern<Integer>> res = null;
		try {
			res = alg.findFrequentPattern(FISTransactions, this.freqActMiningTimeMs, 
					this.targetActISNbr, this.targetActISMargin).get().get().frequentPatterns();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}

	public Integer getFreqActMiningTimeMs() {
		return freqActMiningTimeMs;
	}


	public HFDDGraphBuilderTimeLimited setFreqActMiningTimeMs(Integer freqActMiningTimeMs) {
		this.freqActMiningTimeMs = freqActMiningTimeMs;
		return this;
	}


	public Integer getTargetActISNbr() {
		return targetActISNbr;
	}


	public HFDDGraphBuilderTimeLimited setTargetActISNbr(Integer targetActISNbr) {
		this.targetActISNbr = targetActISNbr;
		return this;
	}


	public Double getTargetActISMargin() {
		return targetActISMargin;
	}


	public HFDDGraphBuilderTimeLimited setTargetActISMargin(Double targetActISMargin) {
		this.targetActISMargin = targetActISMargin;
		return this;
	}
}
