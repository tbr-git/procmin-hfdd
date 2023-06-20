package org.processmining.emdapplications.hfdd.data.itemsetdiffgraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguageIterator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstTraceCC;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceCC;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;
import org.processmining.emdapplications.hfdd.algorithm.measure.HFDDVertexMeasurer;
import org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph.LogSide;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;
import org.processmining.emdapplications.hfdd.data.measurement.HFDDMeasurementEMDSol;

public class ProbDiffDFGBuilder {
	private final static Logger logger = LogManager.getLogger( ProbDiffDFGBuilder.class );
	
	/**
	 * Handle to the graph structure that will be filled.
	 */
	private final Graph<ProbDiffDFGVertex, ProbDiffDFGEdge> probDFG;
	
	/**
	 * Category mapper for the activities.
	 */
	private final CategoryMapper cm;
	
	/**
	 * Maximum category code used for normal activities
	 */
	private final int maxActCatCode;

	/**
	 * Binary digits for max activity category code.
	 */
	private final int binCatDigits;

	/** 
	 * Artificial start vertex
	 */
	private final ProbDiffDFGVertexStart vStart;

	/** 
	 * Artificial end vertex
	 */
	private final ProbDiffDFGVertexEnd vEnd;

	/**
	 * Mapping from category code to activity vertex
	 */
	private final Map<Integer, ProbDiffDFGVertexActivity> mapActVert;
	
	/**
	 * Mapping from edge id to edge 
	 * edgeID: categoryCodeLeftVertex|categoryCodeRight vertex
	 */
	private final Map<Integer, ProbDiffDFGEdge> mapEdge;
	
	public ProbDiffDFGBuilder(CategoryMapper cm) {
		this.cm = cm;
		this.probDFG = new DefaultDirectedGraph<>(ProbDiffDFGEdge.class);		// Empty graph
		this.maxActCatCode = cm.getMaxCategoryCode();
		this.vStart = new ProbDiffDFGVertexStart(maxActCatCode + 1);
		this.vEnd = new ProbDiffDFGVertexEnd(maxActCatCode + 2);
		this.binCatDigits = 32 - Integer.numberOfLeadingZeros(maxActCatCode + 2 - 1) + 1;
		
		this.mapActVert = new HashMap<>();
		this.mapEdge = new HashMap<>();
	}
	
	public<T extends CVariant> ProbDiffDFG buildProbDiffDFG(final HFDDVertex v, 
			final HFDDVertexMeasurer<T> measure, final BiComparisonDataSource<T> biCompDS) {

		HFDDMeasurementEMDSol measurement = measure.measureVertexDetails(v, biCompDS, false);
		
		if (measurement.getEMDSolution().isEmpty()) {
			throw new RuntimeException("Probabilistic Difference DFG creation failed. Failed to get EMD solution!.");
		}
		else {
			return this.buildProbDiffDFG(measurement.getEMDSolution().get());
		}
	}
	
	public ProbDiffDFG buildProbDiffDFG(EMDSolContainer emdSol) {
		//////////////////////////////
		// Add Traces
		//////////////////////////////
		
		probDFG.addVertex(vStart);
		probDFG.addVertex(vEnd);
		addStochLang(emdSol.getLanguageLeft(), LogSide.LEFT);
		addStochLang(emdSol.getLanguageRight(), LogSide.RIGHT);
		
		return new ProbDiffDFG(probDFG);
		
	}
	
	private void addStochLang(OrderedStochasticLanguage lang, LogSide logSide) {
		StochasticLanguageIterator it = lang.iterator();
		int i = 0;
		while(it.hasNext()) {
			TraceDescriptor traceDesc = it.next();
			double p = it.getProbability();
		
			////////////////////
			// Digest Trace
			////////////////////
			addTrace(logSide, traceDesc, (float) p);
		}
	}
	
	private void addTrace(LogSide logSide, TraceDescriptor traceDesc, float p) {

		// Init last activity with artificial start
		ProbDiffDFGVertex lastAct = vStart;
		for(int i = 0; i < traceDesc.length(); i++) {
			
			//////////////////////////////
			// Init activity Id
			//////////////////////////////
			// Array of trace element's string representation
			String activity = traceDesc.toString(i);
			int categoryCode;
			if (traceDesc instanceof BasicTraceCC descCat) {
				categoryCode = descCat.getTraceCategories()[i];
			}
			else if (traceDesc instanceof AbstTraceCC descAbstCat) {
				categoryCode = descAbstCat.getTrace()[i];
			}
			else {
				categoryCode = this.cm.getCategory4Activity(activity);
			}

			//////////////////////////////
			// Vertex Update
			//////////////////////////////
			// Get or Init Activity Vertex
			ProbDiffDFGVertexActivity curAct = null;
			curAct = mapActVert.get(categoryCode);
			if (curAct == null) {
				curAct = new ProbDiffDFGVertexActivity(categoryCode, activity, 0f, 0f);
				this.probDFG.addVertex(curAct);
				mapActVert.put(categoryCode, curAct);
			}
			switch (logSide) {
				case LEFT:
					curAct.incProbabilityLeft(p);
					break;
				case RIGHT:
					curAct.incProbabilityRight(p);
					break;
				default:
					break;
			}
			
			//////////////////////////////
			// Edge Update
			//////////////////////////////
			int edgeId = lastAct.getCategoryCode() << (this.binCatDigits + 1) | curAct.getCategoryCode();
			ProbDiffDFGEdge e = null;
			
			e = mapEdge.get(edgeId);
			if (e == null) {
				e = new ProbDiffDFGEdge(edgeId);
				mapEdge.put(edgeId, e);
				this.probDFG.addEdge(lastAct, curAct, e);
			}

			switch (logSide) {
				case LEFT:
					e.incProbabilityLeft(p);
					break;
				case RIGHT:
					e.incProbabilityRight(p);
					break;
				default:
					break;
			}
			
			// Last vertex = current vertex
			lastAct = curAct;
		}

		//////////////////////////////
		// Last Edge Update
		//////////////////////////////
		int edgeId = lastAct.getCategoryCode() << (this.binCatDigits + 1) | vEnd.getCategoryCode();
		ProbDiffDFGEdge e = null;
		
		e = mapEdge.get(edgeId);
		if (e == null) {
			e = new ProbDiffDFGEdge(edgeId);
			mapEdge.put(edgeId, e);
			this.probDFG.addEdge(lastAct, vEnd, e);
		}
		switch (logSide) {
			case LEFT:
				e.incProbabilityLeft(p);
				break;
			case RIGHT:
				e.incProbabilityRight(p);
				break;
			default:
				break;
		}
		
	}
	
}
