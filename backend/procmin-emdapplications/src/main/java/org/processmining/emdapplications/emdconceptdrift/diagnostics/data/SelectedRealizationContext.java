package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.diagperspective.LightWeightDiagnosticsPerspective;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewRealizationMeta;

public class SelectedRealizationContext {
	
	private final LightWeightDiagnosticsPerspective perspective;
	
	private final ViewRealizationMeta viewRealizationMeta;
	
	public SelectedRealizationContext(LightWeightDiagnosticsPerspective perspective, ViewRealizationMeta viewRealizationMeta) {
		this.perspective = perspective;
		this.viewRealizationMeta = viewRealizationMeta;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(this == obj) {
			return true;
		}
		else {
			if(this.getClass() == obj.getClass()) {
				SelectedRealizationContext c = (SelectedRealizationContext) obj;
				return this.perspective.equals(c.getPerspective()) && this.viewRealizationMeta.equals(c.getRealizationMeta());
			}
			else {
				return false;
			}
		}
	}

	public LightWeightDiagnosticsPerspective getPerspective() {
		return perspective;
	}

	public ViewRealizationMeta getRealizationMeta() {
		return viewRealizationMeta;
	}

}
