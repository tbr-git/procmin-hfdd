package org.processmining.emdapplications.emdconceptdrift.diagnostics.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeListImpl;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsDataLogBuilder;
import org.processmining.emdapplications.emdconceptdrift.util.LogSorter;
import org.processmining.framework.util.collection.AlphanumComparator;

public class MyLogUtil {

	public static void makeConcetNameLiteralInPlace(XLog xlog) {
		for(XTrace t : xlog) {
			XAttribute attribute = t.getAttributes().get("concept:name");
			if(attribute != null && !(attribute instanceof XAttributeLiteral)) {
					t.getAttributes().put("concept:name", new XAttributeListImpl(attribute.toString()));
			}
		}
	}
	
	public static WindowDiagnosticsData extractWindows(XLog xlog, int index, int winSizeL, int winSizeR) {
		LogSorter.sortOnStartTimes(xlog);
		WindowDiagnosticsDataLogBuilder builder = new WindowDiagnosticsDataLogBuilder();
		builder.addTracesFromLog(xlog, index, winSizeL, winSizeR);
		return builder.build();
	}
	
	public static ArrayList<XEventClass> getSortedEventClasses(XLog xlog, XEventClassifier classifier) {
		XLogInfo logInfo = XLogInfoImpl.create(xlog);
//		XLogInfo logInfoL = XLogInfoImpl.create(data.getXLogLeft(), getClassifier());
//		XLogInfo logInfoR = XLogInfoImpl.create(data.getXLogRight(), getClassifier());
//		XEventClasses eventClassesL = logInfoL.getEventClasses(getClassifier());
//		XEventClasses eventClassesR = logInfoL.getEventClasses(getClassifier());
		XEventClasses eventClasses = logInfo.getEventClasses(classifier);
//		Set<XEventClass> eventClasses = new HashSet<>(eventClassesL.getClasses());
//		eventClasses.addAll(eventClassesR.getClasses());
		ArrayList<XEventClass> sortedEventClasses = new ArrayList<>(eventClasses.getClasses());
		Collections.sort(sortedEventClasses, new Comparator<XEventClass>() {

			@Override
			public int compare(XEventClass o1, XEventClass o2) {
				return (new AlphanumComparator().compare(o1.toString(), o2.toString()));
			}
			
		});
		return sortedEventClasses;
	}
}
