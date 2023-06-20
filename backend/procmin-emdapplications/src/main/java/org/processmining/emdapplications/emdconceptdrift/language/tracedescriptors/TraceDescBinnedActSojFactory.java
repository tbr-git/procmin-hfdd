package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.VariantDescriptionConstants;
import org.processmining.emdapplications.data.variantlog.base.VariantKeys;
import org.processmining.emdapplications.data.variantlog.base.VariantPropertyFactory;
import org.processmining.emdapplications.data.xlogutil.statistics.XLogTimeStatistics;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.logclassifier.SojournTimeBinnedClassifier;
import org.processmining.emdapplications.emdconceptdrift.util.timeclustering.BinEdgeCalculator;

public class TraceDescBinnedActSojFactory extends TraceDescBinnedTimeFactory {
	private final static Logger logger = LogManager.getLogger( TraceDescBinnedActSojFactory.class );

	public final static String KEY_SOJOURN_TIME_BINNED = "SojournTimeBinned";

	private static final Marker TMPSELECT_MARKER = MarkerManager.getMarker("TMPSELECT");

	public TraceDescBinnedActSojFactory(XEventClassifier classifier, BinEdgeCalculator binCalc) {
		super(classifier, binCalc);
	}


	@Override
	public TraceDescriptor getTraceDescriptor(XTrace trace) {
		ArrayList<String> sTrace = new ArrayList<>(trace.size());
		ArrayList<Integer> indBinned = new ArrayList<>(trace.size());
		StringBuilder builder = new StringBuilder();
		double t, t_soj, t_last = -1;
		for (XEvent event : trace) {
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				XAttributeMap attributes = event.getAttributes();
				String activity = getClassifier().getClassIdentity(event);
				sTrace.add(activity);
				if(useEventDescriptorInfo() && attributes.containsKey(KEY_SOJOURN_TIME_BINNED)) {
					int binIndex = (int) ((XAttributeDiscrete) attributes.get(KEY_SOJOURN_TIME_BINNED)).getValue();
					indBinned.add(binIndex);
					t_last = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
				}
				else {
					t = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValueMillis() / 1000;
					if(t_last < 0) {
						t_last = t;
					}
					t_soj = t - t_last;
					builder.append(", " + t_soj);
					double[] bins = this.mapBins.get(activity);
					int j = 0;
					while (t_soj > bins[j]) // Assuming that last entry is Double.POSITVE_INFINITY
					   j++;
					indBinned.add(j);
					t_last = t;
				}
			}
		}
		TraceDescBinnedActDur res = new TraceDescBinnedActDur(sTrace.toArray(new String[sTrace.size()]), 
				indBinned.stream().mapToInt(Integer::intValue).toArray());
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
				event.getAttributes().put(KEY_SOJOURN_TIME_BINNED, new XAttributeDiscreteImpl(KEY_SOJOURN_TIME_BINNED, t));
				i++;
			}
		}
	}
		
	
	@Override
	protected Map<String, List<Double>> getTimeMap(XLog xlog) {
		return XLogTimeStatistics.getActivitySojournTimes(xlog, getClassifier());
	}


	@Override
	public void addClassifier4DescAttributes(XLog xlog) {
		super.addClassifier4DescAttributes(xlog);
		xlog.getClassifiers().add(new SojournTimeBinnedClassifier(KEY_SOJOURN_TIME_BINNED));
	}


	@Override
	public boolean isProjectionInvariant() {
		return false;
	}


	@Override
	public boolean doesLogContainInfo4InvariantProjection(XLog xlog) {
		return xlog.getClassifiers().contains(new SojournTimeBinnedClassifier(KEY_SOJOURN_TIME_BINNED));
	}


	@Override
	public TraceDescriptor getEmptyTrace() {
		return new TraceDescBinnedActDur(new String[] {}, new int[] {});
	}

	@Override
	public String getShortDescription() {
		return "CF + Sojourn T. Binned";
	}
	
	@Override
	public VariantKeys getRequiredVariantInfo() {
		VariantPropertyFactory factory = new VariantPropertyFactory();
		factory.addProperty(VariantDescriptionConstants.TIME_SOJ);
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
