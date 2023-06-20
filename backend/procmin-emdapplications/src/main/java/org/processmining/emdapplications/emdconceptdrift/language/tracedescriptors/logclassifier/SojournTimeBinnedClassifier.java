package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.logclassifier;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedActSojFactory;

public class SojournTimeBinnedClassifier extends XEventAttributeClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7606431815184588129L;
	
	private final String keySojTimeBin;

	public SojournTimeBinnedClassifier(String keySojTimeBin) {
		super("Event Name and Sojourn Time Bin Classifier", XConceptExtension.KEY_NAME, keySojTimeBin);
		this.keySojTimeBin = keySojTimeBin;
	}
	
	public String getSojournTimeBinKey() {
		return keySojTimeBin;
	}

}
