package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ContextAwareEmptyTraceBalancedTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.ScalingContext;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2EmptyTraceBalancedOrdStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;

public class ViewConfigAdapter {
	private final static Logger logger = LogManager.getLogger( ViewConfigAdapter.class );

	private int focusLogSizeL;

	private int focusLogSizeR;
	
	private int contextLogSizeL;
	
	private int contextLogSizeR;
	
	public ViewConfigAdapter() {
		focusLogSizeL = -1;
		focusLogSizeR = -1;
		contextLogSizeL = -1;
		contextLogSizeR = -1;
	}
	
	public ViewConfigAdapter setFocusLogSizeLeft(int focusLogSizeL) {
		this.focusLogSizeL = focusLogSizeL;
		return this;
	}

	public ViewConfigAdapter setFocusLogSizeRight(int focusLogSizeR) {
		this.focusLogSizeR = focusLogSizeR;
		return this;
	}

	public ViewConfigAdapter setContextLogSizeLeft(int contextLogSizeL) {
		this.contextLogSizeL = contextLogSizeL;
		return this;
	}

	public ViewConfigAdapter setContextLogSizeRight(int contextLogSizeR) {
		this.contextLogSizeR = contextLogSizeR;
		return this;
	}
	
	public void adaptConfig(MultiViewConfig multiViewConfig) {
		Iterator<ViewConfig> itView = multiViewConfig.getViewIterator();
		
		while(itView.hasNext()) {
			adaptConfig(itView.next());
		}
	}
	
	public void adaptConfig(ViewConfig viewConfig) {
		// Change of focus log
		if(focusLogSizeL > 0 || focusLogSizeR > 0) {
			Window2OrderedStochLangTransformer t = viewConfig.getLangTransformer();
			if(t instanceof ContextAwareEmptyTraceBalancedTransformer) {
				ContextAwareEmptyTraceBalancedTransformer t2 = (ContextAwareEmptyTraceBalancedTransformer) t;
				if(t2.getScalingContext() == ScalingContext.MODEL) {
					if(focusLogSizeL > 0) {
						t2.setContextLogSizeLeft(focusLogSizeL);
					}
					if(focusLogSizeR > 0) {
						t2.setContextLogSizeRight(focusLogSizeR);
					}
				}
				logger.debug("Updated config for: " + t2.toString());
			}
			else if(t instanceof Window2EmptyTraceBalancedOrdStochLangTransformer) {
				Window2EmptyTraceBalancedOrdStochLangTransformer t2 = (Window2EmptyTraceBalancedOrdStochLangTransformer) t;
				if(t2.getScalingContext() == ScalingContext.MODEL) {
					if(focusLogSizeL > 0) {
						t2.setPreScaleLeft(1.0 / focusLogSizeL);
					}
					if(focusLogSizeR > 0) {
						t2.setPreScaleRight(1.0 / focusLogSizeR);
					}
				}
			}
		}
		// Change of context log
		if(contextLogSizeL > 0 || contextLogSizeR > 0) {
			Window2OrderedStochLangTransformer t = viewConfig.getLangTransformer();
			if(t instanceof ContextAwareEmptyTraceBalancedTransformer) {
				ContextAwareEmptyTraceBalancedTransformer t2 = (ContextAwareEmptyTraceBalancedTransformer) t;
				if(t2.getScalingContext() == ScalingContext.GLOBAL) {
					if(contextLogSizeL > 0) {
						t2.setContextLogSizeLeft(contextLogSizeL);
					}
					if(contextLogSizeR > 0) {
						t2.setContextLogSizeRight(contextLogSizeR);
					}
				}
				logger.debug("Updated config for: " + t2.toString());
			}
			else if(t instanceof Window2EmptyTraceBalancedOrdStochLangTransformer) {
				Window2EmptyTraceBalancedOrdStochLangTransformer t2 = (Window2EmptyTraceBalancedOrdStochLangTransformer) t;
				if(t2.getScalingContext() == ScalingContext.GLOBAL) {
					if(contextLogSizeL > 0) {
						t2.setPreScaleLeft(1.0 / contextLogSizeL);
					}
					if(contextLogSizeR > 0) {
						t2.setPreScaleRight(1.0 / contextLogSizeR);
					}
				}
			}
		}
	}
}
