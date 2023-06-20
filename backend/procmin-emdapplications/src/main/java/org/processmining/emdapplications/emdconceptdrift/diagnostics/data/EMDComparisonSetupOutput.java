package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective.DiagnosticsPerspective;

public class EMDComparisonSetupOutput {
	
	private final DiagnosticsPerspective perspective;
	
	public EMDComparisonSetupOutput(DiagnosticsPerspective perspective) {
		this.perspective = perspective;
	}
	
	public DiagnosticsPerspective getBasePerspective() {
		return perspective;
	}
}
