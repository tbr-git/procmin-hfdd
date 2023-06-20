package org.processmining.emdapplications.emdconceptdrift.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.emdapplications.emdconceptdrift.algorithm.SWEMDComp;
import org.processmining.emdapplications.emdconceptdrift.config.DiaglogResultProcessing;
import org.processmining.emdapplications.emdconceptdrift.config.EMDConceptDriftParameters;
import org.processmining.emdapplications.emdconceptdrift.config.ParameterBuilder;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.MultiDimSlidingEMDOutput;
import org.processmining.emdapplications.emdconceptdrift.ui.ParameterDialog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(
	name = "Concept Drift Detection with the EMD Plugin", 
	parameterLabels = { "Log", "Parameters" }, 
	returnLabels = { "Sliding EMD values" }, 
	returnTypes = { MultiDimSlidingEMDOutput.class }, 
	userAccessible = true, 
	help = "Applies EMD based window comparison"
)
public class EMDConceptDriftPlugin {
	
	/**
	 * Plugin with default values
	 * @param context Framework context
	 * @param xlog Log
	 * @return Class that stores output
	 */
	@PluginVariant(variantLabel = "Concept Drift Detection with the EMD Plugin, default", requiredParameterLabels = { 0 })
	@UITopiaVariant(
			affiliation = "RWTH Aachen", 
			author = "Tobias Brockhoff", 
			email = "brockhoff@pads.rwth-aachen.de"
		)
	public static MultiDimSlidingEMDOutput emdConceptDriftDefault(PluginContext context, XLog xlog) {
		ParameterBuilder paramBuilder = new ParameterBuilder();
		return SWEMDComp.multiDimSlidingEMDTraceDistrComp(context, xlog, paramBuilder.build(), new ProMCanceller() {
			
			@Override
			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}
		});	
	}
	
	/**
	 * Plugin with parameters
	 * @param context Framework context
	 * @param xlog Log
	 * @param para Parameters
	 * @return Class that stores output
	 */
	@PluginVariant(variantLabel = "Concept Drift Detection with the EMD Plugin, default", requiredParameterLabels = { 0, 1 })
	public static MultiDimSlidingEMDOutput emdConceptDriftParameter(PluginContext context, XLog xlog, EMDConceptDriftParameters para) {
		return SWEMDComp.multiDimSlidingEMDTraceDistrComp(context, xlog, para, new ProMCanceller() {
			
			@Override
			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}
	
	@PluginVariant(variantLabel = "Concept Drift Detection with the EMD Plugin, default", requiredParameterLabels = { 0 })
	@UITopiaVariant(
			affiliation = "RWTH Aachen", 
			author = "Tobias Brockhoff", 
			email = "brockhoff@pads.rwth-aachen.de"
		)
	public static MultiDimSlidingEMDOutput emdConceptDrift(UIPluginContext context, XLog xlog) {
		ParameterBuilder paramBuilder = new ParameterBuilder();
		ParameterDialog dialog = new ParameterDialog(xlog, paramBuilder);
		InteractionResult result = context.showWizard("EMD Concept Drift", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		else {
			DiaglogResultProcessing.processDialogResults(paramBuilder.getTraceComparisonParamBuilder(), dialog.getTraceComparisonDiaglog());
		}
		return emdConceptDriftParameter(context, xlog, paramBuilder.build());
	}
	

}
