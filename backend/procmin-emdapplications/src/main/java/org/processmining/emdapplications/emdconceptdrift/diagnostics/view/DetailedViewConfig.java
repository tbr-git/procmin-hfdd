package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.DetailedViewRealization;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;

public class DetailedViewConfig extends ViewConfig {
	
	private DescriptorDetailedDistancePair detailedDescDistPair;

	public DetailedViewConfig() {
		super();
		detailedDescDistPair = null;
	}
	
	public DetailedViewConfig(DetailedViewConfig viewConfig) {
		super(viewConfig);
		detailedDescDistPair = viewConfig.getDescriptorDetailedDistancePair();
	}

	public DetailedViewConfig(Window2OrderedStochLangTransformer langTransformer, DescriptorDetailedDistancePair detailedDescDistPair,
			ViewIdentifier viewIdentifier) {
		super(langTransformer, detailedDescDistPair, viewIdentifier);
		this.detailedDescDistPair = detailedDescDistPair;
	}

	@Override
	public DetailedViewRealization createViewOnData(WindowDiagnosticsData data, PerspectiveDescriptor description) {
		return new DetailedViewRealization(new ViewRealizationMeta(getViewIdentifier(), description), detailedDescDistPair, getLangTransformer(), data);
	}
	
	public DescriptorDetailedDistancePair getDescriptorDetailedDistancePair() {
		return this.detailedDescDistPair;
	}

}
