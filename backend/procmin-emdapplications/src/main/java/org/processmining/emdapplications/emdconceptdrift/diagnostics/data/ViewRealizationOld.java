package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.EMDSolver;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.RealizabilityChecker;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.RealizabilityInfo;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewRealizationMeta;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;

public class ViewRealizationOld extends ViewRealization {

	private final static Logger logger = LogManager.getLogger( ViewRealizationOld.class );
	
	private final Window2OrderedStochLangTransformer langTransformer;
	
	private final DescriptorDistancePair descDistPair;
	
	private Optional<WindowDiagnosticsData> data;

	private final RealizabilityInfo viewRealInfo;

	public ViewRealizationOld(ViewRealizationMeta viewDescription, DescriptorDistancePair descDistPair, Window2OrderedStochLangTransformer langTransformer, 
			WindowDiagnosticsData data) {
		super(viewDescription);
		this.langTransformer = langTransformer;
		this.descDistPair = descDistPair;
		this.data = Optional.of(data);
		this.viewRealInfo = RealizabilityChecker.checkRealizability(viewDescription, descDistPair, langTransformer, data);
	}
	
	@Override
	public boolean isRealizable() {
		return viewRealInfo.isRealizable();
	}

	public void populate() throws ViewDataException {
		if(viewRealInfo.isRealizable()) {
			if(!emdSol.isPresent() && viewRealInfo.isRealizable()) {
				emdSol = Optional.of(EMDSolver.getLPSolution(data.get().getXLogLeft().iterator(), data.get().getXLogRight().iterator(), 
						descDistPair.getDescriptorFactory(), descDistPair.getDistance(), langTransformer));
				// Free for garabage collection -> memory management
				data = Optional.empty();
			}
		}
		else {
			throw new ViewDataException(viewRealInfo);
		}
			
	}

}
