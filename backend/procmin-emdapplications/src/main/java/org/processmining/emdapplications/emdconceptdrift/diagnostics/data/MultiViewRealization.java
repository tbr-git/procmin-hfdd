package org.processmining.emdapplications.emdconceptdrift.diagnostics.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.ViewRealizationMeta;

import com.google.common.collect.Iterators;

public class MultiViewRealization {
	private final static Logger logger = LogManager.getLogger( MultiViewRealization.class );

	private final DetailedViewRealization topLevelView;
	
	private final List<ViewRealization> subViews;

	
	private Map<ViewRealizationMeta, ViewRealization> mapViewId2Realization;

	public MultiViewRealization(DetailedViewRealization topLevelView, List<ViewRealization> subViews, WindowDiagnosticsData dataLog) {
		this.topLevelView = topLevelView;
		this.subViews = Collections.unmodifiableList(subViews);

		mapViewId2Realization = new HashMap<>();
		
		mapViewId2Realization.put(this.topLevelView.getRealizationMeta(), this.topLevelView);
		subViews.stream().forEach(v -> mapViewId2Realization.put(v.getRealizationMeta(), v));
	}
	
	public void initAllViews() throws ViewDataException {
		initTopLevelView();
		initSubViews();
	}
	
	public void initTopLevelView() throws ViewDataException {
		topLevelView.populate();
	}
	
	public void initSubViews() throws ViewDataException {
		for(ViewRealization v: subViews) {
			v.populate();
		}
	}
	
	/**
	 * Reduces the memory consumption of this view realization
	 */
	public void reduceMemoryConsumption() {
		for(ViewRealization v: subViews) {
			try {
				v.reduceMemoryConsumption();
			} catch (ViewDataException e) {
				logger.error("Could not reduce memory consumption!");
				e.printStackTrace();
			}
		}
	}
	
	public DetailedViewRealization getTopLevelViewRealization() {
		return topLevelView;
	}
	
	public ViewRealization getRealization(ViewRealizationMeta viewRealzationMeta) {
		return mapViewId2Realization.get(viewRealzationMeta);
	}
	
	public Iterator<ViewRealization> getViewIterator() {
		return Iterators.concat(Collections.singleton(topLevelView).iterator(), subViews.iterator());
	}
}
