package org.processmining.hfddbackend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.hfdd.algorithm.dto.DiffCandidateShortInfo;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexActivitiesDTO;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexMeasurementDTO;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationException;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuildingException;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexConditionType;
import org.processmining.emdapplications.hfdd.data.abstraction.ComparisonAbstraction;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DiffDecompGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSankeyGraph;
import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.ProbDiffDFG;
import org.processmining.hfddbackend.dto.CSGraphSpecDTO;
import org.processmining.hfddbackend.model.HFDDRun;
import org.springframework.web.multipart.MultipartFile;

public interface HFDDService {
	
	/**
	 * Create a novel HFDD run.
	 * @return Id of the novel HFDD run
	 */
	public UUID createHFDDRun();

	/**
	 * Initialize the given run with example logs.
	 * 
	 * Example logs should be part of the distribution (e.g., as a resource).
	 * @param hfddRunId
	 * @return True, iff successful
	 */
	public boolean loadExampleLogs(UUID hfddRunId);
	
	/**
	 * Upload and set the left log for the specified run.
	 * @param hfddRunId
	 * @param fileLogLeft
	 * @return
	 */
	public boolean uploadLogLeft(UUID hfddRunId, MultipartFile fileLogLeft);

	/**
	 * Upload and set the right log for the specified run.
	 * @param hfddRunId
	 * @param fileLogRight
	 * @return
	 */
	public boolean uploadLogRight(UUID hfddRunId, MultipartFile fileLogRight);
	
	/**
	 * Initialize the comparison of the specified run.
	 * @param hfddRunId
	 * @return True if succeeded
	 * @throws HFDDIterationManagementBuildingException
	 */
	public boolean initializeComparison(UUID hfddRunId) throws HFDDIterationManagementBuildingException;

	/**
	 * Initialize the comparison of the specified run.
	 * If possible, number of itemsets should be close to the provided target value (i.e., there are sufficient itemsets).
	 * 
	 * @param hfddRunId Run identifier
	 * @param freqActMiningTimeMs Maximum itemset search time (in ms)
	 * @param targetActISNbr Target value for the number of itemsets
	 * @param targetActISMargin Margin around target value
	 * @param maxLoopUnroll Maximum loop unrolling iterations: No unrolling if <0 or null
	 * @return True if succeeded
	 * @throws HFDDIterationManagementBuildingException
	 */
	public boolean initializeComparison(UUID hfddRunId, Integer freqActMiningTimeMs, 
			Integer targetActISNbr, Double targetActISMargin, 
			Integer maxLoopUnroll) throws HFDDIterationManagementBuildingException;
	
	/**
	 * Get the corresponding HFDDRun.
	 * @param hfddRunId
	 * @return
	 */
	public Optional<HFDDRun> getHFDDRun(UUID hfddRunId);
	
	/**
	 * Get the category mapper for the corresponding run.
	 * @param hfddRunId
	 * @return
	 */
	public CategoryMapper getCategoryMapper(UUID hfddRunId);
	
	/**
	 * 
	 * @param hfddRunId
	 * @param appliedAbstractions
	 * @return
	 * @throws HFDDIterationException 
	 */
	public boolean addIteration(UUID hfddRunId, List<ComparisonAbstraction> appliedAbstractions) throws HFDDIterationException;

	/**
	 * 
	 * @param hfddRunId
	 * @param appliedAbstractions
	 * @param condVertex Vertex to condition the iteration on
	 * @return
	 * @throws HFDDIterationException 
	 */
	public boolean addIteration(UUID hfddRunId, List<ComparisonAbstraction> appliedAbstractions, 
			List<Integer> condVertex, VertexConditionType vertexType, double maxProbCoverLoss) throws HFDDIterationException;
	
	public boolean dropLastIteration(UUID hfddRunId);
	
	// TODO Proper dependency to the apache commons tuple
	/**
	 * Get the EMD values for the set vertices for the specified iteration and run.
	 * @param hfddRunId
	 * @param iteration
	 * @return
	 * @throws HFDDIterationException 
	 */
	public List<VertexMeasurementDTO> getMeasurements(UUID hfddRunId, int iteration) throws HFDDIterationException;
	
	/**
	 * Get the list of dominating vertices.
	 * 
	 * @param hfddRunId Run id
	 * @param iteration Iteration number
	 * @param domOptions Domination options (thresholds EMD (minimum) and specifies how domination is "propagated"
	 * @return
	 * @throws HFDDIterationException
	 */
	public List<DiffCandidateShortInfo> getDominatingThresholdedItems(UUID hfddRunId, int iteration, 
			DominatingDiffFilteringOptions domOptions) throws HFDDIterationException;

	/**
	 * Get the (Sankey) flow diagram for the intra-vertex activity sequence mapping.
	 * 
	 * @param hfddRunId Run id
	 * @param iteration Iteration number
	 * @param vertexId Vertex id
	 * @return Sankey flow diagram
	 */
	public CSSankeyGraph getIntraItemsetFlow(UUID hfddRunId, int iteration, int vertexId);

	/**
	 * Get the DFG showing the two intra-vertex activity sequence mappings.
	 * 
	 * @param hfddRunId Run id
	 * @param iteration Iteration number
	 * @param vertexId Vertex id
	 * @return DFG
	 */
	public ProbDiffDFG getIntraItemsetDFG(UUID hfddRunId, int iteration, int vertexId);
	

	/**
	 * Get the visualization of the cornerstone graph induced by the provided cornerstone vertices (ids).
	 * @param hfddRunId Run id
	 * @param cornerstoneVertexIds List of cornerstone vertex ids
	 * @return Sankey diagram-based cornerstone graph visualization
	 */
	public CSSankeyGraph getCornerStoneGraphVisualization(UUID hfddRunId, List<Integer> cornerstoneVertexIds);
	
	/**
	 * Get the visualization of the cornerstone graph induced by the provided cornerstone vertices (ids).
	 * @param hfddRunId Run id
	 * @param csGSpecDTO Specification of the DDG graph
	 * @return Difference decomposition graph
	 */
	public DiffDecompGraph getCSGraphVisualizationData(UUID hfddRunId, CSGraphSpecDTO csGSpecDTO);
	
	/**
	 * Get the activities associated with the provided verticies (provided by their ids)
	 * @param hfddRunId Run id
	 * @param vertexIds List of vertex IDs
	 * @return List of (id, activities) pairs.
	 */
	public List<VertexActivitiesDTO> getActivities(UUID hfddRunId, List<Integer> vertexIds) throws InvalidVertexIdException;
	
	/**
	 * Get the vertex id that is associated with the given activity ids.
	 * @param hfddRunId Run id
	 * @param activities List of activity ids to find the corresponding vertex for
	 * @return Vertex id or empty if there is none
	 */
	public Optional<Integer> getVertexForActivities(UUID hfddRunId, List<Integer> activityIds);

}
