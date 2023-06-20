package org.processmining.emdapplications.emdconceptdrift.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescBinnedActDur;

public class EditVisualizer {

	public static String formatEditArray(TraceDescBinnedActDur t1, TraceDescBinnedActDur t2, 
			Pair<Double, List<Pair<Integer, Pair<Integer, Integer>>>> editSol) {	
		String[][] sEditArray = new String[t1.getTraceLength() + 2][t2.getTraceLength() + 2];
		for(int i = 0; i < t1.getTraceLength() + 2; i++) {
			for(int j = 0; j < t2.getTraceLength() + 2; j++) { 
				sEditArray[i][j] = "";
			}
		}
		for(int i = 0; i < t1.getTraceLength(); i++) {
			String l1 = t1.getTraceLabels()[i];
			int time1 = t1.getTimes()[i];
			sEditArray[i + 2][0] = l1 + "(" + time1 + ")";
		}
		for(int j = 0; j < t2.getTraceLength(); j++) {
			String l2 = Arrays.stream(t2.getTraceLabels()[j].split(" ")).map((s) -> s.substring(0, 2)).collect(Collectors.joining());
			int time2 = t2.getTimes()[j];
			sEditArray[0][j + 2] = l2 + "(" + time2 + ")";
		}
		for(Pair<Integer, Pair<Integer, Integer>> op:editSol.getRight()) {
			String opLabel = "";
			switch(op.getLeft()) {
			case 0:
				break;
			case 1:
				opLabel = "";
				break;
			case 2: 
				opLabel = "-";
				break;
			case 3:
				opLabel = "M";
				break;
			case 4:
				opLabel = "R";
				break;
			case 5:
				opLabel = "I";
				break;
			case 6:
				opLabel = "D";
				break;
			}
			sEditArray[op.getRight().getLeft() + 1][op.getRight().getRight() + 1] = opLabel;
		}

		return "\n" + formatAsTable(sEditArray) + "\nCost: " + editSol.getLeft();
	}
	
	public static String formatAsTable(String[][] tab)
	{
	    int[] maxLengths = new int[tab[0].length];
	    for (String[] row : tab)
	    {
	        for (int i = 0; i < row.length; i++)
	        {
	            maxLengths[i] = Math.max(maxLengths[i], row[i].length());
	        }
	    }

	    StringBuilder formatBuilder = new StringBuilder();
	    for (int maxLength : maxLengths)
	    {
	        formatBuilder.append("%-").append(maxLength + 2).append("s");
	    }
	    String format = formatBuilder.toString();

	    StringBuilder result = new StringBuilder();
	    for (Object[] row : tab)
	    {
	        result.append(String.format(format, row)).append("\n");
	    }
	    return result.toString();
	}
}
