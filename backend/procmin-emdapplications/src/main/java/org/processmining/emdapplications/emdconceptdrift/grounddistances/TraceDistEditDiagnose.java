package org.processmining.emdapplications.emdconceptdrift.grounddistances;

import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

public interface TraceDistEditDiagnose extends TraceDescDistCalculator {
	public EditSequence get_distance_op(TraceDescriptor t1, TraceDescriptor t2);
}
