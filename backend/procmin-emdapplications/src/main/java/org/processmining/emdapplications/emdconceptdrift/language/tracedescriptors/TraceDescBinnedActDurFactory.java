package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.VariantDescriptionConstants;
import org.processmining.emdapplications.data.variantlog.base.VariantKeys;
import org.processmining.emdapplications.data.variantlog.base.VariantPropertyFactory;
import org.processmining.emdapplications.data.xlogutil.statistics.XLogTimeStatistics;
import org.processmining.emdapplications.emdconceptdrift.util.timeclustering.BinEdgeCalculator;

public class TraceDescBinnedActDurFactory extends TraceDescBinnedTimeFactory {
	private final static Logger logger = LogManager.getLogger( TraceDescBinnedActDurFactory.class );

	private final static String KEY_SERVICE_TIME_BINNED = "ServiceTimeBinned";

	public TraceDescBinnedActDurFactory(XEventClassifier classifier, BinEdgeCalculator binCalc) {
		super(classifier, binCalc);
	}

	@Override
	public TraceDescriptor getTraceDescriptor(XTrace trace) {
//		String[] sTrace = new String[trace.size() / 2];
//		int[] indBinned = new int[trace.size() / 2];
		List<String> lTrace = new LinkedList<>();
		List<Integer> lBins = new LinkedList<>();
		{
			int i = 0;
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				else {
					XAttributeMap attributes = event.getAttributes();
					String activity = getClassifier().getClassIdentity(event);
					lTrace.add(activity); 
//					sTrace[i] = activity;
					if(useEventDescriptorInfo() && attributes.containsKey(KEY_SERVICE_TIME_BINNED)) {
						int binIndex = (int) ((XAttributeDiscrete) attributes.get(KEY_SERVICE_TIME_BINNED)).getValue();
//						indBinned[i] = binIndex;
						lBins.add(binIndex);
					}
					else {
						double t = ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
						double[] bins = this.mapBins.get(activity);
						int j = 0;
						while (t > bins[j]) // Assuming that last entry is Double.POSITVE_INFINITY
						   j++;
//						indBinned[i] = j;
						lBins.add(j);
					}
					i++;
				}
			}
		}
//		TraceDescBinnedActDur res = new TraceDescBinnedActDur(sTrace, indBinned);
		String[] sTrace = new String[lTrace.size()];
		lTrace.toArray(sTrace);
		int[] indBinned = new int[lBins.size()];
		int i = 0;
		for(int ind : lBins) {
			indBinned[i] = ind;
			i++;
		}
		TraceDescBinnedActDur res = new TraceDescBinnedActDur(sTrace, indBinned);
		return res; 
	}
	
	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
		TraceDescBinnedActDur traceDesc = (TraceDescBinnedActDur) this.getTraceDescriptor(trace);
		int i = 0;
		for (XEvent event : trace) {
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				int t = traceDesc.getTimes()[i];
				event.getAttributes().put(KEY_SERVICE_TIME_BINNED, new XAttributeDiscreteImpl(KEY_SERVICE_TIME_BINNED, t));
				i++;
			}
		}
	}
	
	

	@Override
	protected Map<String, List<Double>> getTimeMap(XLog xlog) {
		return XLogTimeStatistics.getActivityServiceTimes(xlog, getClassifier());
	}

	@Override
	public boolean isProjectionInvariant() {
		return true;
	}

	@Override
	public TraceDescriptor getEmptyTrace() {
		return new TraceDescBinnedActDur(new String[] {}, new int[] {});
	}

	@Override
	public String getShortDescription() {
		return "CF + Service T. Binned";
	}

	@Override
	public VariantKeys getRequiredVariantInfo() {
		VariantPropertyFactory factory = new VariantPropertyFactory();
		factory.addProperty(VariantDescriptionConstants.TIME_SERV);
		factory.addProperty(VariantDescriptionConstants.TIME_BINNED);
		return factory.build();
	}

	@Override
	public Pair<TraceDescriptor, Integer> getTraceDescriptor(CVariant variant,
			CVariantLog<? extends CVariant> contextLog) {
		// TODO Implement
		throw new RuntimeException("Timed Trace factory cannot handle descriptor creation on variants yet");
	}
	
}
