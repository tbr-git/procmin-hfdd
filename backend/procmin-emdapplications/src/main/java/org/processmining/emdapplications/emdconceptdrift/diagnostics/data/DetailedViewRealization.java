package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import java.util.List;
import java.util.Optional;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.frequentpattern.connector.FreqEditOpReprSequence;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.frequentpattern.connector.MineFreqEditDiffSeqWrapper;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.DescriptorDetailedDistancePair;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewRealizationMeta;
import org.processmining.emdapplications.emdconceptdrift.language.OrderedStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;

public class DetailedViewRealization extends ViewRealizationOld {
	
	private final DescriptorDetailedDistancePair descDetailedDistPair;
	
	private Optional<List<FreqEditOpReprSequence>> freqEditSeqRepresentative;
	
	public DetailedViewRealization(ViewRealizationMeta viewDescription, DescriptorDetailedDistancePair descDetailedDistPair, Window2OrderedStochLangTransformer langTransformer, WindowDiagnosticsData data) {
		super(viewDescription, descDetailedDistPair, langTransformer, data);
		this.descDetailedDistPair = descDetailedDistPair;
		this.freqEditSeqRepresentative = Optional.empty();
	}
	
	public DescriptorDetailedDistancePair getDescDetailedDistPair() {
		return descDetailedDistPair;
	}
	
	public List<FreqEditOpReprSequence> getFrequentEditSequenceRepresentatives() {
		if(!freqEditSeqRepresentative.isPresent()) {
			mineFrequentEditSequenceRepresentatives();
		}
		return freqEditSeqRepresentative.get();
	}
	
	public void mineFrequentEditSequenceRepresentatives() {
		EMDSolContainer emdSol = null ; 
		try {
			emdSol = this.getEMDSol(); 
		} catch (ViewDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OrderedStochasticLanguage langL = emdSol.getLanguageLeft();
		OrderedStochasticLanguage langR = emdSol.getLanguageRight();
		freqEditSeqRepresentative = Optional.of(MineFreqEditDiffSeqWrapper.mineFreqEditDiffSeqOnNzFlows(emdSol.getNonZeroFlows(), langL, langR, descDetailedDistPair));
		
	}

}
