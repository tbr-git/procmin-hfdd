package org.processmining.emdapplications.emdconceptdrift.helperclasses;

import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDistEditDiagnose;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;

public class EMDCDDiagnosticsOutput {
	
	private final XLog xlog;
	
	private final MultiDimSlidingEMDOutput slidEMDoutput;

	private final AbstractTraceDescriptorFactory langFac;
	
	private final TraceDistEditDiagnose trDescDist; 

	public EMDCDDiagnosticsOutput(XLog xlog, MultiDimSlidingEMDOutput slidEMDoutput,
			AbstractTraceDescriptorFactory langFac, TraceDistEditDiagnose trDescDist) {
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

	public TraceDistEditDiagnose getTrDescDist() {
		return trDescDist;
	}

}
