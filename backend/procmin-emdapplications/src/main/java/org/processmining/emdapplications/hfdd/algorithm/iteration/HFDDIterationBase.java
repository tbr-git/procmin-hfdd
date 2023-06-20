package org.processmining.emdapplications.hfdd.algorithm.iteration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.variantlog.abstraction.CVariantAbst;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.AdaptiveLVS;
import org.processmining.emdapplications.hfdd.algorithm.PerspectiveIteration;
import org.processmining.emdapplications.hfdd.algorithm.measure.HFDDVertexMeasurer;
import org.processmining.emdapplications.hfdd.algorithm.measure.HFDDVertexMeasurerFactory;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexCondition;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexConditionType;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public class HFDDIterationBase<E extends CVariantAbst> {
	private final static Logger logger = LogManager.getLogger( HFDDIterationBase.class );
	
	/**
	 * Handle to the parent iteration
	 */
	protected final HFDDIterationBase<E> parentIteration;
	
	/**
	 * The perspective descriptor that identifies the created
	 * perspective on the data. Can be used as an identifier.
	 */
	private final PerspectiveDescriptor pDesc;
	
	/**
	 * Iteration.
	 */
	private final int iteration;

	/**
	 * Prepared data source for this iteration.
	 */
	protected BiComparisonDataSource<E> preparedCompDS;

	/**
	 * Adaptive LVS that will be used in this iteration.
	 */
	protected AdaptiveLVS aLVS;
	
	/**
	 * Measure to be applied to the vertices given the data.
	 */
	protected HFDDVertexMeasurer<E> measure;
	
	/**
	 * Handle to the data HFDDGraph.
	 */
	protected final HFDDGraph hfddGraph;
	
	/**
	 * Was the iteration initialized.
	 */
	private boolean isInitialized = false;

	public HFDDIterationBase(HFDDGraph hfddGraph, int iteration,
			BiComparisonDataSource<E> preparedCompDS) {
		super();
		this.hfddGraph = hfddGraph;
		this.parentIteration = null;
		this.pDesc = new PerspectiveIteration(iteration);
		this.iteration = iteration;
		this.preparedCompDS = preparedCompDS;
	}

	/**
	 * Initialize the iteration. 
	 * 
	 * The heavier lifting of the construction, e.g., populating or transforming data sources should happen here
	 * 
	 * This may, for example, include:
	 * <p><ol>
	 * <li>Setup of the distance.
	 * <li>Preparation of the datasource (apply abstractions).
	 * </ol><p>
	 *
	 */
	public final void initializeIteration() {
		if (!isInitialized) {
			this.initializeDistance();
			this.prepareDataSource();
			this.instantiateMeasure();
			this.isInitialized = true;
		}
	}

	public boolean initializeDistance() {
		// For base iteration there are no abstractions yet
		this.aLVS = new AdaptiveLVS();
		return true;
	}

	/**
	 * Prepare the datasource.
	 * 
	 * Data sources will be recursively fetched from parent data source.
	 * 
	 * @return
	 */
	public boolean prepareDataSource() {
		// Data is provided in the constructor
		return true;
	}

	/**
	 * Get the adaptive LVS used in this iteration.
	 * 
	 * @return
	 */
	public AdaptiveLVS getAdaptiveLVS() {
		if (this.aLVS == null) {
			this.initializeDistance();
		}
		return aLVS;
	}

	/**
	 * Instantiate the measurement executor based on the 
	 * <p><ul>
	 * <li> Distance measure
	 * <li> Prepared data source
	 * </ul><p>
	 *
	 */
	protected void instantiateMeasure() {
		
		measure = HFDDVertexMeasurerFactory.createMeasurerAbstContextFreeCond(
				this.preparedCompDS.getClassifier(), 
				aLVS, 
				getAggDataCBase(false), pDesc);

//		DescriptorDistancePair desDistPair = new DescriptorDistancePair(this.aLVS, 
//				new AbstTraceBuilder(this.preparedCompDS.getClassifier()));

//		measure = new HFDDVertexMeasurerCSConditionedImpl<>(hfddGraph, 
//				getAggDataCBase(false), desDistPair, pDesc);

	}

	/**
	 * Get the prepared data source (prepare if necessary). 
	 *
	 * If the data source is not yet prepared, it will be prepared and returned.
	 * @return The prepared data source
	 */
	public BiComparisonDataSource<E> getPreparedCompDS() {
		if (this.preparedCompDS == null) {
			this.prepareDataSource();
		}
		return preparedCompDS;
	}

	public void setPreparedCompDS(BiComparisonDataSource<E> preparedCompDS) {
		this.preparedCompDS = preparedCompDS;
	}

	public HFDDIterationBase<E> getParentIteration() {
		return parentIteration;
	}

	public PerspectiveDescriptor getpDesc() {
		return pDesc;
	}

	public int getIteration() {
		return iteration;
	}

	public HFDDVertexMeasurer<E> getBaseMeasure() {
		return measure;
	}

	public boolean isInitialized() {
		return isInitialized;
	}
	
	public Set<HFDDVertex> getConditionVertices(VertexConditionType conditionType) {
		return new HashSet<>();
	}
	
	public Optional<ArrayList<Set<VertexCondition>>> getAggDataCBase(boolean copy) {
		return Optional.empty();
	}
	
}
