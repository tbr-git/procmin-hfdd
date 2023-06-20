package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.util.optional.JavaxScriptRunner;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.VariantDescriptionConstants;
import org.processmining.emdapplications.data.variantlog.base.VariantKeys;
import org.processmining.emdapplications.data.variantlog.base.VariantPropertyFactory;
import org.processmining.emdapplications.emdconceptdrift.util.MathUtil;

import gnu.trove.strategy.HashingStrategy;

public class TimedTraceDescriptorFactory extends AbstractTraceDescriptorFactory {
	private final static Logger logger = LogManager.getLogger( TimedTraceDescriptorFactory.class );

	private final static String KEY_SOJOURN_TIME = "SojournTime";

	private final static String KEY_SERVICE_TIME = "ServiceTime";
	
	private HashingStrategy<TraceDescriptor> hashStrat;
	
	private double normAccDuration = -1;
	
	private double normSojourn = -1;
	
	private final TimeBinType timeBinType;

	public TimedTraceDescriptorFactory(XEventClassifier classifier, TimeBinType timeBinType) {
		super(classifier);
		this.timeBinType = timeBinType;
		normAccDuration = 1;
		this.hashStrat = new HashingStrategy<TraceDescriptor>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(TraceDescriptor object) {
				return object.hashCode();
			}

			public boolean equals(TraceDescriptor o1, TraceDescriptor o2) {
				return o1.equals(o2);
			}
		};
	}

	@Override
	public HashingStrategy<TraceDescriptor> getHashingStrat() {
		return this.hashStrat;
	}

	@Override
	public TraceDescriptor getTraceDescriptor(XTrace trace) {
		if(timeBinType == TimeBinType.DURATION) {
			return getTraceDescriptorDuration(trace);
		}
		else {
			return getTraceDescriptorSojourn(trace);
		}
	}
	
	private TraceDescriptor getTraceDescriptorDuration(XTrace trace) {
		String[] sTrace = new String[trace.size() / 2];
		double tActivity = 0;
		double[] tStart = new double[trace.size() / 2];
		{
			int i = 0;
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				else {
					XAttributeMap attributes = event.getAttributes();
					sTrace[i] = getClassifier().getClassIdentity(event);
					double t = ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
					tActivity += t;
					if(useEventDescriptorInfo() && attributes.containsKey(KEY_SERVICE_TIME)) {
						tStart[i] = ((XAttributeContinuous) attributes.get(KEY_SERVICE_TIME)).getValue();
					}
					else {
						tStart[i] = tActivity / normAccDuration;
					}
					i++;
				}
			}
		}
		return new TimedTrace(sTrace, tStart);
	}

	private TraceDescriptor getTraceDescriptorSojourn(XTrace trace) {
		String[] sTrace = new String[trace.size() / 2];
		double tStart = -1;
		double[] tComplete = new double[trace.size() / 2];
		double t;
		{
			int i = 0;
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				XAttributeMap attributes = event.getAttributes();
				if(tStart < 0) {
					tStart = ((XAttributeTimestamp) attributes.get("time:timestamp")).getValueMillis() / 1000;
				}
				sTrace[i] = getClassifier().getClassIdentity(event);
				if(useEventDescriptorInfo() && attributes.containsKey(KEY_SOJOURN_TIME)) {
					tComplete[i] = ((XAttributeContinuous) attributes.get(KEY_SOJOURN_TIME)).getValue();
				}
				else {
					t = ((XAttributeTimestamp) attributes.get("time:timestamp")).getValueMillis() / 1000;
					tComplete[i] = (t - tStart) / normSojourn;
				}
				i++;
			}
		}
		return new TimedTrace(sTrace, tComplete);
	}
	
	@Override
	public void init(XLog xlog, boolean forcedInit) {
		super.init(xlog);
		logger.info("Running initialization of TimedTraceDescriptorFactory...");
		if(timeBinType == TimeBinType.DURATION) {
			initDuration(xlog);
		}
		else {
			initSojourn(xlog);
		}
		logger.info("Initialization done.");
	}
	
	public void initDuration(XLog xlog) {
		logger.info("Running initialization for binning based on activity duration");
		double[] arrTraceAccActDur = new double[xlog.size()];
		int i = 0;
		for (XTrace trace : xlog) {
			for (XEvent event : trace) {
				if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
					continue;
				}
				arrTraceAccActDur[i] += ((XAttributeContinuousImpl) event.getAttributes().get("@@duration")).getValue();
			}
			i++;
		}
		normAccDuration = MathUtil.getPercentile(arrTraceAccActDur, 0.975);
	}

	public void initSojourn(XLog xlog) {
		logger.info("Running initialization for binning based on activity sojourn time");
		double[] arrTraceLen = new double[xlog.size()];
		int i = 0;
		for (XTrace trace : xlog) {
			XEvent eventStart = null;
			Iterator<XEvent> itFindFirstComplete = trace.iterator();
			while(itFindFirstComplete.hasNext() &&
					XLifecycleExtension.instance().extractStandardTransition(eventStart = itFindFirstComplete.next()).compareTo(StandardModel.COMPLETE) != 0) {
			}
			//TODO Handle incomplete logs with start at the end
			XEvent eventEnd = trace.get(trace.size() - 1);
			
			double tStart = ((XAttributeTimestamp) eventStart.getAttributes().get(
					"time:timestamp")).getValueMillis() / 1000;
			double tEnd = ((XAttributeTimestamp) eventEnd.getAttributes().get(
					"time:timestamp")).getValueMillis() / 1000;
			
			arrTraceLen[i] = tEnd - tStart;
			i++;
		}
		normSojourn = MathUtil.getPercentile(arrTraceLen, 0.975);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("TimedTraceDescriptorFactory");
		return builder.toString();
	}

	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
		TimedTrace traceDesc = (TimedTrace) this.getTraceDescriptor(trace);
		int i = 0;
		for (XEvent event : trace) {
			if(XLifecycleExtension.instance().extractStandardTransition(event).compareTo(StandardModel.COMPLETE) != 0) {
				continue;
			}
			else {
				double t = traceDesc.getTimes()[i];
				if(timeBinType == TimeBinType.DURATION) {
					event.getAttributes().put(KEY_SERVICE_TIME, new XAttributeContinuousImpl(KEY_SERVICE_TIME, t));
				}
				else if(timeBinType == TimeBinType.SOJOURN) {
					event.getAttributes().put(KEY_SOJOURN_TIME, new XAttributeContinuousImpl(KEY_SOJOURN_TIME, t));
				}
				else {
					logger.error("Unsupported Time Descriptor Type");
				}
				i++;
			}
		}
	}

	@Override
	public boolean isProjectionInvariant() {
		switch(timeBinType) {
			case DURATION:
				return true;
			case SOJOURN:
				return false;
			default:
				return false;
		}
	}

	@Override
	public TraceDescriptor getEmptyTrace() {
		return new TimedTrace(new String[] {}, new double[] {});
	}
	
	@Override
	public TraceDescriptor getEmptyCVariant(CVariantLog<? extends CVariant> contextLog) {
		//TODO 
		throw new RuntimeException("No empty variant available for TimedTraceDescriptors");
	}
	

	@Override
	public String getShortDescription() {
		return "CF + Cont Time";
	}

	@Override
	public VariantKeys getRequiredVariantInfo() {
		VariantPropertyFactory factory = new VariantPropertyFactory();
		switch(timeBinType) {
			case DURATION:
				factory.addProperty(VariantDescriptionConstants.TIME_SERV);
			case SOJOURN:
				factory.addProperty(VariantDescriptionConstants.TIME_SOJ);
		}
		factory.addProperty(VariantDescriptionConstants.TIME_CONT);
		return factory.build();
	}

	@Override
	public Pair<TraceDescriptor, Integer> getTraceDescriptor(CVariant variant,
			CVariantLog<? extends CVariant> contextLog) {
		// TODO Implement
		throw new RuntimeException("Timed Trace factory cannot handle descriptor creation on variants yet");
	}


}
