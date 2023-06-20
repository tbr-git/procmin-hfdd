package org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.frequentpattern.connector.FreqEditOpReprSequence;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.FlowDiagnostic;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.MultiViewRealization;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewDataException;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.MultiViewConfig;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
import org.processmining.emdapplications.emdconceptdrift.io.JSONWriter;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.NonZeroFlows;

public class LightWeightDiagnosticsPerspective {

	private final static Logger logger = LogManager.getLogger( LightWeightDiagnosticsPerspective.class );

	private XEventClassifier classifier;
	
	private final MultiViewConfig multiViewConfig;
	
	private final PerspectiveDescriptor description;
	
	private MultiViewRealization multiView;
	
	public LightWeightDiagnosticsPerspective(WindowDiagnosticsData data, MultiViewConfig multiViewConfig, PerspectiveDescriptor description) {
		this.classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		this.description = description;
		this.multiViewConfig = multiViewConfig;
		this.multiView = multiViewConfig.createMultiViewOnData(data, description);
		this.multiView.reduceMemoryConsumption();
		System.gc();
	}


	private EMDSolContainer getTopLevelEMDSol() throws ViewDataException {
		return multiView.getTopLevelViewRealization().getEMDSol();
	}
	
	public void prepareMainView() throws ViewDataException {
		multiView.getTopLevelViewRealization().populate();
	}
	
	public List<FreqEditOpReprSequence> getTopLevelFrequentEditDifferences() {
		return multiView.getTopLevelViewRealization().getFrequentEditSequenceRepresentatives();
	}

	public void initTopLevelFrequentEditDifferences() {
		multiView.getTopLevelViewRealization().mineFrequentEditSequenceRepresentatives();
	}

	public List<FlowDiagnostic> getTopLevelFlowCostDiagnostics() throws ViewDataException {
		EMDSolContainer emdSolContainer = getTopLevelEMDSol();
		NonZeroFlows nonZeroFlows = emdSolContainer.getNonZeroFlows();
		OrderedStochasticLanguage langL = emdSolContainer.getLanguageLeft();
		OrderedStochasticLanguage langR = emdSolContainer.getLanguageRight();
		
		List<FlowDiagnostic> lFlowDiagnostics = new LinkedList<>();
		for(Triple<Integer, Integer, Double> t : nonZeroFlows) {
			TraceDescriptor l = langL.get(t.getLeft());
			TraceDescriptor r = langR.get(t.getMiddle());
			EditSequence editSeq = multiView.getTopLevelViewRealization().getDescDetailedDistPair().
					getDetailedDistance().get_distance_op(l, r);
			FlowDiagnostic f = new FlowDiagnostic.Builder().setEditSequence(editSeq).setFlow(t.getRight()).setTraceLeft(l).setTraceRight(r).build();
			lFlowDiagnostics.add(f);

		}
		return lFlowDiagnostics;
	}
	
	
	public XEventClassifier getClassifier() {
		return this.classifier;
	}
	
	public MultiViewRealization getViewHierachy() {
		return this.multiView;
	}
	
	public MultiViewConfig getViewConfig() {
		return multiViewConfig;
	}
	
	public void writeDiagnosticsInformation(String filename) throws ViewDataException {
		EMDSolContainer emdSolContainer = getTopLevelEMDSol();
		JSONWriter.write_json("C:/temp/EMDCDDiagnosticsTmp/lp.json", emdSolContainer.getJSON());
		NonZeroFlows nonZeroFlows = emdSolContainer.getNonZeroFlows();
		
		
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		
		//TODO All views
		OrderedStochasticLanguage langL = emdSolContainer.getLanguageLeft();
		OrderedStochasticLanguage langR = emdSolContainer.getLanguageRight();
		for(Triple<Integer, Integer, Double> t : nonZeroFlows) {
			TraceDescriptor l = langL.get(t.getLeft());
			TraceDescriptor r = langR.get(t.getMiddle());
			EditSequence editSeq = multiView.getTopLevelViewRealization().getDescDetailedDistPair().
					getDetailedDistance().get_distance_op(l, r);
//			EditSequence editSeq = trDescDist.get_distance_op(l, r);

			JSONObject joEditInfo = new JSONObject();
			joEditInfo.put("traceLeft", l.toJson());
			joEditInfo.put("traceRight", r.toJson());
			joEditInfo.put("flow", t.getRight());
			joEditInfo.put("editOps", editSeq.getJSON());
			
			ja.put(joEditInfo);
		}
		jo.put("flowEditOps", ja);
		JSONWriter.write_json("C:/temp/EMDCDDiagnosticsTmp/flowEditInfo.json", jo);
	}


	public PerspectiveDescriptor getDescription() {
		return description;
	}
}
