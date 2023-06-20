package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CVariantLog;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2SimpleNormOrdStochLangTransformer;

public class RealizabilityChecker {
	
	public static final int LOW_SUPPORT_THRESHOLD_ABSOLUT = 10;
	
	public static RealizabilityInfo checkRealizability(ViewRealizationMeta viewMeta, 
			DescriptorDistancePair descDistPair, Window2OrderedStochLangTransformer langTransformer, WindowDiagnosticsData data) {
		
		RealizationProblemType  problem = RealizationProblemType.NONE;
		String problemInfo = "";
		if(langTransformer instanceof  Window2SimpleNormOrdStochLangTransformer) {
			if(data.getXLogLeft().size() == 0 || data.getXLogRight().size() == 0) {
				if(data.getXLogLeft().size() == 0 && data.getXLogRight().size() == 0) {
					problem = RealizationProblemType.NO_SUPPORT_LEFTRIGHT;
					problemInfo = "Can not realize the view due to no support in both windows (" + langTransformer.getClass().getName() + ")";
				}
				else if(data.getXLogLeft().size() == 0) {
					problem =  RealizationProblemType.NO_SUPPORT_LEFT;
					problemInfo = "Can not realize the view due to no support in the left window (" + langTransformer.getClass().getName() + ")";
				}
				else {
					problem =  RealizationProblemType.NO_SUPPORT_RIGHT;
					problemInfo = "Can not realize the view due to no support in the right window (" + langTransformer.getClass().getName() + ")";
				}
			}
			else if (data.getXLogLeft().size() < LOW_SUPPORT_THRESHOLD_ABSOLUT || data.getXLogRight().size() < LOW_SUPPORT_THRESHOLD_ABSOLUT) {
				if(data.getXLogLeft().size() < LOW_SUPPORT_THRESHOLD_ABSOLUT && data.getXLogRight().size() < LOW_SUPPORT_THRESHOLD_ABSOLUT) {
					problem = RealizationProblemType.LITTLE_SUPPORT_LEFTRIGHT;
					problemInfo = "Can not realize the view due to little support in both windows (" + langTransformer.getClass().getName() + ")";
				}
				else if(data.getXLogLeft().size() < LOW_SUPPORT_THRESHOLD_ABSOLUT) {
					problem =  RealizationProblemType.LITTLE_SUPPORT_LEFT;
					problemInfo = "Can not realize the view due to little support in the left window (" + langTransformer.getClass().getName() + ")";
				}
				else {
					problem =  RealizationProblemType.LITTLE_SUPPORT_RIGHT;
					problemInfo = "Can not realize the view due to little support in the right window (" + langTransformer.getClass().getName() + ")";
				}
			}
		}
		return new RealizabilityInfo(problem, problemInfo);
	}

	public static RealizabilityInfo checkRealizability(ViewRealizationMeta viewMeta, 
			DescriptorDistancePair descDistPair, Window2OrderedStochLangTransformer langTransformer, 
			BiComparisonDataSource<? extends CVariant> biDataSource) {
		
		RealizationProblemType  problem = RealizationProblemType.NONE;
		String problemInfo = "";
		// Check factory compatibility
		AbstractTraceDescriptorFactory descFactory = descDistPair.getDescriptorFactory();
		// Check if variant information in the data sources are sufficient for the requested view
		if(descFactory.getRequiredVariantInfo().containedIn(biDataSource.getDataSourceLeft().getVariantProperties()) &&
				descFactory.getRequiredVariantInfo().containedIn(biDataSource.getDataSourceRight().getVariantProperties())) {
//		if(descFactory.getRequiredVariantInfo().equals(biDataSource.getDataSourceLeft().getVariantProperties()) &&
//				descFactory.getRequiredVariantInfo().equals(biDataSource.getDataSourceRight().getVariantProperties())) {
			CVariantLog<? extends CVariant> variantLogLeft;
			CVariantLog<? extends CVariant> variantLogRight;
			try {
				variantLogLeft = biDataSource.getDataSourceLeft().getVariantLog();
				variantLogRight = biDataSource.getDataSourceRight().getVariantLog();
			} catch (SLDSTransformationError e) {
				problemInfo = e.getMessage();
				problem = RealizationProblemType.DATASOURCE_BROKEN;
				return new RealizabilityInfo(problem, problemInfo);
			}
			// Check language factory requirements
			if(langTransformer instanceof  Window2SimpleNormOrdStochLangTransformer) {
				// Simple normalization Requires that there is sufficient support on both sides
				if(variantLogLeft.sizeLog() == 0 || variantLogRight.sizeLog() == 0) {
					if(variantLogLeft.sizeLog() == 0 && variantLogRight.sizeLog() == 0) {
						problem = RealizationProblemType.NO_SUPPORT_LEFTRIGHT;
						problemInfo = "Can not realize the view due to no support in both windows (" + langTransformer.getClass().getName() + ")";
					}
					else if(variantLogLeft.sizeLog() == 0) {
						problem =  RealizationProblemType.NO_SUPPORT_LEFT;
						problemInfo = "Can not realize the view due to no support in the left window (" + langTransformer.getClass().getName() + ")";
					}
					else {
						problem =  RealizationProblemType.NO_SUPPORT_RIGHT;
						problemInfo = "Can not realize the view due to no support in the right window (" + langTransformer.getClass().getName() + ")";
					}
				}
				else if (variantLogLeft.sizeLog() < LOW_SUPPORT_THRESHOLD_ABSOLUT || variantLogRight.sizeLog() < LOW_SUPPORT_THRESHOLD_ABSOLUT) {
					if(variantLogLeft.sizeLog() < LOW_SUPPORT_THRESHOLD_ABSOLUT && variantLogRight.sizeLog() < LOW_SUPPORT_THRESHOLD_ABSOLUT) {
						problem = RealizationProblemType.LITTLE_SUPPORT_LEFTRIGHT;
						problemInfo = "Can not realize the view due to little support in both windows (" + langTransformer.getClass().getName() + ")";
					}
					else if(variantLogLeft.sizeLog() < LOW_SUPPORT_THRESHOLD_ABSOLUT) {
						problem =  RealizationProblemType.LITTLE_SUPPORT_LEFT;
						problemInfo = "Can not realize the view due to little support in the left window (" + langTransformer.getClass().getName() + ")";
					}
					else {
						problem =  RealizationProblemType.LITTLE_SUPPORT_RIGHT;
						problemInfo = "Can not realize the view due to little support in the right window (" + langTransformer.getClass().getName() + ")";
					}
				}
			}
		}
		else {
			problem = RealizationProblemType.DATA_SPEC_ERROR;
			problemInfo = "Factory requires variant info " + descFactory.getRequiredVariantInfo() + ".\nLogs requires " 
					+ biDataSource.getDataSourceLeft().getVariantProperties() + " --- " + biDataSource.getDataSourceRight().getVariantProperties();
		}
		
		return new RealizabilityInfo(problem, problemInfo);
	}
	
}
