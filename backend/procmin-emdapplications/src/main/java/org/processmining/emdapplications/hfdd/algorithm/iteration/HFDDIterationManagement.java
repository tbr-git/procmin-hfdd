package org.processmining.emdapplications.hfdd.algorithm.iteration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.variantlog.abstraction.CCCVariantAbstImpl;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.hfdd.algorithm.dto.DiffCandidateShortInfo;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexMeasurementDTO;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexShortInfoDTO;
import org.processmining.emdapplications.hfdd.algorithm.iteration.diffcandidates.DiffCandidate;
import org.processmining.emdapplications.hfdd.algorithm.iteration.diffcandidates.InterestingVertexFinder;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexCondition;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexConditionType;
import org.processmining.emdapplications.hfdd.data.abstraction.ComparisonAbstraction;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraphFactory;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DDGGraphType;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DiffDecompGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DiffDecompGraphBuilder;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSankeyGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSankeyGraphBuilder;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;
import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.ProbDiffDFG;
import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.ProbDiffDFGBuilder;

public class HFDDIterationManagement {
	private final static Logger logger = LogManager.getLogger( HFDDIterationManagement.class );
	
	/**
	 * HFDD Graph instance that will be the basis for
	 * the iterative comparison.
	 */
	private final HFDDGraph hfddGraph; 
	
	/**
	 * Event data to is going to be compared.
	 */
	private final BiComparisonDataSource<CCCVariantAbstImpl> dataSource;
	
	/**
	 * List of iteration specifications / information.
	 */
	private final LinkedList<HFDDIterationBase<CCCVariantAbstImpl>> hfddIterations;
	
	/**
	 * Iteration counter.
	 */
	private int iterations = 0;
	
	public HFDDIterationManagement(BiComparisonDataSource<CCCVariantAbstImpl> dataSource, 
			HFDDGraph hfddGraph) {
		this.dataSource = dataSource;
		this.hfddGraph = hfddGraph;
		this.hfddIterations = new LinkedList<>();
	}

	/**
	 * Initialize the base iteration (i.e., EMD without LVS abstractions).
	 */
	public void initBaseIteration() {
		// Base Iteration no abstraction applied yet
		HFDDIterationBase<CCCVariantAbstImpl> iterationBase = 
				new HFDDIterationBase<>(this.hfddGraph, 0, dataSource);
		iterationBase.initializeIteration();
		hfddIterations.add(iterationBase);
		try {
			getHfddGraph().applyMeasure(iterationBase.measure, dataSource, true);
		} catch (SLDSTransformerBuildingException e) {
			logger.error("Failed to initialize the base iteration");
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a list of metric values for the given iteration - tuples (set id, metric value).
	 * 
	 * Iterations start at 0 (base iteration).
	 * 
	 * @return List of tuples (set id, metric value)
	 * @throws HFDDIterationException Last measurement data 404
	 */
	public List<VertexMeasurementDTO> getComparisonMetrics(int iteration) throws HFDDIterationException {
		if (iteration > this.iterations) {
			throw new HFDDIterationException("Invalid iteration number! " + iteration + " >= " + 
					this.iterations);
		}
		PerspectiveDescriptor perspDesc = this.hfddIterations.get(iteration).getpDesc();
		List<VertexMeasurementDTO> iterationMetrics = 
			this.getHfddGraph().getMeasurements(perspDesc)
				.stream()
				.filter(p -> p.getRight().getMetric().isPresent())
				.map(p -> new VertexMeasurementDTO(p.getLeft(), p.getRight().getMetric().get()))
				.toList();
		
		// Sanity check
		if (iterationMetrics.size() < 0.9 * this.getHfddGraph().getMeasurements(perspDesc).size()) {
			logger.warn("Suspiciously few defined measurements.");
		}
		// No such recorded measurement
		if (iterationMetrics.size() == 0) {
			throw new HFDDIterationException("No measurement data for iteration " + this.iterations);
		}
		return iterationMetrics;
	}
	
	/**
	 * Get a list of metric values for the last iteration - tuples (set id, metric value).
	 * @return List of tuples (set id, metric value)
	 * @throws HFDDIterationException Last measurement data 404
	 */
	public List<VertexMeasurementDTO> getLastIterationComparisonMetrics() throws HFDDIterationException {
		return this.getComparisonMetrics(this.iterations);
	}
	
	public HFDDIterationBase<CCCVariantAbstImpl> getIteration(int iteration) throws HFDDIterationException {
		// 0th iteration is base
		if (iteration < 0 || iteration > this.iterations) {
			throw new HFDDIterationException("There are " + this.iterations + " but requested was number " + iteration);
		}
		return this.hfddIterations.get(iteration);
	}
	
	public boolean dropLastIteration() {
		if (this.iterations > 0) {
			this.hfddIterations.pollLast();
			this.iterations--;
			return true;
		}
		return false;
	}
	
	/**
	 * Get the dominating vertices from the given iteration that are above the threshold.
	 * 
	 * Iterations start at 0.
	 * 
	 * @param metricThreshold Minimum metric value
	 * @param domMetricThreshold Threshold of metric reduction to build domination chains 
	 * 	(superset's metric must exceed domMetricThreshold (%) quantile of subset's metric) 
	 * @param domSupportThresholdp Threshold of support reduction to build domination chains 
	 * 	(superset's support must exceed domMetricThreshold (%) quantile of subset's support) 
	 * @param domSupportThresholdp Threshold of support reduction to build domination chains

	 * @return
	 * @throws HFDDIterationException 
	 */
	public List<DiffCandidateShortInfo> getDominatingThresholdedItems(int iteration, double metricThreshold, 
			double threshSurprise, double threshDom) throws HFDDIterationException {

		//TODO domSupportThreshold not used
		if (iteration > this.iterations) {
			throw new HFDDIterationException("Invalid iteration number! " + iteration + " >= " + 
					this.iterations);
		}
		final PerspectiveDescriptor perspDesc = this.hfddIterations.get(iteration).getpDesc();

		InterestingVertexFinder finderBase = new InterestingVertexFinder(this, iteration);

		// Find interesting vertices in this iteration
		Collection<DiffCandidate> dominatingVertices = finderBase.findInterestingVertices(metricThreshold, 
				threshSurprise, threshDom);
		
		// Extract vertex short info
		List<DiffCandidateShortInfo> lDominatingVertices = dominatingVertices.stream().map(
				v -> generateShortInfo(getGraph(), v, perspDesc)).toList();
		
		return lDominatingVertices;
	}
	
	/**
	 * Get the dominating vertices from the last iteration that are above the threshold.
	 * 
	 * @param metricThreshold Minimum metric value
	 * @param domMetricThreshold Threshold of metric reduction to build domination chains
	 * @param domSupportThresholdp Threshold of support reduction to build domination chains

	 * @return
	 * @throws HFDDIterationException 
	 */
	public List<DiffCandidateShortInfo> getLastIterationDominatingThresholdedItems(double metricThreshold, 
			double domMetricThreshold, double domSupportThreshold) throws HFDDIterationException {
		
		return this.getDominatingThresholdedItems(iterations, metricThreshold, domMetricThreshold, domSupportThreshold);
	}
	
	/**
	 * Create a cornerstone graph for the given set of cornerstone activity itemsets.
	 * @param cornerStoneVerticesActivities
	 * @return
	 */
	public CSGraph getCornerStoneGraph(String[][] cornerStoneVerticesActivities) {
	
		//  Get cornerstone vertex ids
		List<Integer> cornerstoneVerticesIds = new LinkedList<>();
		for (String[] activities : cornerStoneVerticesActivities) {
			HFDDVertex v = this.getHfddGraph().getVertex(activities);
			if (v == null) {
				logger.error("Requesting a cornerstone vertex that is not element of the graph");
			}
			else {
				cornerstoneVerticesIds.add(v.getId());
			}
		}
		
		return this.getCornerStoneGraph(cornerstoneVerticesIds);
	}
	
	/**
	 * Create a cornerstone graph for the given set of cornerstone activity itemsets.
	 * @param cornerStoneVerticesActivities
	 * @param conditionIteration Iteration from which the subprocesses are used for conditioning.
	 * @return
	 * @throws HFDDIterationException 
	 */
	public CSGraph getCornerStoneGraph(String[][] cornerStoneVerticesActivities, 
			int conditionIteration) throws HFDDIterationException {
	
		//  Get cornerstone vertex ids
		List<Integer> cornerstoneVerticesIds = new LinkedList<>();
		for (String[] activities : cornerStoneVerticesActivities) {
			HFDDVertex v = this.getHfddGraph().getVertex(activities);
			if (v == null) {
				logger.error("Requesting a cornerstone vertex that is not element of the graph");
			}
			else {
				cornerstoneVerticesIds.add(v.getId());
			}
		}
		
		return this.getCornerStoneGraph(cornerstoneVerticesIds, conditionIteration);
	}

	/**
	 * Create a cornerstone graph for the given list of cornerstone vertex ids.
	 * @param cornerStoneVertexSetIds
	 * @return
	 */
	public CSGraph getCornerStoneGraph(List<Integer> cornerstoneVerticesIds) {
		// Create cornerstone graph
		CSGraph csGraph = CSGraphFactory.buildFromHFDDGraph(this.getHfddGraph(), 
				cornerstoneVerticesIds, Optional.empty());
		return csGraph;
	}

	/**
	 * Create a cornerstone graph for the given list of cornerstone vertex ids.
	 * @param cornerStoneVertexSetIds
	 * @param conditionIteration Iteration from which the subprocesses are used for conditioning.
	 * @return
	 * @throws HFDDIterationException 
	 */
	public CSGraph getCornerStoneGraph(List<Integer> cornerstoneVerticesIds, 
			int conditionIteration) throws HFDDIterationException {
		HFDDIterationBase<CCCVariantAbstImpl> hfddIteration = this.getIteration(conditionIteration);
		Optional<ArrayList<Set<VertexCondition>>> conditionBase = hfddIteration.getAggDataCBase(true);
		// Create cornerstone graph
		CSGraph csGraph = CSGraphFactory.buildFromHFDDGraph(this.getHfddGraph(), 
				cornerstoneVerticesIds, conditionBase);
		return csGraph;
	}
	
	/**
	 * Get the intra itemset vertex Sankey flow diagram.
	 * @param itemset Itemset to be considered.
	 * @param iteration Iteration number (Use the data and abstraction from this iteration)
	 * @return Sankey Diagram visualizing the intra vertex flow
	 */
	public CSSankeyGraph getIntraSetFlow(String[] itemset, int iteration) {
		// Create mock cornerstone graph
		String[][] investigateVertexActivities = new String[][] {
			itemset
		};
		CSGraph csGraph = this.getCornerStoneGraph(investigateVertexActivities);
		
		return getSankeyForCornerstoneGraph(csGraph, iteration);
	}
	
	/**
	 * Get the intra itemset vertex Sankey flow diagram.
	 * @param setId Id of the itemset to be considered.
	 * @param iteration Iteration number (Use the data and abstraction from this iteration)
	 * @return Sankey Diagram visualizing the intra vertex flow
	 */
	public CSSankeyGraph getIntraSetFlow(int setId, int iteration) {
		List<Integer> lMock = new LinkedList<>();
		lMock.add(setId);
		CSGraph csGraph = this.getCornerStoneGraph(lMock);
		return getSankeyForCornerstoneGraph(csGraph, iteration);
	}

	public ProbDiffDFG getIntraSetDFG(int setId, int iteration) {
		ProbDiffDFGBuilder dfgBuilder = new ProbDiffDFGBuilder(this.getCategoryMapper());
		HFDDIterationBase<CCCVariantAbstImpl> iterationData = hfddIterations.get(iteration);
		HFDDVertex hfddVertex = hfddGraph.getVertexbyID(setId); 	// Vertex for Id
		
		return dfgBuilder.buildProbDiffDFG(hfddVertex, iterationData.getBaseMeasure(), iterationData.getPreparedCompDS());
	}
	
	/**
	 * 
	 * @param csGraph
	 * @return
	 */
	public CSSankeyGraph getSankeyForCornerstoneGraph(CSGraph csGraph, int iteration) {
		// Initialize the data
		HFDDIterationBase<CCCVariantAbstImpl> iterationData = hfddIterations.get(iteration);
		csGraph.initializeComparisonData(iterationData.getPreparedCompDS());
		
		
		// Build the actual Sankey diagram
		CSSankeyGraphBuilder skBuilder = new CSSankeyGraphBuilder();
		skBuilder.setCsGraph(csGraph).setDataSource(dataSource);
		skBuilder.setItemAbbreviator(s -> s.replace("+complete", ""));
		
		CSSankeyGraph skGraph = skBuilder.build();
		
		return skGraph;
	}
	
	/**
	 * Create the Difference decomposition graph for the provided cornerstone graph.
	 * To this end, the cornerstone graph must be measured against the actual data.
	 * @param csGraph Cornerstone graph
	 * @param ddgType Type of the DDG (e.g., only residual or including vertex conditioning)
	 * @return
	 */
	public DiffDecompGraph getDDGForCornerstoneGraph(CSGraph csGraph, DDGGraphType ddgType) {
		// Initialize the data (from 0th iteration)
		HFDDIterationBase<CCCVariantAbstImpl> iterationData = hfddIterations.get(0);
		csGraph.initializeComparisonData(iterationData.getPreparedCompDS()); 
		
		// Build the actual differenced decomposition graph
		DiffDecompGraphBuilder ddgBuilder = new DiffDecompGraphBuilder();
		ddgBuilder
			.setGraphProbabilityType(ddgType)
			.setCsGraph(csGraph).setItemAbbreviator(s -> s.replace("+complete", ""));

		DiffDecompGraph ddGraph = ddgBuilder.build();
		return ddGraph;
	}
	

	/**
	 * Extract the short info DTO from a vertex and a perspective.
	 * @param v The vertex
	 * @param pDescLastIt Perspective descriptor of the last iteration
	 * @return Data transfer object containing short vertex info on the last iteration
	 */
	private static DiffCandidateShortInfo generateShortInfo(HFDDGraph hfddGraph, DiffCandidate diffCandidate, PerspectiveDescriptor pDescLastIt) {
		int id = diffCandidate.v().getId();
		String[] activities = diffCandidate.v().getVertexInfo().getItemsetHumanReadable();
		Arrays.sort(activities);
		double m = diffCandidate.v().getVertexInfo().getMeasurements().get(pDescLastIt).getMetric().get();
		
		String[] condActivities = null;
		int condId = id;
		if (diffCandidate.condContext().isPresent()) {
			condActivities = diffCandidate.condContext().get().stream()
				.map(u -> u.getVertexInfo().getItemsetHumanReadable())
				.flatMap(Arrays::stream)
				.distinct()
				.sorted()
				.toArray(String[]::new);
			
			BitSet activityUnion = diffCandidate.v().getVertexInfo().getActivitiesCopy();

			diffCandidate.condContext().get().stream()
				.map(u -> u.getVertexInfo().getActivities())
				.forEach(aSet -> activityUnion.or(aSet));
			
			HFDDVertex unionVertex = hfddGraph.getVertex(activityUnion);
			if (unionVertex == null) {
				// TODO only for the error message
				logger.warn("Union vertex " + Arrays.toString(activities) + 
						" + " + Arrays.toString(condActivities) + " does not exist per se!");
				condId = -1;
			}
			else {
				condId = unionVertex.getId();
			}
			
		}
		return new DiffCandidateShortInfo(id, condId, activities, condActivities, m);
	}
	
	/**
	 * Creates a new LVS abstraction and runs the corresponding measurement on the HFDD graph.
	 * @param appliedAbstractions List of abstraction specifications
	 * @throws HFDDIterationException Exception raised when running the iteration fails.
	 */
	public void createRunNextIteration(List<ComparisonAbstraction> appliedAbstractions, 
			Optional<HFDDVertex> condVertex, Optional<VertexConditionType> vertCond, 
			Optional<Double> condMaxPropCoverLoss) throws HFDDIterationException {
		
		if (condVertex.isPresent() && condMaxPropCoverLoss.isEmpty()) {
			logger.error("A condition vertex was provided but no condition threshold was given. "
					+ "Will use default value (0.01)");
			condMaxPropCoverLoss = Optional.of(0.01);
		}
		this.iterations++;		// New iteration

		// Create new iterations
		HFDDIterationRefined<CCCVariantAbstImpl> refinedIteration = 
				new HFDDIterationRefined<CCCVariantAbstImpl>(
						hfddIterations.getLast(), iterations, 
						appliedAbstractions, condVertex, vertCond, condMaxPropCoverLoss);
		hfddIterations.add(refinedIteration);
		
		// Execute the measurement on the HFDD graph 
		refinedIteration.initializeIteration();
		
		try {
			getHfddGraph().applyMeasure(refinedIteration.getBaseMeasure(), 
					refinedIteration.getPreparedCompDS(), true); 
		} catch (SLDSTransformerBuildingException e) {
			e.printStackTrace();
			logger.error("Data source transformation error while"
					+ " addding a new HFDD iteration: {}", e.getMessage());
			throw new HFDDIterationException("Data source transformation: " + e.getMessage());
		}
	}	

	/**
	 * Creates a new LVS abstraction and runs the corresponding measurement on the HFDD graph.
	 * @param appliedAbstractions List of abstraction specifications
	 * @throws HFDDIterationException Exception raised when running the iteration fails.
	 */
	public void createRunNextIteration(List<ComparisonAbstraction> appliedAbstractions) throws HFDDIterationException {
		this.createRunNextIteration(appliedAbstractions, 
				Optional.empty(), Optional.empty(), Optional.empty());
	}	

	/**
	 * Creates a new LVS abstraction and runs the corresponding measurement on the HFDD graph.
	 * @param appliedAbstractions List of abstraction specifications
	 * @throws HFDDIterationException Exception raised when running the iteration fails.
	 */
	public void createRunNextIteration(List<ComparisonAbstraction> appliedAbstractions, 
			List<Integer> condVertex, VertexConditionType conditionType,  double condMaxPropCoverLoss) throws HFDDIterationException {
		
		BitSet bsCondV = new BitSet();
		condVertex.stream().forEach(bsCondV::set);
		
		HFDDVertex v = getHfddGraph().getVertex(bsCondV);
		
		if (v == null) {
			logger.error("Requested condition activity set does not respond to a HFDD vertex.");
			this.createRunNextIteration(appliedAbstractions);
		}
		else {
			this.createRunNextIteration(appliedAbstractions, 
					Optional.of(v), Optional.of(conditionType), Optional.of(condMaxPropCoverLoss));
		}
	}	

	
	public HFDDGraph getGraph() {
		return this.getHfddGraph();
	}
	
	public PerspectiveDescriptor getPerspective4Iteration(int iteration) {
		return this.hfddIterations.get(iteration).getpDesc();
	}
	
	/**
	 * The category mapper used for creating the HFDD Graph.
	 * @return
	 */
	public CategoryMapper getCategoryMapper() {
		return getHfddGraph().getCategoryMapper();
	}
	
	/**
	 * Get the current number of applied iterations.
	 * Initially 0 is returned (refers to the base iteration).
	 * 
	 * @return Current number of applied iterations
	 */
	public int getIterationCount() {
		return this.iterations;
	}

	public HFDDGraph getHfddGraph() {
		return hfddGraph;
	}

	public BiComparisonDataSource<CCCVariantAbstImpl> getDataSource() {
		return dataSource;
	}
	
	public Set<HFDDVertex> getConditionVertices(int iteration, VertexConditionType type) {
		if (iteration > this.iterations) {
			logger.warn("Requested condition vertices for an iteration beyond the limits. "
					+ "Will return condition vertices for max known iteration");
			iteration = this.iterations;
		}

		HFDDIterationBase<CCCVariantAbstImpl> iterationData = hfddIterations.get(iteration);
		return iterationData.getConditionVertices(type);
	}
}
