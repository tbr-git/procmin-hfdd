package org.processmining.emdapplications.emdconceptdrift.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.GroundDistances;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.EdgeCalculatorType;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimeBinType;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimedTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedActDurFactory;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedActSojFactory;
import org.processmining.emdapplications.emdconceptdrift.util.timeclustering.BinEdgeCalculator;
import org.processmining.emdapplications.emdconceptdrift.util.timeclustering.KMeansEdgeCalculator;
import org.processmining.emdapplications.emdconceptdrift.util.timeclustering.PercentileEdgeCalculator;
import org.processmining.models.causalgraph.XEventClassifierAwareSimpleCausalGraph;

public class EMDTraceCompParamBuilder {
	private final static Logger logger = LogManager.getLogger( EMDTraceCompParamBuilder.class );
	private XEventClassifier classifier;

	private GroundDistances distName;
	
	private AbstractTraceDescriptorFactory traceDescFactory = null;
	
	private TraceDescDistCalculator distanceCalculator = null;
	
	private int[] binQuantiles = null;
	
	private TimeBinType timeBinType = TimeBinType.DURATION;
	
	private EdgeCalculatorType edgeCalcType = EdgeCalculatorType.PERCENTILE;
	
	private int nCluster = -1;
	

	public EMDTraceCompParamBuilder() {
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		distName = GroundDistances.LEVENSTHEIN;
	}

	public EMDTraceCompParamBuilder setDistance(GroundDistances dist) {
		this.distName = dist;
		return this;
	}
	
	public GroundDistances getDistance() {
		return this.distName;
	}
	
	public EMDTraceComparisonParameters build() {
		// Default values
		switch(this.getDistName()) {
		case LEVENSTHEIN:
			this.setTraceDescFactory(new BasicTraceDescriptorFactory(this.getClassifier()));
			break;
		case TIMEBINNEDLVS:
			BinEdgeCalculator edgeCalc = null;
			switch(this.edgeCalcType) {
			case PERCENTILE:
				logger.info("Use percentile clustering");
				if(this.binQuantiles == null) {
					this.binQuantiles = new int[] {33, 66};
				}
				edgeCalc = new PercentileEdgeCalculator(this.binQuantiles);
				break;
			case KMEANS:
				logger.info("Use kmeans clustering k=" + this.nCluster);
				if(this.nCluster == -1) {
					this.nCluster = 2;
				}
				edgeCalc = new KMeansEdgeCalculator(this.nCluster);
				break;
			}
			switch(timeBinType) {
			case DURATION:
				this.setTraceDescFactory(new TraceDescBinnedActDurFactory(this.getClassifier(), edgeCalc));
				break;
			case SOJOURN:
				this.setTraceDescFactory(new TraceDescBinnedActSojFactory(this.getClassifier(), edgeCalc));
				break;
			default:
				this.setTraceDescFactory(new TraceDescBinnedActSojFactory(this.getClassifier(), edgeCalc));
				break;
			}
			break;
		case TWED:
			this.setTraceDescFactory(new TimedTraceDescriptorFactory(this.getClassifier(), timeBinType));
			break;
		default:
			break;
		
		}

		return new EMDTraceComparisonParameters(classifier, distanceCalculator, traceDescFactory);
	}
	
	public EMDTraceCompParamBuilder setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
		return this;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	protected GroundDistances getDistName() {
		return distName;
	}

	public AbstractTraceDescriptorFactory getTraceDescFactory() {
		return traceDescFactory;
	}

	public TraceDescDistCalculator getDistanceCalculator() {
		return distanceCalculator;
	}

	public void setTraceDescFactory(AbstractTraceDescriptorFactory traceDescFactory) {
		this.traceDescFactory = traceDescFactory;
	}

	public void setDistanceCalculator(TraceDescDistCalculator distanceCalculator) {
		this.distanceCalculator = distanceCalculator;
	}

	public void setBinQuantiles(int[] binQuantiles) {
		this.binQuantiles = binQuantiles;
	}
	
	public EMDTraceCompParamBuilder setEdgeCalculator(EdgeCalculatorType edgeCalcType) {
		this.edgeCalcType = edgeCalcType;
		return this;
	}

	public EdgeCalculatorType getEdgeCalculator() {
		return this.edgeCalcType;
	}

	public EMDTraceCompParamBuilder setTimeBinType(TimeBinType timeBinType) {
		this.timeBinType = timeBinType;
		return this;
	}
	
	public EMDTraceCompParamBuilder setNbrClusters(int nClusters) {
		this.nCluster = nClusters;
		return this;
	}
}
