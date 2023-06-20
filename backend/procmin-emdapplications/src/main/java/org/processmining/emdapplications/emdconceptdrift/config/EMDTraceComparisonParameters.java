package org.processmining.emdapplications.emdconceptdrift.config;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

public class EMDTraceComparisonParameters {
	/**
	 * Classifier parameter. This determines which classifier will be used
	 * during the mining.
	 */
	private final XEventClassifier classifier;
	
	private final TraceDescDistCalculator groundDist;
	
	private final AbstractTraceDescriptorFactory traceDescFactory; 
	

	/**
	 * Create default parameter values.
	 */
	protected EMDTraceComparisonParameters(XEventClassifier classifier, TraceDescDistCalculator groundDist, 
			AbstractTraceDescriptorFactory traceDescriptorFactory) {
		this.classifier = classifier;
		this.groundDist = groundDist;
		this.traceDescFactory = traceDescriptorFactory;
	}


	/**
	 * Gets the classifier.
	 * 
	 * @return The classifier.
	 */
	public XEventClassifier getClassifier() {
		return classifier;
	}

	/**
	 * Returns whether these parameter values are equal to the given parameter
	 * values.
	 * 
	 * @param object
	 *            The given parameter values.
	 * @return Whether these parameter values are equal to the given parameter
	 *         values.
	 */
	public boolean equals(Object object) {
		if (object instanceof EMDConceptDriftParameters) {
			EMDTraceComparisonParameters parameters = (EMDTraceComparisonParameters) object;
			if (classifier.equals(parameters.classifier)) {
				return true;
			}
			//TODO Compare windows sizes
		}
		return false;
	}
	
	/**
	 * Returns the hash code for these parameters.
	 */
	public int hashCode() {
		return classifier.hashCode();
	}

	@Override
	public String toString() {
		return "EMDConceptDriftParameters [classifier=" + classifier + "]";
	}
	
	public TraceDescDistCalculator getDistance() {
		return groundDist;
	}
	
	public AbstractTraceDescriptorFactory getTraceDescFactory() {
		return traceDescFactory;
	}
}
