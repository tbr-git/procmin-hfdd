package org.processmining.emdapplications.emdconceptdrift.solver.akp.init;

import java.util.List;

import org.processmining.emdapplications.emdconceptdrift.solver.akp.graph.Tree;
import org.processmining.emdapplications.emdconceptdrift.solver.akp.helper.InitTreeArrayStruct;


public class StructInitEPSA {
	
	/**
	 * A handle to the internal tree representing the current 
	 * basic solution (in general not feasible)
	 */
	public Tree tree;
	
	/**
	 * Count sources
	 */
	public int cSrc;
	/**
	 * Count targets;
	 */
	public int cTar;
	
	/**
	 * List of deficit trees
	 */
	public List<Integer> defTrees;
	
	/**
	 * List of surplus trees
	 */
	public List<Integer> surTrees;
	
	/**
	 * Cost matrix (source,target)
	 */
	public double[][] costs;
	
	public StructInitEPSA(InitTreeArrayStruct inst) {
		this.tree = new Tree(inst);
		
		this.cSrc = inst.cSrc;
		this.cTar = inst.cTar;
		this.defTrees = inst.defTrees;
		this.surTrees = inst.surTrees;
		this.costs = inst.costs;
	}
}
