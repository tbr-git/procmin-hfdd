package org.processmining.emdapplications.emdconceptdrift.grounddistances;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.EditSequence;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSEditOperation;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.editdiagnostics.LVSOpNames;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTrace;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceCC;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class LVSStatefullWithEdit extends LevenshteinStateful implements TraceDistEditDiagnose {

	@Override
	public EditSequence get_distance_op(TraceDescriptor t1, TraceDescriptor t2) {
		int[] leftCat;
		int[] rightCat;
		if(t1 instanceof BasicTrace && t2 instanceof BasicTrace) {
			BasicTrace t1Basic = (BasicTrace) t1;
			BasicTrace t2Basic = (BasicTrace) t2;
			Pair<int[], int[]> tracesCategorical = categorizeTraces(t1Basic.getsTrace(), t2Basic.getsTrace());
			leftCat = tracesCategorical.getLeft();
			rightCat = tracesCategorical.getRight();
		}
		else if(t1 instanceof BasicTraceCC && t2 instanceof BasicTraceCC) {
			BasicTraceCC t1Casted = (BasicTraceCC) t1;
			BasicTraceCC t2Casted = (BasicTraceCC) t2;
			leftCat = t1Casted.getTraceCategories();
			rightCat = t2Casted.getTraceCategories();
		}
		else {
			throw new RuntimeException("LVSStatefullWithEdit is not defined for descriptors: " + t1.getClass() + " - " + t2.getClass());
		}
		
		return calcNormWeightedLevDistWithOp(leftCat, rightCat);
		
	}
	
	
	public EditSequence calcNormWeightedLevDistWithOp(int[] left, int[] right) {
		double norm = (double) Math.max(left.length, right.length);


		EditSequence.Builder editSeqBuilder = new EditSequence.Builder();
		editSeqBuilder = addDistNOp(left, right, editSeqBuilder);
		editSeqBuilder.setCost((norm > 0) ? editSeqBuilder.getCost() / norm : 0 );

		return editSeqBuilder.build();
	}	
	
	
	public EditSequence.Builder addDistNOp(int[] left, int[] right, EditSequence.Builder editSeqBuilder) {
		double[][] distance = new double[left.length + 1][right.length + 1]; 
		LVSEditOperation[][] optPath = new LVSEditOperation[left.length + 1][right.length + 1]; 
																				 
	    distance[0][0] = 0;
	    optPath[0][0] = null;
	    for(int i = 0; i < left.length; i++) {
	    	distance[i+1][0] = distance[i][0] + 1;
	    	optPath[i+1][0] = new LVSEditOperation(LVSOpNames.DELETE, i, -1);
	    }
	    for(int j = 0; j < right.length; j++) {
	    	distance[0][j + 1] = distance[0][j] + 1;
	    	optPath[0][j+1] = new LVSEditOperation(LVSOpNames.INSERT, -1, j);
	    }
																				 
	    
	    double cost = 0;
		for (int i = 1; i <= left.length; i++) {
			for (int j = 1; j <= right.length; j++) {
				cost = 0;
				boolean needRenaming = false;
				if(left[i - 1] != right[j - 1]) {
					cost += 1;
					needRenaming = true;
				}
				cost += distance[i - 1][j - 1];
				double costInsert = distance[i][j - 1] + 1;
				double costDelete = distance[i-1][j] + 1;

				if(cost <= costInsert && cost <= costDelete) {
					if(needRenaming) {
						optPath[i][j] = new LVSEditOperation(LVSOpNames.RENAME, i-1, j-1);
					}
					else {
						optPath[i][j] = new LVSEditOperation(LVSOpNames.MATCH, i-1, j-1);
					}
					distance[i][j] = cost;
				}
				else if(costInsert <= costDelete) {
					optPath[i][j] = new LVSEditOperation(LVSOpNames.INSERT, -1, j-1);
					distance[i][j] = costInsert;
				}
				else {
					optPath[i][j] = new LVSEditOperation(LVSOpNames.DELETE, i-1, -1);
					distance[i][j] = costDelete;
				}
			}
		}
		
//		List<Pair<Integer, Pair<Integer, Integer>>> lOptPath = new LinkedList<>();
//		StringBuilder builder = new StringBuilder();
		int i = left.length;
		int j = right.length;
		while(i > 0 || j > 0) {
			editSeqBuilder.addEditOperationReverse(optPath[i][j]);	
			switch(optPath[i][j].getOperation()) {
				case MATCH:
					i--;
					j--;
					break;
				case RENAME:
					i--;
					j--;
					break;
				case DELETE:
					i--;
					break;
				case INSERT:
					j--;
					break;
			}
			
		}
		editSeqBuilder.setCost(distance[left.length][right.length]);
		return editSeqBuilder;
	}              	

	public Pair<int[], int[]> categorizeTraces(String[] left, String[] right) {
		TObjectIntMap<String> map = new TObjectIntHashMap<>(10, 0.5f, -1);
		int lastIndex = -1;

		int[] leftCat = new int[left.length];
		for (int i = 0; i < left.length; i++) {
			leftCat[i] = map.adjustOrPutValue(left[i], 0, lastIndex + 1);
			if (leftCat[i] == lastIndex + 1) {
				lastIndex++;
			}
		}

		int[] rightCat = new int[right.length];
		for (int i = 0; i < right.length; i++) {
			rightCat[i] = map.adjustOrPutValue(right[i], 0, lastIndex + 1);
			if (rightCat[i] == lastIndex + 1) {
				lastIndex++;
			}
		}
		
		return Pair.of(leftCat, rightCat);
	}
}
