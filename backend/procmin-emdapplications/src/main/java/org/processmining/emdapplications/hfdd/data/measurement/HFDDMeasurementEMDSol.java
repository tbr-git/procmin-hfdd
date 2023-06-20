package org.processmining.emdapplications.hfdd.data.measurement;

import java.util.Optional;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewConfig;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;
import org.processmining.emdapplications.hfdd.util.EMDSolAnalyzer;

public class HFDDMeasurementEMDSol extends HFDDMeasurement {
	
	private final Optional<EMDSolContainer> emdSol;
	
	
	/**
	 * Copy constructor if measurements would be equal for two viewConfigs and perspective descriptors.
	 * @param m
	 * @param perspectiveDescription
	 * @param viewConfig
	 */
	public HFDDMeasurementEMDSol(HFDDMeasurementEMDSol m, PerspectiveDescriptor perspectiveDescription, 
			ViewConfig viewConfig) {
		super(perspectiveDescription, viewConfig, m.getProbLeftNonEmpty(), m.getProbRightNonEmpty(), 
				m.getMetric(), m.getFlow2EmptyTraceCost(), m.isMetricDefined(), m.isProbabilityZero());
		this.emdSol = m.getEMDSolution();
	}

	public HFDDMeasurementEMDSol(PerspectiveDescriptor perspectiveDescription, ViewConfig viewConfig,
			double probLeftNonEmpty, double probRightNonEmpty, Optional<EMDSolContainer> emdSol, 
			boolean metricDefined, boolean probabilityZero) {
		super(perspectiveDescription, viewConfig, probLeftNonEmpty, probRightNonEmpty, 
				emdSol.isPresent() ? Optional.of(emdSol.get().getEMD()) : Optional.empty(), 
				emdSol.isPresent() ? Optional.of(EMDSolAnalyzer.flow2EmptyCost(emdSol.get())) : Optional.empty(), 
						metricDefined, probabilityZero);
		this.emdSol = emdSol;
	}
	
	public Optional<EMDSolContainer> getEMDSolution() {
		return this.emdSol;
	}

}
