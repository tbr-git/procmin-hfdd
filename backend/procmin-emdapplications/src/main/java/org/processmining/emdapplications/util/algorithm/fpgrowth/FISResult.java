package org.processmining.emdapplications.util.algorithm.fpgrowth;

import java.util.Set;

/**
 * @param frequentPatterns Patterns found
 * @param resultComplete Is the itemset complete (i.e., contains all items for the chosen threshold)
 * @param minSupport Minimum support at which the results was mined
*/
public record FISResult<T>(
	Set<WeightedFrequentPattern<T>> frequentPatterns,
	boolean resultComplete, 
	double minSupport) {
}
