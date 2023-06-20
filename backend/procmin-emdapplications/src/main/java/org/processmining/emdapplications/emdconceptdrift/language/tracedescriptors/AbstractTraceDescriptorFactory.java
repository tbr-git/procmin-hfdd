package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.VariantKeys;

import gnu.trove.strategy.HashingStrategy;

public abstract class AbstractTraceDescriptorFactory {
	
	private boolean reuseEventDescriptorInfo = true;

	public abstract TraceDescriptor getTraceDescriptor(XTrace trace);

	public abstract Pair<TraceDescriptor, Integer> getTraceDescriptor(CVariant variant, CVariantLog<? extends CVariant> contextLog);
	
	/**
	 * Classifier used to map events
	 */
	private final XEventClassifier classifier;
	
	public AbstractTraceDescriptorFactory(XEventClassifier classifier) {
		this.classifier = classifier;
	}
	
	public abstract HashingStrategy<TraceDescriptor> getHashingStrat();
	
	public abstract void complementTraceByDescAttributes(XTrace trace);
	
	public void complementLogByDescAttributes(XLog xlog) {
		for(XTrace trace : xlog) {
			complementTraceByDescAttributes(trace);
		}
		addClassifier4DescAttributes(xlog);
	}
	
	public void addClassifier4DescAttributes(XLog xlog) {
		return;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}
	
	public void init(XLog xlog, boolean forcedInit) {
		
	}

	public final void init(XLog xlog) {
		init(xlog, false);
	}
	
	public boolean useEventDescriptorInfo() {
		return reuseEventDescriptorInfo;
	}
	
	public void enableUsingEventDescriptorInfoUsage() {
		reuseEventDescriptorInfo = true;
	}

	public void disableUsingEventDescriptorInfoUsage() {
		reuseEventDescriptorInfo = false;
	}
	
	public abstract boolean isProjectionInvariant();
	
	public boolean doesLogContainInfo4InvariantProjection(XLog xlog) {
		return isProjectionInvariant();
	}
	
	public abstract TraceDescriptor getEmptyTrace();

	public abstract TraceDescriptor getEmptyCVariant(CVariantLog<? extends CVariant> contextLog);
	
	public abstract String getShortDescription();
	
	public abstract VariantKeys getRequiredVariantInfo();
	
}
