package org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.frequentpattern.connector;

import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSEditOperation;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public class LVSEditOpTriple {
	
	final LVSEditOperation op;

	final TraceDescriptor traceL; 

	final TraceDescriptor traceR;

	public LVSEditOpTriple(LVSEditOperation op, TraceDescriptor traceL, TraceDescriptor traceR) {
		super();
		this.op = op;
		this.traceL = traceL;
		this.traceR = traceR;
	}

	public LVSEditOperation getOp() {
		return op;
	}

	public TraceDescriptor getTraceL() {
		return traceL;
	}

	public TraceDescriptor getTraceR() {
		return traceR;
	}
	

}
