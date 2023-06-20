package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.data.variantlog.abstraction.CVariantAbst;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapperExtensible;
import org.processmining.emdapplications.data.variantlog.base.VariantDescriptionConstants;
import org.processmining.emdapplications.data.variantlog.base.VariantKeys;

import gnu.trove.strategy.HashingStrategy;

public class AbstTraceBuilder extends AbstractTraceDescriptorFactory {

	/**
	 * Logger
	 */
	private final static Logger logger = LogManager.getLogger( AbstTraceBuilder.class );

	/**
	 * Hashing strategy for the generated trace descriptors.
	 */
	private HashingStrategy<TraceDescriptor> hashStrat;
	
	/**
	 * Fallback mapper that is initialized on the fly.
	 */
	private CategoryMapperExtensible cmFallback;
	
	public AbstTraceBuilder(XEventClassifier classifier) {
		super(classifier);
		cmFallback = new CategoryMapperExtensible();

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
		int[] catTrace = new int[trace.size()];
		int[][] abstractions = new int[trace.size()][];
		{
			int i = 0;
			for (XEvent event : trace) {
				String activity = getClassifier().getClassIdentity(event);
				catTrace[i] = cmFallback.getCategory4ActivityOrAdd(activity);
				i++;
			}
		}
		TraceDescriptor desc = new AbstTraceCC(catTrace, abstractions, this.cmFallback);
		return desc;
	}
	
	@Override
	public Pair<TraceDescriptor, Integer> getTraceDescriptor(CVariant variant,
			CVariantLog<? extends CVariant> contextLog) {
		if(!(variant instanceof CVariantAbst)) {
			logger.error("Abstract trace descriptor factory requires abstract variants as input");
		}
		//TODO Better use generic factories?
		return Pair.of(new AbstTraceCC((CVariantAbst) variant, 
				contextLog.getCategoryMapper()), variant.getSupport());
		
	}

	@Override
	public HashingStrategy<TraceDescriptor> getHashingStrat() {
		return this.hashStrat;
	}

	@Override
	public void complementTraceByDescAttributes(XTrace trace) {
	}

	@Override
	public boolean isProjectionInvariant() {
		return true;
	}

	@Override
	public TraceDescriptor getEmptyTrace() {
		return new AbstTraceCC(new int[0], null, null);
	}

	@Override
	public TraceDescriptor getEmptyCVariant(CVariantLog<? extends CVariant> contextLog) {
		return new AbstTraceCC(new int[0], new int[0][], null);
	}

	@Override
	public String getShortDescription() {
		return "ACF";
	}

	@Override
	public VariantKeys getRequiredVariantInfo() {
		return new VariantKeys(VariantDescriptionConstants.ACTIVITY 
				| VariantDescriptionConstants.ABSTRACTIONS);
	}

}
