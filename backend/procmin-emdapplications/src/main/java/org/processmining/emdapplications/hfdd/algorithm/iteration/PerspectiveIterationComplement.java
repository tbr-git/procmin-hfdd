package org.processmining.emdapplications.hfdd.algorithm.iteration;

import org.processmining.emdapplications.hfdd.algorithm.PerspectiveIteration;

public class PerspectiveIterationComplement extends PerspectiveIteration {

	private final int backupID; 
	
	public PerspectiveIterationComplement(int iteration, int backupID) {
		super(iteration);
		this.backupID = backupID;
	}
	
	@Override
	public String getID() {
		return super.getID() + " Backup " + backupID;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = 31 * hash + backupID;
		return hash;
	}

}
