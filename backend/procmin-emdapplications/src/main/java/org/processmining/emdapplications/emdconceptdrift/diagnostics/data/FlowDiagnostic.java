package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public class FlowDiagnostic {
	
	private final double flow;
	
	private final EditSequence editSequence;
	
	private final TraceDescriptor traceL;

	private final TraceDescriptor traceR;

	public static class Builder {
		
		private double flow;
		
		private EditSequence editSequence;

		private TraceDescriptor traceL;

		private TraceDescriptor traceR;
		
		public Builder() {
			super();
			flow = -1;
			editSequence = null;
		}

		public Builder setFlow(double flow) {
			this.flow = flow;
			return this;
		}
		
		public Builder setEditSequence(EditSequence editSequence) {
			this.editSequence = editSequence;
			return this;
		}
		
		public Builder setTraceLeft(TraceDescriptor traceL) {
			this.traceL = traceL;
			return this;
		}
		
		public Builder setTraceRight(TraceDescriptor traceR) {
			this.traceR = traceR;
			return this;
		}

		public FlowDiagnostic build() {
			return new FlowDiagnostic(this);
		}
		
	}

	protected FlowDiagnostic(Builder builder) {
		super();
		this.flow = builder.flow;
		this.editSequence = builder.editSequence;
		this.traceL = builder.traceL;
		this.traceR = builder.traceR;
	}
	
	public double getFlow() {
		return flow;
	}
	
	public EditSequence getEditSequence() {
		return this.editSequence;
	}
	
	public TraceDescriptor getTraceLeft() {
		return traceL;
	}

	public TraceDescriptor getTraceRight() {
		return traceR;
	}

}
