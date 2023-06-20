package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.util.FilterUtil;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;


public class WindowDiagnosticsData {

	private final static Logger logger = LogManager.getLogger( WindowDiagnosticsData.class );
	
	private boolean enhancedByMetaData;
	
	private XLog xlogL;
	
	private XLog xlogR;
	
	private Optional<XLog> xlog;
	
	public WindowDiagnosticsData() {
		xlogL = null;
		xlogR = null;
		xlog = Optional.empty();
		enhancedByMetaData = false;
	}
	
	public WindowDiagnosticsData(XLog xlogL, XLog xlogR) {
		this.xlogL = xlogL;
		this.xlogR = xlogR;
		xlog = Optional.empty();
	}
	
	public XLog getXLogLeft() {
		return this.xlogL;
	}

	public XLog getXLogRight() {
		return this.xlogR;
	}
	
	public XLog getXLog() {
		if(!xlog.isPresent()) {
			List<XLog> l = new LinkedList<>();
			l.add(xlogL);
			l.add(xlogR);
			xlog = Optional.of(new XLogLightJoin(l));
		}
		return xlog.get();
	}
	
	public void enhanceLogByDescMetaData(AbstractTraceDescriptorFactory fac) {
		fac.complementLogByDescAttributes(xlogL);
		fac.complementLogByDescAttributes(xlogR);
		enhancedByMetaData = true;
	}
	
	public void clearLifeCycleStarts() {
		if(!enhancedByMetaData) {
			logger.warn("Removing lifecylce transitions from logs without addings factory meta data might cause problems");
		}
		xlogL = FilterUtil.filterLifecycleStart(xlogL);
		xlogR = FilterUtil.filterLifecycleStart(xlogR);
	}
	
}
