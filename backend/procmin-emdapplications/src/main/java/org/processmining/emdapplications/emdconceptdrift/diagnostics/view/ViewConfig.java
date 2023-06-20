package org.processmining.emdapplications.emdconceptdrift.diagnostics.view;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewRealization;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewRealizationOld;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.ViewRealizationSLDS;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.WindowDiagnosticsData;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.language.transformer.Window2OrderedStochLangTransformer;

/**
 * Class that can be used to create a view on two event logs using EMD.
 * 
 * If fully configures this view by specifying:
 * <ul>
 * <li> A stochastic language transformer (how to transform an input event log into a stochastic language)
 * <li> How to transform an input variant into a feature </li>
 * <li> Ground distance for the features </li>
 * </ul>
 * 
 * @author brockhoff
 *
 */
public class ViewConfig {

	private final static Logger logger = LogManager.getLogger( ViewConfig.class );
	
	/**
	 * Identifier for this view
	 */
	private ViewIdentifier viewId;

	/** 
	 * Stochastic language transformer
	 */
	private Window2OrderedStochLangTransformer langTransformer;
	
	/**
	 * Descriptor - Distance Pair
	 * How to transform a trace variant into an EMD input feature
	 * How to measure the distance between two features
	 */
	private DescriptorDistancePair descDistPair;
	

	public ViewConfig() {
		langTransformer = null;
		descDistPair = null;
		viewId = null;
	}
	
	/**
	 * Copy constructor.
	 * @param langTransformer
	 * @param descDistPair
	 * @param viewIdentifier
	 */
	public ViewConfig(Window2OrderedStochLangTransformer langTransformer, DescriptorDistancePair descDistPair,
			ViewIdentifier viewIdentifier) {
		super();
		this.langTransformer = langTransformer;
		this.descDistPair = descDistPair;
		this.viewId = viewIdentifier;
	}
	
	public ViewConfig(ViewConfig viewConfig) {
		//TODO
		Window2OrderedStochLangTransformer copiedLanguageTransformer = null;
		Class<?> transformerType = viewConfig.getLangTransformer().getClass();
	    // This next line throws a number of checked exceptions you need to catch
	    try {
			copiedLanguageTransformer = (Window2OrderedStochLangTransformer) transformerType.getConstructor(transformerType).newInstance(viewConfig.getLangTransformer());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			logger.error("Could not copy the language transformer in the copy constructor");
			e.printStackTrace();
		}
	    
	    this.langTransformer = copiedLanguageTransformer;
	    this.descDistPair = viewConfig.getDescDistPair();
	    this.viewId = viewConfig.getViewIdentifier();
	}
	
	public ViewConfig setLangTransformer(Window2OrderedStochLangTransformer langTransformer) {
		this.langTransformer = langTransformer;
		return this;
	}

	public ViewConfig setDescriptorDistancePair(DescriptorDistancePair descDistPair) {
		this.descDistPair = descDistPair;
		return this;
	}

	public ViewConfig setViewIdenfitier(ViewIdentifier viewIdentifier) {
		this.viewId = viewIdentifier;
		return this;
	}

	public Window2OrderedStochLangTransformer getLangTransformer() {
		return langTransformer;
	}

	public DescriptorDistancePair getDescDistPair() {
		return descDistPair;
	}

	public ViewIdentifier getViewIdentifier() {
		return viewId;
	}

	public ViewRealization createViewOnData(WindowDiagnosticsData data, PerspectiveDescriptor description) {
		if(langTransformer == null) {
			logger.error("Cannot create view realization: No language transformer specified");
			return null;
		}
		else if(descDistPair == null) {
			logger.error("Cannot create view realization: No descriptor distance pair specified");
			return null;
		}
		else if(viewId == null) {
			logger.error("Cannot create view realization: No view identifier specified");
			return null;
		}
		else {
			return new ViewRealizationOld(new ViewRealizationMeta(viewId, description), descDistPair, langTransformer, data);
		}
	}
	
	public ViewRealizationSLDS createViewOnData(BiComparisonDataSource<? extends CVariant> data, PerspectiveDescriptor description) {
		if(langTransformer == null) {
			logger.error("Cannot create view realization: No language transformer specified");
			return null;
		}
		else if(descDistPair == null) {
			logger.error("Cannot create view realization: No descriptor distance pair specified");
			return null;
		}
		else if(viewId == null) {
			logger.error("Cannot create view realization: No view identifier specified");
			return null;
		}
		else {
			return new ViewRealizationSLDS(new ViewRealizationMeta(viewId, description), descDistPair, langTransformer, data);
		}
	}
	
	public boolean isConsistent4LogProjection(XLog xlog) {
		AbstractTraceDescriptorFactory descFactory = descDistPair.getDescriptorFactory();
		return descFactory.isProjectionInvariant() || descFactory.doesLogContainInfo4InvariantProjection(xlog);
	}


	@Override
	public String toString() {
		return "View Config (" + getViewIdentifier() +  "): " + getDescDistPair().getShortDescription() + " with transformer: " + getLangTransformer().getShortDescription();
	}
	
}
