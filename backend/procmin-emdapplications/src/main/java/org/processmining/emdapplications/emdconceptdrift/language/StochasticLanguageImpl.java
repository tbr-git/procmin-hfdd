package org.processmining.emdapplications.emdconceptdrift.language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.json.JSONArray;
import org.json.JSONObject;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
import org.processmining.framework.plugin.ProMCanceller;

import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TObjectFloatMap;

public class StochasticLanguageImpl implements StochasticLanguage {

	private final static Logger logger = LogManager.getLogger( StochasticLanguageImpl.class );

	/**
	 * Mapping from trace to its total frequency
	 */
	protected TObjectFloatMap<TraceDescriptor> sLog;
	
	/**
	 * Total weight over all traces
	 */
	protected double totalWeight;
	
	/**
	 * Factory that generates trace descriptors and provides a HashingStrategy for these
	 */
	protected AbstractTraceDescriptorFactory traceDescFac;
	
	/**
	 * Absolute number of traces on which this stochastic language is derived
	 */
	protected int absoluteNbrTraces;
	
	/**
	 * Constructor
	 * 
	 * @param sLog Mapping from traces to total frequency
	 * @param totalWeight Total weight over all traces
	 */
	protected StochasticLanguageImpl(TObjectFloatMap<TraceDescriptor> sLog, double totalWeight, int absoluteNbrOfTraces, AbstractTraceDescriptorFactory traceDescFac) {
		this.sLog = sLog;
		this.totalWeight = totalWeight;
		this.traceDescFac = traceDescFac;
		this.absoluteNbrTraces = absoluteNbrOfTraces;
	}
	
	
	

	/**
	 * Implementation based on {@link org.processmining.earthmoversstochasticconformancechecking.algorithms.XLog2StochasticLanguage#convert(XLog, XEventClassifier, ProMCanceller)}.
	 */
	@Override
	public StochasticLanguageIterator iterator() {
		double normalizationWeight = this.totalWeight;
		TObjectFloatIterator<TraceDescriptor> it = sLog.iterator();
		return new StochasticLanguageIterator() {

			public TraceDescriptor next() {
				it.advance();
				return it.key();
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public double getProbability() {
				return it.value() / normalizationWeight;
			}
		};
	}

	@Override
	public int getNumberOfTraceVariants() {
		return sLog.size();
	}

	@Override
	public String toString() {
		String strSLog = "";
		//accessing keys/values through an iterator:
		for (StochasticLanguageIterator it = iterator(); it.hasNext(); ) {
			strSLog += it.next().toString();
			strSLog += ": ";
			strSLog += String.valueOf(it.getProbability());
			strSLog += "\n";
		}
		return "FreqBasedStochasticLanguage [sLog=" + strSLog + ", totalWeight=" + totalWeight + "]";
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		for (StochasticLanguageIterator it = this.iterator(); it.hasNext(); ) {
//		for (TObjectFloatIterator<TraceDescriptor> it = sLog.iterator(); it.hasNext(); ) {
			TraceDescriptor desc = it.next();
			JSONObject tmp = new JSONObject();
			tmp.put("Variant", desc.toJson());
			tmp.put("Count", sLog.get(desc));
			tmp.put("Probability", it.getProbability());
			ja.put(tmp);
		}
		jo.put("Traces", ja);
		return jo;
	}
	
	@Override
	public double getTotalWeight() {
		return this.totalWeight;
	}
	
	@Override
	public double getProbability(TraceDescriptor traceDesc) {
		if(sLog.containsKey(traceDesc)) {
			return sLog.get(traceDesc) / this.totalWeight;
			
		}
		else {
			return 0;
		}
	}
	
	@Override
	public boolean contains(TraceDescriptor traceDescriptor) {
		return sLog.containsKey(traceDescriptor);
	}


	@Override
	public int getAbsoluteNbrOfTraces() {
		return absoluteNbrTraces;
	}
	
	

}
