package org.processmining.emdapplications.emdconceptdrift.writing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.algorithm.SWEMDComp;
import org.processmining.emdapplications.emdconceptdrift.config.EMDConceptDriftParameters;
import org.processmining.emdapplications.emdconceptdrift.config.EMDTraceComparisonParameters;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.FreqBasedStochasticLanguageImpl;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.log.utils.XLogBuilder;

public class CalcLogEMD {
	private final static Logger logger = LogManager.getLogger( CalcLogEMD.class );

	public static Double calcEMDBetweenLogs(PluginContext context, XLog xlogL, XLog xlogR, EMDTraceComparisonParameters para, ProMCanceller canceller) {
		logger.info("Start computation of EMD between two logs");
		logger.info("Initializing Trace Descriptor factory...");
		XLog xlog = XLogBuilder.newInstance().startLog("Complete").build(); 
		xlog.addAll(xlogL);
		xlog.addAll(xlogR);

		TraceDescDistCalculator trDescDist = para.getDistance();
		AbstractTraceDescriptorFactory langFac = para.getTraceDescFactory();
		langFac.init(xlog);
		
		
		StochasticLanguage langL = FreqBasedStochasticLanguageImpl.convert(xlogL.iterator(), canceller, langFac);
		StochasticLanguage langR = FreqBasedStochasticLanguageImpl.convert(xlogR.iterator(), canceller, langFac);
		
		logger.info("Run emd computation");
		double emd = SWEMDComp.compareWindows(langL, langR, trDescDist);
		logger.info("Final EMD value: "  + emd);
		
		return Double.valueOf(emd);

	}

}
