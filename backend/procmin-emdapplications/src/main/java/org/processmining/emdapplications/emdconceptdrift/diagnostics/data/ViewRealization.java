package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewIdentifier;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewRealizationMeta;
import org.processmining.emdapplications.emdconceptdrift.solutiondata.EMDSolContainer;

public abstract class ViewRealization {
	
	private final static Logger logger = LogManager.getLogger( ViewRealization.class );
	
	protected Optional<EMDSolContainer> emdSol;
	
	private final ViewRealizationMeta viewDescription;
	
	public ViewRealization(ViewRealizationMeta viewDescription) {
		this.emdSol = Optional.empty();
		this.viewDescription = viewDescription;
	}
	
	public EMDSolContainer getEMDSol() throws ViewDataException {
		if(!emdSol.isPresent())
			this.populate();
		return emdSol.get();
	}
	
	public abstract void populate() throws ViewDataException;
	
	public void reduceMemoryConsumption() throws ViewDataException {
		// Initializing a view vill delete its reference to the window data
		// -> The XLog instances in the window data are the most space-consuming elements
		this.populate();
	}

	public ViewIdentifier getViewIdentifier() {
		return viewDescription.getViewIdentifier();
	}
	
	public ViewRealizationMeta getRealizationMeta() {
		return this.viewDescription;
	}
	
	public abstract boolean isRealizable();
}
