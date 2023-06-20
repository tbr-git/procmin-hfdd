package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.base.CCCVariantImpl;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.VariantDescriptionConstants;
import org.processmining.emdapplications.data.variantlog.base.VariantKeys;

import gnu.trove.strategy.HashingStrategy;

public class BasicTraceDescriptorFactory extends AbstractTraceDescriptorFactory {
	private final static Logger logger = LogManager.getLogger( BasicTraceDescriptorFactory.class );
	private static final Marker TMPSELECT_MARKER = MarkerManager.getMarker("TMPSELECT");
	
	HashingStrategy<TraceDescriptor> hashStrat;

	public BasicTraceDescriptorFactory(XEventClassifier classifier) {
		super(classifier);
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
	public TraceDescriptor getTraceDescriptor(XTrace trace) {
		String[] sTrace = new String[trace.size()];
		{
			int i = 0;
			for (XEvent event : trace) {
				String activity = getClassifier().getClassIdentity(event);
				sTrace[i] = activity;
				i++;
			}
		}
		TraceDescriptor desc = new BasicTrace(sTrace);
		return desc;
	}

	@Override
	public Pair<TraceDescriptor, Integer> getTraceDescriptor(CVariant variant,
			CVariantLog<? extends CVariant> contextLog) {
		return Pair.of(new BasicTraceCC(variant, contextLog.getCategoryMapper()), variant.getSupport());
		
	}

	@Override
	public HashingStrategy<TraceDescriptor> getHashingStrat() {
		return this.hashStrat;
	}


	@Override
	public String toString() {
		return "BasicTraceDescriptorFactory";
	}

	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
		return;
	}

	@Override
	public boolean isProjectionInvariant() {
		return true;
	}

	@Override
	public TraceDescriptor getEmptyTrace() {
		return new BasicTrace(new String[] {});
	}

	@Override
	public String getShortDescription() {
		return "CF";
	}

	@Override
	public VariantKeys getRequiredVariantInfo() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY);
	}

	@Override
	public TraceDescriptor getEmptyCVariant(CVariantLog<? extends CVariant> contextLog) {
		return new BasicTraceCC(new CCCVariantImpl(new int[] {}, 1), contextLog.getCategoryMapper());
	}



}
