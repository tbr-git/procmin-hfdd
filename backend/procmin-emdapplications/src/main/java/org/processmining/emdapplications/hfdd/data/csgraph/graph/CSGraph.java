package org.processmining.emdapplications.hfdd.data.csgraph.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.LevenshteinStateful;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.util.backgroundwork.CachedBackgroundTaskService;
import org.processmining.emdapplications.hfdd.algorithm.measure.CSVertexConditionedMeasurer;
import org.processmining.emdapplications.hfdd.algorithm.measure.CSVertexConditionedResidualMeasurer;
import org.processmining.emdapplications.hfdd.algorithm.measure.CSVertexMeasurer;
import org.processmining.emdapplications.hfdd.algorithm.measure.CSVertexResidualMeasurer;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexCondition;

public class CSGraph {
	private static final Logger logger = LogManager.getLogger(CSGraph.class);
	
	private final UUID uuid;
	/**
	 * Handle to the JGraphtT graph.
	 */
	private final Graph<CSGraphVertex, DefaultEdge> g;
	
	private boolean isComparisonDataInitialized;
	
	private final Optional<ArrayList<Set<VertexCondition>>> conditionBase;
	
	protected CSGraph(Graph<CSGraphVertex, DefaultEdge> graphStructure, 
			Optional<ArrayList<Set<VertexCondition>>> conditionBase) {
		this.uuid = UUID.randomUUID();
		this.g = graphStructure;
		this.isComparisonDataInitialized = false;
		this.conditionBase = conditionBase;
	}
	
	/**
	 * Run the comparison data initialization with a given vertex measure.
	 * 
	 * @param <T> Variant type required by the coupler (data source variants must extends this type)
	 * @param biCompDS Data source to measure on
	 * @param vertexMeasure Vertex measure that defines the comparison data.
	 * @return True, if initialization succeeded.
	 */
	private<T extends CVariant> boolean initializeComparisonData(BiComparisonDataSource<T> biCompDS, 
			Collection<CSVertexMeasurer<T>> vertexMeasures) {

		// ================================================================================
		// Run Initialization on Vertices
		// ================================================================================
		List<Future<Boolean>> runningInitializations = new LinkedList<>();		// Task List
		
		for (CSVertexMeasurer<T> vertexMeasure : vertexMeasures) {
			for(CSGraphVertex v : this.g.vertexSet()) {
				runningInitializations.add(CachedBackgroundTaskService.getInstance().submit(
						new CSVertexInitializationTask<T>(this.g, v, vertexMeasure, biCompDS)));
			}
		}
		
		this.isComparisonDataInitialized = true;
		try {
			for(Future<Boolean> f : runningInitializations) {
				isComparisonDataInitialized &= f.get();
			}
		}
		catch (InterruptedException e1) {
			logger.error("Comparison data initialization has been interrupted");
			return false;
		}
		catch (ExecutionException e) {
			e.printStackTrace();
			logger.error("Error while running concurrent cornerstone graph initialization on vertices");
		}
		if (!this.isComparisonDataInitialized) {
			logger.error("Cornerstone comparison data initialization did not succeed!");
		}
		
		return this.isComparisonDataInitialized;
	}

	/**
	 * Run the comparison data initialization with default view description.
	 * 
	 * Uses global (frequency-aware) language transformer and standard LVS.
	 * 
	 * Runs the initialization for each vertex concurrently.
	 * @return True, if initialization succeeded.
	 */
	public<T extends CVariant> boolean initializeComparisonData(BiComparisonDataSource<T> biCompDS) {
		
		Collection<CSVertexMeasurer<T>> measures = getDefaultMeasures(biCompDS);
		
		return this.initializeComparisonData(biCompDS, measures);
	}
	
	public<T extends CVariant> Collection<CSVertexMeasurer<T>> getDefaultMeasures(BiComparisonDataSource<T> biCompDS) { 

		biCompDS.ensureCaching();
		// Trace descriptor + distance
		DescriptorDistancePair desDistPair = new DescriptorDistancePair(new LevenshteinStateful(), 
				new BasicTraceDescriptorFactory(biCompDS.getClassifier()));
		
		List<CSVertexMeasurer<T>> measures = new LinkedList<>();
		// Instantiate the measurement
		CSVertexResidualMeasurer<T> measureRes = new CSVertexResidualMeasurer<>(this, desDistPair);
		measures.add(measureRes);
		if (conditionBase.isPresent()) {
			CSVertexConditionedMeasurer<T> measureCond = 
					new CSVertexConditionedMeasurer<>(this, desDistPair, conditionBase.get());
			measures.add(measureCond);
			
			CSVertexConditionedResidualMeasurer<T> measureCondRes = 
					new CSVertexConditionedResidualMeasurer<>(this, desDistPair, conditionBase.get());
			measures.add(measureCondRes);
		}
		return measures;
	}

	/**
	 * Initialize the comparison data for the provided vertex.
	 * 
	 * Class that wraps initialization tasks for specific vertices such that initialization can be parallelized.
	 *
	 * @author brockhoff
	 *
	 * @param <E> Log coupler assumes this variant
	 * @param <T> Variant log element type (must extend the type required by the log coupler)
	 */
	public static class CSVertexInitializationTask<T extends CVariant> implements Callable<Boolean> {
		
		/** 
		 * Vertex to run the measurement on.
		 */
		private final CSGraphVertex v;

		/** 
		 * Measurement execution routine that couples log probability measures.
		 */
		private final CSVertexMeasurer<T> vertexMeasure;
		
		/**
		 * Base data source.
		 * Most likely, the vertex will copy this source and apply his particular reduction on it.
		 */
		private final BiComparisonDataSource<T> biCompDS;
		
		/**
		 * Handle to the graph in which the provided vertex {@link #v} is embedded.
		 * Useful to access, for example, neighbors.
		 */
		private final Graph<CSGraphVertex, DefaultEdge> g;


		public CSVertexInitializationTask(Graph<CSGraphVertex, DefaultEdge> g, 
				CSGraphVertex v, CSVertexMeasurer<T> vertexMeasure,
				BiComparisonDataSource<T> biCompDS) {
			super();
			this.g = g;
			this.v = v;
			this.vertexMeasure = vertexMeasure;
			this.biCompDS = biCompDS;
		}


		@Override
		public Boolean call() throws Exception {
			this.vertexMeasure.processVertex(v, biCompDS);
			return true;
		}
		
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	public Graph<CSGraphVertex, DefaultEdge> getG() {
		return g;
	}

	public boolean isComparisonDataInitialized() {
		return isComparisonDataInitialized;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public Optional<ArrayList<Set<VertexCondition>>> getConditionBase() {
		return this.conditionBase;
	}

}
