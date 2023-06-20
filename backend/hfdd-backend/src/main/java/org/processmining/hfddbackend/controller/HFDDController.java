package org.processmining.hfddbackend.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.time.StopWatch;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.hfdd.algorithm.dto.DiffCandidateShortInfo;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexActivitiesDTO;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexMeasurementDTO;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationException;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuildingException;
import org.processmining.emdapplications.hfdd.data.abstraction.ComparisonAbstraction;
import org.processmining.emdapplications.hfdd.data.abstraction.VertexConditionLVSAbst;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DiffDecompGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSankeyGraph;
import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.ProbDiffDFG;
import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.serialization.ProbDiffDFGFrontendInfo;
import org.processmining.hfddbackend.dto.CSGraphSpecDTO;
import org.processmining.hfddbackend.service.DominatingDiffFilteringOptions;
import org.processmining.hfddbackend.service.HFDDService;
import org.processmining.hfddbackend.service.InvalidVertexIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
//@CrossOrigin(origins = {"http://137.226.117.2:4200", "http://137.226.117.72:4200"})
@CrossOrigin(origins = {"*"})
public class HFDDController {
	private static final Logger logger = LoggerFactory.getLogger(HFDDController.class);
	
	/**
	 * Handle to the service.
	 */
	private final HFDDService hfddService;
	
	@Autowired
	public HFDDController(HFDDService hfddService) {
		this.hfddService = hfddService;
	}
	
	/**
	 * Creates a new empty run.
	 * @return Id of the new run
	 */
	@PostMapping("/createRun")
	public ResponseEntity<UUID> createHFDDRun() {
		UUID id = hfddService.createHFDDRun();
		logger.info("Created HFDD run " + id.toString());
		return ResponseEntity.ok(id);
	}
	
	/**
	 * Load example logs into the run.
	 * @param hfddRunId Id of the run
	 * @return
	 */
	@PostMapping("/{hfddRunId}/loadExampleLogs")
	public ResponseEntity<ResponseMessage> loadExampleLogs(@PathVariable("hfddRunId") UUID hfddRunId) {

		boolean res = hfddService.loadExampleLogs(hfddRunId);

		// Check Success
		if (res) {
			return ResponseEntity.ok(new ResponseMessage("Loaded example logs"));
		}
		else {
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to load example logs"));
		}
	}
	
	/**
	 * Upload the left log to the run with the specified id.
	 * @param hfddRunId Run id
	 * @param file XES-file
	 * @return 
	 */
	@PostMapping(path="/{hfddRunId}/logLeft", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseMessage> uploadLeftLog(@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestParam("logLeft") MultipartFile file) {
		logger.info("Received left log POST request.");
		boolean res = hfddService.uploadLogLeft(hfddRunId, file);

		// Check Success
		if (res) {
			return ResponseEntity.ok(new ResponseMessage("Left log set"));
		}
		else {
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to set left log!"));
		}
	}

	/**
	 * Upload the right log to the run with the specified id.
	 * @param hfddRunId Run id
	 * @param file XES-file
	 * @return 
	 */
	@PostMapping("/{hfddRunId}/logRight")
	public ResponseEntity<ResponseMessage> uploadRightLog(@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestParam("logRight") MultipartFile file) {
		logger.info("Received right log POST request.");
		boolean res = hfddService.uploadLogRight(hfddRunId, file);

		// Check Success
		if (res) {
			return ResponseEntity.ok(new ResponseMessage("Right log set"));
		}
		else {
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to set right log!"));
		}
	}
	
	/**
	 * Initialize the comparison for the run with the provided id.
	 * 
	 * @param hfddRunId Run id
	 * @return True if successful
	 */
	@PostMapping("/{hfddRunId}/initComparison")
	public ResponseEntity<ResponseMessage> initializeComparison(@PathVariable("hfddRunId") UUID hfddRunId) {
		StopWatch stopWatch = StopWatch.createStarted();
		try {
			hfddService.initializeComparison(hfddRunId);
		} catch (HFDDIterationManagementBuildingException e) {
			e.printStackTrace();
			stopWatch.stop();
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to initialize the comparison!"));
		}
		stopWatch.stop();
		logger.info("Initialized comparsion in {}", stopWatch.toString());
		return ResponseEntity.ok(new ResponseMessage("Initialized Comparison."));
	}

	/**
	 * Initialize the comparison for the run with the provided id.
	 * Runs a search for a good number of itemsets by varying the required minimum support.
	 * 
	 * @param hfddRunId Run id
	 * @param freqActMiningTimeMs Maximum time for searching itemsets in ms
	 * @param targetActISNbr Target value for number of itemsets
	 * @param targetActISMargin Margin in which itemsets around the target number are acceptable [(1-margin)*target, (1+margin)*target] 
	 * @return True if successful
	 */
	@PostMapping("/{hfddRunId}/initComparisonISSearch")
	public ResponseEntity<ResponseMessage> initializeComparison(@PathVariable("hfddRunId") UUID hfddRunId,
			@RequestParam(value="freqActMiningTimeMs", required=false, defaultValue="20000") Integer freqActMiningTimeMs,
			@RequestParam(value="targetActISNbr", required=false, defaultValue="1000") Integer targetActISNbr,
			@RequestParam(value="targetActISMargin", required=false, defaultValue="0.1") Double targetActISMargin,
			@RequestParam(value="maxLoopUnroll", required=false, defaultValue="-1") Integer maxLoopUnroll) {

		logger.info("Running session initialization with: "
				+ "Target number of items: {}, margin: {}, Mining time: {}, loop unrolling: {}", 
				targetActISNbr, targetActISMargin, freqActMiningTimeMs, maxLoopUnroll);

		StopWatch stopWatch = StopWatch.createStarted();
		try {
			hfddService.initializeComparison(hfddRunId, freqActMiningTimeMs, 
					targetActISNbr, targetActISMargin, maxLoopUnroll);
		} catch (HFDDIterationManagementBuildingException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to initialize the comparison!"));
		}
		stopWatch.stop();
		logger.info("Initialized itemset-based comparsion in {}", stopWatch.toString());
		return ResponseEntity.ok(new ResponseMessage("Initialized Comparison."));
	}
	
	/**
	 * Get The category mapper for the run.
	 */
	@GetMapping("/{hfddRunId}/getCatMapper")
	public ResponseEntity<CategoryMapper> getCategoryMapper(@PathVariable("hfddRunId") UUID hfddRunId) {
		CategoryMapper catMapper = hfddService.getCategoryMapper(hfddRunId);
		
		if (catMapper == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok(catMapper);
		}
	}

	/**
	 * Get The category mapper for the run.
	 */
	@GetMapping("/{hfddRunId}/{iteration}/getMetricValues")
	public ResponseEntity<List<VertexMeasurementDTO>> getIterationValues(@PathVariable("hfddRunId") UUID hfddRunId, 
			@PathVariable("iteration") int iteration) {
		
		// Try to get the values
		List<VertexMeasurementDTO> values;

		StopWatch stopWatch = StopWatch.createStarted();
		try {
			values = hfddService.getMeasurements(hfddRunId, iteration);
		} catch (HFDDIterationException e) {
			logger.error("Error while retrieving the metric values for run {} - iteration {}", hfddRunId, iteration);
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}

		stopWatch.stop();
		logger.info("Fetched metric values in {}", stopWatch.toString());
		
		// Return the values
		if (values == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok(values);
		}
	}
	
	
	/**
	 * Get the dominating vertices for the iterations and filtering options.
	 * @param hfddRunId Run id
	 * @param iteration Iteration number
	 * @param domOptions Domination filtering options
	 * @return List of dominating vertices
	 */
	@PostMapping("/{hfddRunId}/{iteration}/getDomVertices")
	public ResponseEntity<List<DiffCandidateShortInfo>> getDominatingThresholdedItems(
			@PathVariable("hfddRunId") UUID hfddRunId, 
			@PathVariable("iteration") int iteration,
			@RequestBody DominatingDiffFilteringOptions domOptions) {
		List<DiffCandidateShortInfo> domVertices = null;
		try {

			StopWatch stopWatch = StopWatch.createStarted();
			domVertices = hfddService.getDominatingThresholdedItems(hfddRunId, iteration, domOptions);
			stopWatch.stop();
			logger.info("Computed interesting candidateds in {}", stopWatch.toString());
		} catch (HFDDIterationException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}

		if (domVertices == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok(domVertices);
		}
	}
	
	/**
	 */
	@GetMapping("/{hfddRunId}/{iteration}/getIntraVertexFlow/{vertexId}")
	public ResponseEntity<CSSankeyGraph> getIntraVertexFlow(@PathVariable("hfddRunId") UUID hfddRunId, 
			@PathVariable("iteration") int iteration, @PathVariable("vertexId") int vertexId) {
		
		// Try to get the values
		CSSankeyGraph skGraph;
		skGraph = hfddService.getIntraItemsetFlow(hfddRunId, iteration, vertexId);
		
		// Return the graph
		if (skGraph == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok(skGraph);
		}
	}
	
	@GetMapping("/{hfddRunId}/{iteration}/getIntraVertexDFG/{vertexId}")
	public ResponseEntity<ProbDiffDFGFrontendInfo> getIntraVertexDFG(@PathVariable("hfddRunId") UUID hfddRunId, 
			@PathVariable("iteration") int iteration, @PathVariable("vertexId") int vertexId) {
		
		logger.info("Intra Vertex DFG requested for vertex " + vertexId);
		StopWatch stopWatch = StopWatch.createStarted();
		// Try to get the values
		ProbDiffDFG diffDFG;
		diffDFG = hfddService.getIntraItemsetDFG(hfddRunId, iteration, vertexId);

		stopWatch.stop();
		logger.info("Computed intra-vertex DFG in {}", stopWatch.toString());
		// Return the graph
		if (diffDFG == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			ProbDiffDFGFrontendInfo res = new ProbDiffDFGFrontendInfo(diffDFG);
			return ResponseEntity.ok(res);
		}
	}
	
	@PostMapping("/{hfddRunId}/getCornerstoneGraph")
	public ResponseEntity<CSSankeyGraph> getCornerstoneGraph(
			@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestBody List<Integer> cornerstoneVertices) {

		// Try to get the values
		CSSankeyGraph skGraph;

		StopWatch stopWatch = StopWatch.createStarted();
		skGraph = hfddService.getCornerStoneGraphVisualization(hfddRunId, cornerstoneVertices);
		stopWatch.stop();
		logger.info("Created cornerstone graph {}", stopWatch.toString());
		
		// Return the graph
		if (skGraph == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok(skGraph);
		}
	}

	@PostMapping("/{hfddRunId}/getDDGraph")
	public ResponseEntity<DiffDecompGraph> getDDGraph(
			@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestBody CSGraphSpecDTO csGSpecDTO) {

		// Try to get the values
		DiffDecompGraph ddGraph = null;

		StopWatch stopWatch = StopWatch.createStarted();
		ddGraph = hfddService.getCSGraphVisualizationData(hfddRunId, csGSpecDTO);
		stopWatch.stop();
		logger.info("Created difference-decomposition graph in {}", stopWatch.toString());
		
		// Return the graph
		if (ddGraph == null) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok(ddGraph);
		}
	}

	/**
	 * Create a new iteration for the given run using the provided abstractions.
	 * 
	 * @param hfddRunId Run id
	 * @param abstractions List of abstractions
	 * @return True iff successful
	 */
	@PostMapping("/{hfddRunId}/addIteration")
	public ResponseEntity<ResponseMessage> addIteration(@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestBody List<ComparisonAbstraction> abstractions) {
		logger.info("Request to add an iteration with " + abstractions.size() + " abstractions.");
		try {

			StopWatch stopWatch = StopWatch.createStarted();
			hfddService.addIteration(hfddRunId, abstractions);
			stopWatch.stop();
			logger.info("Created iteration in {}", stopWatch.toString());
		} catch (HFDDIterationException e) {
			e.printStackTrace();
			System.out.println("Adding iteration failed");
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to add iteration!"));
		}
		return ResponseEntity.ok(new ResponseMessage("Successfully added iteration."));
	}

	/**
	 * Create a new iteration for the given run using the provided abstractions.
	 * 
	 * @param hfddRunId Run id
	 * @param abstractions List of abstractions
	 * @return True iff successful
	 */
	@PostMapping("/{hfddRunId}/addIterationCond")
	public ResponseEntity<ResponseMessage> addIterationCond(@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestBody VertexConditionLVSAbst vCondLVSAbst) {
		logger.info("Request to add a conditioned iteration " + vCondLVSAbst.getVertCondType() + " and " + vCondLVSAbst.getAbstractions().size() + " abstractions.");
		try {

			StopWatch stopWatch = StopWatch.createStarted();
			hfddService.addIteration(hfddRunId, vCondLVSAbst.getAbstractions(), 
					vCondLVSAbst.getCondVertex(), vCondLVSAbst.getVertCondType(), 
					vCondLVSAbst.getCondMaxPropCoverLoss());
			stopWatch.stop();
			logger.info("Created iteration in {}", stopWatch.toString());
		} catch (HFDDIterationException e) {
			e.printStackTrace();
			System.out.println("Adding iteration failed");
			return ResponseEntity.internalServerError().body(new ResponseMessage("Failed to add iteration!"));
		}
		return ResponseEntity.ok(new ResponseMessage("Successfully added conditioned iteration."));
	}
	
	/**
	 * Drops the last iteration.
	 * 
	 * @param hfddRunId Run id
	 * @return Message
	 */
	@PostMapping("/{hfddRunId}/dropLastIteration")
	public ResponseEntity<ResponseMessage> dropLastIteration(@PathVariable("hfddRunId") UUID hfddRunId) {
		logger.info("Request to drop last iteration for " + hfddRunId);
		boolean success = hfddService.dropLastIteration(hfddRunId);
		
		if (success) {
			return ResponseEntity.ok(new ResponseMessage("Dropped last iteration."));
		}
		else {
			return ResponseEntity.badRequest().body(new ResponseMessage("Could not drop iteration."));
		}
	}

	/**
	 * Get Activities for vertex id(s).
	 * @param hfddRunId Run id
	 * @param vertexIds List of vertex activity info (id, activities) for the queried vertices
	 * @return List of dominating vertices
	 */
	@PostMapping("/{hfddRunId}/getActivitiesForIds")
	public ResponseEntity<List<VertexActivitiesDTO>> getActivities(
			@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestBody List<Integer> vertexIds) {
		List<VertexActivitiesDTO> res = null;
		try {
			res = this.hfddService.getActivities(hfddRunId, vertexIds);
		} catch (InvalidVertexIdException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(null);
		}
		return ResponseEntity.ok(res);
	}
	
	/**
	 * Get the id of the vertex corresponding to the provided activity IDs.
	 * @param hfddRunId Run id
	 * @param vertexIds List activity IDs
	 * @return Vertex Ids or null if it does not exist.
	 */
	@PostMapping("/{hfddRunId}/getVertexForActivities")
	public ResponseEntity<Integer> getVertexForActivities(
			@PathVariable("hfddRunId") UUID hfddRunId, 
			@RequestBody List<Integer> activityIds) {
		
		Optional<Integer> res = this.hfddService.getVertexForActivities(hfddRunId, activityIds);
	
		if (res.isEmpty()) {
			return ResponseEntity.badRequest().body(null);
		}
		else {
			return ResponseEntity.ok(res.get());
		}
	}
	
}
