package org.processmining.emdapplications.util.algorithm.fpgrowth;

/**
 * This code is based on the code found in  https://github.com/PySualk/fp-growth-java 
 * @author brockhoff
 * @author PySualk
 *
 */
public interface WeightedTransactionDataSource<T> {

	WeightedTransaction<T> next();

	boolean hasNext();

	void reset();

}
