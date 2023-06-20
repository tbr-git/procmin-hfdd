package org.processmining.emdapplications.emdconceptdrift.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.emdapplications.emdconceptdrift.config.EMDConceptDriftParameters;
import org.processmining.emdapplications.emdconceptdrift.config.EMDTraceComparisonParameters;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedActDur;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedTimeFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.log.utils.XLogBuilder;

public class TimeLogExtractor {
	private final static Logger logger = LogManager.getLogger( TimeLogExtractor.class );
	
	public static XLog addTimeBinFeatures(PluginContext context, XLog xlog, EMDTraceComparisonParameters para, ProMCanceller canceller) {
		logger.info("Start extracting binned log " + para.getDistance().toString() + 
				" and trace descriptor factory " + para.getTraceDescFactory().toString());
		
		TraceDescBinnedTimeFactory langFac = (TraceDescBinnedTimeFactory) para.getTraceDescFactory();
		logger.info("Initializing Trace Descriptor factory...");
		para.getTraceDescFactory().init(xlog);
		
		context.getProgress().setMaximum(xlog.size());
		
		XLogBuilder logBuilder = XLogBuilder.newInstance().startLog("LogTimeBinFeatures");
				
		int i = 0;
		int j = 0;
		int j_comp = 0;
		for(XTrace trace : xlog) {
			TraceDescBinnedActDur desc = (TraceDescBinnedActDur) langFac.getTraceDescriptor(trace);
			int[] bins = desc.getTimes();
			logBuilder.addTrace("Trace " + i);
			XAttributeMap tAttrMap = trace.getAttributes();
			tAttrMap.forEach((key, attr) -> logBuilder.addAttribute(attr));
			j = 0;
			j_comp = 0;
			for(XEvent e : trace) {
				logBuilder.addEvent("Event " + j);
				XAttributeMap eAttrMap = e.getAttributes();
				eAttrMap.forEach((key, attr) -> logBuilder.addAttribute(attr));
				if(((XAttributeLiteralImpl) e.getAttributes().get("lifecycle:transition")).
						getValue().equals("start")) {
					continue;
				}
				else {
					logBuilder.addAttribute("TimeBin", bins[j_comp]);
					j_comp++;
				}
				j++;
			}
			i++;
		}
		
		return logBuilder.build();

	}


}
