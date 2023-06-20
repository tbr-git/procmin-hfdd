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

public class HFDDGraphBuilderByMinSupport extends HFDDGraphBuilder {
	
	/**
	 * Minimum support.
	 */
	private double minRelativeSupport = 0.02;

	@Override
	protected Set<WeightedFrequentPattern<Integer>> mineActivityItemsets(
			BiComparisonDataSource<? extends CVariant> dataSource) 
			throws SLDSTransformationError {
//		int totalSize = dataSource.getDataSourceLeft().getVariantLog().sizeLog();
//		totalSize += dataSource.getDataSourceRight().getVariantLog().sizeLog();

		// Build transactions
		WeightedTransactionDataSourceImpl<Integer> FISTransactions = 
				WeightedActivitySetTransactionsBuilder.buildFromLog(dataSource);
		//WeightedFPGrowth<Integer> alg = new WeightedFPGrowth<>();
		WeightedFPGrowth<Integer> alg = new WeightedFPGrowth<>((i1, i2) -> Integer.compare(i1, i2));
	
		Set<WeightedFrequentPattern<Integer>> res = null;
		try {
			res = alg.findFrequentPattern(minRelativeSupport, FISTransactions).get().get().frequentPatterns();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;

	}

	public double getMinRelativeSupport() {
		return minRelativeSupport;
	}

	public HFDDGraphBuilderByMinSupport setMinRelativeSupport(double minRelativeSupport) {
		this.minRelativeSupport = minRelativeSupport;
		return this;
	}

}
