package org.processmining.emdapplications.data.stochlangdatasource.transform.selection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.StochasticLanguageDataSource;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSAbstractTransformerFactory;
import org.processmining.emdapplications.data.variantlog.base.CVariant;

public class SLDSFilterByTraceIndexWindowFactory<E extends CVariant> extends SLDSAbstractTransformerFactory<E> { 
	private final static Logger logger = LogManager.getLogger( SLDSFilterByTraceIndexWindowFactory.class );
	
	/**
	 * Trace index of window center
	 */
	private int traceIndex;
	
	/**
	 * Window size
	 */
	private int windowSize;

	/**
	 * Log name
	 */
	private String logName;
	
	/**
	 * Extract log to the left of trace index
	 */
	private boolean extractLeft;
	
	public SLDSFilterByTraceIndexWindowFactory() {
		super();
		logName = "TraceIndexFilteredLog";
	}
	
	@Override
	public StochasticLanguageDataSource<E> build() {
		//TODO Implement a argument validity check that throws an exception
		if(this.parentDataSource == null) {
			logger.error("Missing parent data source.");
		}
		return new SLDSFilterByTraceIndexWindow<E>(this.parentDataSource, this.traceIndex, 
				this.windowSize, this.logName, this.extractLeft);
	}
	
	public SLDSFilterByTraceIndexWindowFactory<E> setTraceIndex(int traceIndex) {
		this.traceIndex = traceIndex;
		return this;
	}

	public SLDSFilterByTraceIndexWindowFactory<E> setWindowSize(int windowSize) {
		this.windowSize = windowSize;
		return this;
	}

	public SLDSFilterByTraceIndexWindowFactory<E> setLogName(String logName) {
		this.logName = logName;
		return this;
	}

	public SLDSFilterByTraceIndexWindowFactory<E> extractLeft() {
		this.extractLeft = true;
		return this;
	}

	public SLDSFilterByTraceIndexWindowFactory<E> extractRight() {
		this.extractLeft = false;
		return this;
	}

}
