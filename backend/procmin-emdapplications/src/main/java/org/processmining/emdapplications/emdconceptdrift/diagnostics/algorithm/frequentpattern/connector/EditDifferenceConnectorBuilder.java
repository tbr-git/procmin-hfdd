package org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.frequentpattern.connector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.prefixspan.SequenceDatabase;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.algorithm.prefixspan.WeightedSequence;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSEditOperation;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSOpNames;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class EditDifferenceConnectorBuilder {

	private BiMap<String, Integer> op2int;

	private Map<String, LVSEditOpTriple> op2Repr;
	
	int nextFreeId = 1;
	
	SequenceDatabase seqDatabase;
	
	public EditDifferenceConnectorBuilder() {
		op2int = HashBiMap.create();
		op2Repr = new HashMap<>();
		seqDatabase = new SequenceDatabase();
	}
	
	
	public EditDifferenceConnectorBuilder addToDataset(EditSequence editSeq, TraceDescriptor traceL, 
			TraceDescriptor traceR, double sequenceWeight) {
		
		List<Integer> l = new LinkedList<>();
		for(LVSEditOperation op : editSeq) {
			if(op.getOperation() != LVSOpNames.MATCH) {
				String opDesc = op.getDescription(traceL, traceR);
				int opId;
				if(!op2int.containsKey(opDesc)) {
					opId = nextFreeId;
					nextFreeId++;
					op2int.put(opDesc, opId);
					op2Repr.put(opDesc, new LVSEditOpTriple(op, traceL, traceR));
				}
				else {
					opId = op2int.get(opDesc);
				}
				l.add(opId);
				l.add(-1);
			}
		}
		if(l.size() != 0) {
			l.set(l.size() - 1, -2);
			int[] databaseSeq = l.stream().mapToInt(i -> i).toArray();
			
			seqDatabase.addSequence(new WeightedSequence(databaseSeq, sequenceWeight));
		}
		return this;
	}
	
	public EditDifferenceConnection build() {
		return new EditDifferenceConnection(op2int, op2Repr, seqDatabase);
	}
	
	
	
	
	
	
	
//		import java.util.LinkedList;
//		import java.util.List;
//
//		import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
//		import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSEditOperation;
//		import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSOpNames;
//		import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
//
//		public class EditTransactionBuilder {
//			
//			private double transactionWeight = 1.0;
//			private List<String> items;
//
//			public EditTransactionBuilder() {
//				items = new LinkedList<>();
//			}
//			
//			public EditTransactionBuilder setTransaction(EditSequence editSeq, TraceDescriptor traceL, 
//					TraceDescriptor traceR) {
//				
//				for(LVSEditOperation op : editSeq) {
//					if(op.getOperation() != LVSOpNames.MATCH) {
//						items.add(op.getDescription(traceL, traceR));
//					}
//				}
//				return this;
//			}
//			
//			public EditTransactionBuilder setWeight(double transactionWeight) {
//				this.transactionWeight = transactionWeight;
//				return this;
//			}
//			
//			public EditTransaction build() {
//				return new EditTransaction(items, transactionWeight);
//			}
//			
//		}
}
