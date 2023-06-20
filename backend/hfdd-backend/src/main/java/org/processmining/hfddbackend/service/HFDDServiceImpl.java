package org.processmining.hfddbackend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.hfdd.algorithm.dto.DiffCandidateShortInfo;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexActivitiesDTO;
import org.processmining.emdapplications.hfdd.algorithm.dto.VertexMeasurementDTO;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationException;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagement;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuildingException;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexConditionType;
import org.processmining.emdapplications.hfdd.data.abstraction.ComparisonAbstraction;
import org.processmining.emdapplications.hfdd.data.csgraph.graph.CSGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DDGGraphType;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DDGLayouter;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.DiffDecompGraph;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.sankey.CSSankeyGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;
import org.processmining.emdapplications.hfdd.data.itemsetdiffgraph.ProbDiffDFG;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hfddbackend.dto.CSGraphSpecDTO;
import org.processmining.hfddbackend.model.HFDDRun;
import org.processmining.stochasticawareconformancechecking.cli.FakeContext;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HFDDServiceImpl implements HFDDService {
	private static final Logger logger = LoggerFactory.getLogger(HFDDServiceImpl.class);
	
	@Autowired
	private HFDDRunRepo runRepo;

	@Override
	public UUID createHFDDRun() {
		UUID id = UUID.randomUUID();
		runRepo.saveHFDDRun(id, new HFDDRun());
		
		return id;
	}
	
	@Override
	public boolean loadExampleLogs(UUID hfddRunId) {
		logger.info("Initializing with default logs");
		ClassLoader classLoader = getClass().getClassLoader();

		////////////////////
		// Left Log
		////////////////////
		logger.info("Loading left log");
		File fileLogLeft = new File(classLoader.getResource("log_example_left.xes").getFile());
		
		// Load Left log from xes-file
		XLog logLeft = loadXESFile(fileLogLeft);

		////////////////////
		// Right Log
		////////////////////
		logger.info("Loading right log");
		File fileLogRight = new File(classLoader.getResource("log_example_right.xes").getFile());
		
		// Load Left log from xes-file
		XLog logRight = loadXESFile(fileLogRight);
		logger.info("Done Loading log");
		
		////////////////////
		// Set logs in run
		////////////////////
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload left log since the run id is invalid!");
			return false;
		}
		hfddRun.get().setLeftLog(logLeft);
		hfddRun.get().setRightLog(logRight);

		return true;
	}

	@Override
	public boolean uploadLogLeft(UUID hfddRunId, MultipartFile fileLogLeft) {
		logger.info("Loading left log");
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload left log since the run id is invalid!");
			return false;
		}
		
		XLog logLeft = loadLog(fileLogLeft);
		if (logLeft == null) {
			logger.error("Failed to load the left log.");
			return false;
		}
		else {
			hfddRun.get().setLeftLog(logLeft);
			logger.info("Done loading left log.");
			return true;
		}
	}

	@Override
	public boolean uploadLogRight(UUID hfddRunId, MultipartFile fileLogRight) {
		logger.info("Loading right log");
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return false;
		}
		
		XLog logRight = loadLog(fileLogRight);
		if (logRight == null) {
			logger.error("Failed to load the right log.");
			return false;
		}
		else {
			hfddRun.get().setRightLog(logRight);
			logger.info("Done loading right log.");
			return true;
		}
	}

	@Override
	public boolean initializeComparison(UUID hfddRunId) throws HFDDIterationManagementBuildingException {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return false;
		}
		
		hfddRun.get().initHFDDRun();
		
		return true;
		
	}

	@Override
	public boolean initializeComparison(UUID hfddRunId, Integer freqActMiningTimeMs, Integer targetActISNbr,
			Double targetActISMargin, Integer maxLoopUnroll) throws HFDDIterationManagementBuildingException {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return false;
		}
		Optional<Integer> optLoopUnroll;
		if (maxLoopUnroll == null || maxLoopUnroll <= 0) {
			optLoopUnroll = Optional.empty();
		}
		else {
			optLoopUnroll = Optional.of(maxLoopUnroll);
		}
		hfddRun.get().initHFDDRun(freqActMiningTimeMs, targetActISNbr, targetActISMargin, optLoopUnroll);

		return true;
	}

	@Override
	public Optional<HFDDRun> getHFDDRun(UUID hfddRunId) {
		return runRepo.getHFDDRun(hfddRunId);
	}
	
	@Override
	public boolean addIteration(UUID hfddRunId, List<ComparisonAbstraction> appliedAbstractions) throws HFDDIterationException {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return false;
		}
		
		hfddRun.get().getHfddItMan().get().createRunNextIteration(appliedAbstractions);
		
		return true;
	}

	@Override
	public boolean addIteration(UUID hfddRunId, 
			List<ComparisonAbstraction> appliedAbstractions, 
			List<Integer> condVertex, VertexConditionType conditionType, double maxProbCoverLoss) throws HFDDIterationException {
		
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return false;
		}
		
		hfddRun.get().getHfddItMan().get().createRunNextIteration(appliedAbstractions, 
				condVertex, conditionType, maxProbCoverLoss);
		
		return true;
	}
	
	public boolean dropLastIteration(UUID hfddRunId) {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return false;
		}

		return hfddRun.get().getHfddItMan().get().dropLastIteration(); 
	}

	@Override
	public CategoryMapper getCategoryMapper(UUID hfddRunId) {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return null;
		}
		
		return hfddRun.get().getHfddItMan().get().getCategoryMapper();
	}

	@Override
	public List<VertexMeasurementDTO> getMeasurements(UUID hfddRunId, int iteration) throws HFDDIterationException {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return null;
		}
		
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		return hfddItMan.getComparisonMetrics(iteration);
	}

	@Override
	public List<DiffCandidateShortInfo> getDominatingThresholdedItems(UUID hfddRunId, int iteration,
			DominatingDiffFilteringOptions domOptions) throws HFDDIterationException {

		// Get Handle to the iteration management
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		if (hfddRun.isEmpty()) {
			logger.error("Could not upload right log since the run id is invalid!");
			return null;
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		// Get the dominating vertices
		List<DiffCandidateShortInfo> res = hfddItMan.getDominatingThresholdedItems(iteration, 
				domOptions.metricThreshold(), domOptions.metricSurpriseThreshold(), 
				domOptions.backwardDominationThreshold());

		return res;
	}

	@Override
	public CSSankeyGraph getIntraItemsetFlow(UUID hfddRunId, int iteration, int vertexId) {
		
		// Get Handle to the iteration management
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not create the inter-itemset flow visualiation for vertex {} "
					+ "and iteration {} !", vertexId, iteration);
			return null;
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		// Get the graph
		CSSankeyGraph skGraph = hfddItMan.getIntraSetFlow(vertexId, iteration);
		return skGraph;
	}
	
	@Override
	public ProbDiffDFG getIntraItemsetDFG(UUID hfddRunId, int iteration, int vertexId) {
		
		// Get Handle to the iteration management
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not create the inter-itemset flow visualiation for vertex {} "
					+ "and iteration {} !", vertexId, iteration);
			return null;
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		// Get the graph
		ProbDiffDFG diffDFG = hfddItMan.getIntraSetDFG(vertexId, iteration);
		return diffDFG;
	}

	@Override
	public CSSankeyGraph getCornerStoneGraphVisualization(UUID hfddRunId, List<Integer> cornerstoneVertexIds) {

		// Get Handle to the iteration management
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not create the cornerstone graph visualiation for vertices {} ", 
					cornerstoneVertexIds);
			return null;
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		// Get the graph
		CSGraph csGraph = hfddItMan.getCornerStoneGraph(cornerstoneVertexIds);

		// Measure and color graph using the base/initial data
		// Big difference are colored "red" and will not be ignored
		CSSankeyGraph skGraph = hfddItMan.getSankeyForCornerstoneGraph(csGraph, 0);
		return skGraph;
	}

	@Override
	public DiffDecompGraph getCSGraphVisualizationData(UUID hfddRunId, CSGraphSpecDTO csGSpecDTO) {

		// Get Handle to the iteration management
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not create the cornerstone graph visualiation for run {} ", 
					hfddRunId);
			return null;
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		logger.info("Creating cornerstone graph for spec: {}", csGSpecDTO);
		
		// Get the graph
		CSGraph csGraph = null;
		if (csGSpecDTO.getConditionIteration() > -1) {
			try {
				csGraph = hfddItMan.getCornerStoneGraph(csGSpecDTO.getCornerstoneVertices(), 
						csGSpecDTO.getConditionIteration());
			} catch (HFDDIterationException e) {
				logger.error("Problems with the iteration");
				e.printStackTrace();
				return null;
			}
		}
		else {
			csGraph = hfddItMan.getCornerStoneGraph(csGSpecDTO.getCornerstoneVertices()); 
		}

		DiffDecompGraph ddGraph = null;
		if (csGSpecDTO.getConditionIteration() > -1) {
			ddGraph = hfddItMan.getDDGForCornerstoneGraph(csGraph, DDGGraphType.CONDITIONED_RESIDUAL);
		}
		else {
			ddGraph = hfddItMan.getDDGForCornerstoneGraph(csGraph, DDGGraphType.RESIDUAL);
		}
		////////////////////////////////////////////////////////////
		// Layouting
		////////////////////////////////////////////////////////////
		// Vertex levels
		DDGLayouter.updateLevelValues(ddGraph);
		
		// Intra-EMD flow ordering
		DDGLayouter.layoutEMDFlows(ddGraph);
		
		// Initialize edges for frontend tree-based layouter
		DDGLayouter.updateSetLayoutTreeEdges(ddGraph);

		return ddGraph;
	}

	/**
	 * Load the event log from the uploaded file.
	 * @param fileLog Event log file
	 * @return Parsed event log
	 */
	private XLog loadLog(MultipartFile fileLog) {
		// Create a temp file
		Path tmpPath = null;
		try {
			tmpPath = Files.createTempFile("log", ".xes");
		} catch (IOException e1) {
			e1.printStackTrace();
			logger.error("Failed to create a temp file for uploading the log");
			return null;
		}
		File tmpFile = tmpPath.toFile();
		
		// Write uploaded multipart file to temporary file
		try {
			fileLog.transferTo(tmpFile);
		} catch (Exception e) {
			logger.error("Cancelling log loading: Failed to write the uploaded "
					+ "file to a temporary file: {}", e.getMessage());
			return null;
		}

		// Parse the log from the temporary file
		XLog log = loadXESFile(tmpFile);
		
		// Delete the temporary file
		try {
			Files.deleteIfExists(tmpPath);
		} catch (IOException e) {
			logger.error("Could not delete temporary log file!");
		}
		
		return log;
	}
	
	/**
	 * Load a log from a xes-file.
	 * @param fileXesLog
	 * @return Loaded Xlog;
	 */
	private XLog loadXESFile(File fileXesLog) {
		XLog log = null;
		PluginContext fakeContext = new FakeContext();
		try {
			// Load the xes files
			logger.debug("Loading the log...");
			log = (XLog) new OpenLogFileLiteImplPlugin().importFile(fakeContext, fileXesLog);
		} catch (Exception e) {
			logger.error("Failed to parse the log: {}", e.getMessage());
			return null;
		}
		return log;
		
	}

	@Override
	public List<VertexActivitiesDTO> getActivities(UUID hfddRunId, 
			List<Integer> vertexIds) throws InvalidVertexIdException {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not find the given session " + hfddRunId);
			return null;
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		HFDDGraph hfddGraph = hfddItMan.getGraph();
		
		List<VertexActivitiesDTO> l = vertexIds.stream()
			.map(i -> hfddGraph.getVertexbyID(i))
			.filter(v -> v != null)
			.map(v -> new VertexActivitiesDTO(v.getId(), v.getVertexInfo().getItemsetHumanReadable()))
			.toList();
		if (l.size() != vertexIds.size()) {
			throw new InvalidVertexIdException();
		}

		return l;
	}

	@Override
	public Optional<Integer> getVertexForActivities(UUID hfddRunId, List<Integer> activityIds) {
		Optional<HFDDRun> hfddRun = runRepo.getHFDDRun(hfddRunId);
		
		if (hfddRun.isEmpty()) {
			logger.error("Could not find the given session " + hfddRunId);
			return Optional.empty();
		}
		HFDDIterationManagement hfddItMan = hfddRun.get().getHfddItMan().get();
		
		BitSet activities = new BitSet();
		activityIds.forEach(activities::set);

		
		HFDDVertex v = hfddItMan.getGraph().getVertex(activities);
		if (v == null) {
			return Optional.empty();
		}
		else {
			return Optional.of(v.getId());
		}
		
	}

}

