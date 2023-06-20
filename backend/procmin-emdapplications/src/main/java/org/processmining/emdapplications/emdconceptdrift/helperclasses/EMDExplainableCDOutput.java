package org.processmining.emdapplications.emdconceptdrift.helperclasses;

import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

public class EMDExplainableCDOutput {
	
	private final XLog xlog;
	
	private final MultiDimSlidingEMDOutput slidEMDoutput;

	private final AbstractTraceDescriptorFactory langFac;
	
	private final TraceDescDistCalculator trDescDist; 

	public EMDExplainableCDOutput(XLog xlog, MultiDimSlidingEMDOutput slidEMDoutput,
			AbstractTraceDescriptorFactory langFac, TraceDescDistCalculator trDescDist) {
		super();
		this.xlog = xlog;
		this.slidEMDoutput = slidEMDoutput;
		this.langFac = langFac;
		this.trDescDist = trDescDist;
	}

	public XLog getXlog() {
		return xlog;
	}

	public MultiDimSlidingEMDOutput getSlidEMDoutput() {
		return slidEMDoutput;
	}

	public AbstractTraceDescriptorFactory getLangFac() {
		return langFac;
	}

	public TraceDescDistCalculator getTrDescDist() {
		return trDescDist;
	}

}
