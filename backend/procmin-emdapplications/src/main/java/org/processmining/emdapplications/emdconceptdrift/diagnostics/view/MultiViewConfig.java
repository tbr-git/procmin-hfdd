package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.DetailedViewRealization;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.MultiViewRealization;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewRealization;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;

import com.google.common.collect.Iterators;

public class MultiViewConfig {
	private final static Logger logger = LogManager.getLogger( MultiViewConfig.class );
	
	private DetailedViewConfig topLevelView;
	
	private List<ViewConfig> subViews;


	public MultiViewConfig() {
		topLevelView = null;
		subViews = new LinkedList<>();
	}

	public MultiViewConfig(DetailedViewConfig topLevelView, List<ViewConfig> subViews) {
		super();
		this.topLevelView = topLevelView;
		this.subViews = subViews;
	}

	public MultiViewConfig(MultiViewConfig multiViewConfig) {
		this.topLevelView = new DetailedViewConfig(multiViewConfig.getTopLevelView());
		this.subViews = new LinkedList<ViewConfig>();
		multiViewConfig.getSubViews().stream().forEach(v -> this.subViews.add(new ViewConfig(v)));
	}
	
	public MultiViewConfig setTopLevelView(DetailedViewConfig topLevelView) {
		this.topLevelView = topLevelView;
		return this;
	}

	public MultiViewConfig addSubView(ViewConfig subView) {
		this.subViews.add(subView);
		return this;
	}

	public MultiViewConfig addSubViews(Collection<ViewConfig> subViews) {
		this.subViews.addAll(subViews);
		return this;
	}
	
	public Iterator<ViewConfig> getViewIterator() {
		return Iterators.concat(Collections.singleton(topLevelView).iterator(), subViews.iterator());
	}
	
	public DetailedViewConfig getTopLevelView() {
		return topLevelView;
	}

	public List<ViewConfig> getSubViews() {
		return subViews;
	}
	
	public MultiViewRealization createMultiViewOnData(WindowDiagnosticsData data, PerspectiveDescriptor description) {
		if(topLevelView == null) {
			logger.error("Cannot create multi-view realization: No top level view specified!");
			return null;
		}
		else {
			DetailedViewRealization topLevelViewRealization = topLevelView.createViewOnData(data, description);
			List<ViewRealization> subViewRealizations = subViews.stream().map(v -> v.createViewOnData(data, description)).collect(Collectors.toList());
			return new MultiViewRealization(topLevelViewRealization, subViewRealizations, data);
		}
	}

	public boolean isConsistent4LogProjection(XLog xlog) {
		Iterator<ViewConfig> itView = getViewIterator();
		boolean isConsistent = true;
		while(isConsistent && itView.hasNext()) {
			if(!itView.next().isConsistent4LogProjection(xlog)) {
				isConsistent = false;
			}
		}
		return isConsistent;
	}
	
}
