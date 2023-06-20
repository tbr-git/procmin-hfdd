package org.processmining.emdapplications.emdconceptdrift.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.emdconceptdrift.config.DiaglogResultProcessing;
import org.processmining.emdapplications.emdconceptdrift.config.ParameterBuilder;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.GroundDistances;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.EdgeCalculatorType;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimeBinType;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.framework.util.ui.widgets.WidgetImages;

import com.fluxicon.slickerbox.components.IconVerticalTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Parameter Dialog GUI for configuring the plugin.
 * @author brockhoff
 *
 */
public class ParameterDialog extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1333585372632133771L;

	private final static Logger logger = LogManager.getLogger( ParameterDialog.class );

	/**
	 * Panes for Distance and Window configuration
	 */
	private final IconVerticalTabbedPane tabbedpane;

	/**
	 * Distance configuration (property) panel
	 */
	private ParameterDiaglogTraceComparison distConfig;
	

	/**
	 * Create the plugin configuration dialog
	 * @param log The input log to the plugin
	 * @param paramBuilder
	 */
	public ParameterDialog(XLog log, final  ParameterBuilder paramBuilder) {
		// Basic layout is a BorderLayout
		setBackground(new Color(40, 40, 40));
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());

		// Pane for selecting between Distance and Window Configurations
		tabbedpane = new IconVerticalTabbedPane(new Color(230, 230, 230, 210),
				new Color(20, 20, 20, 160));
		add(tabbedpane, BorderLayout.CENTER);
		
		// Add tabs for Window and Distance configuration panes
		tabbedpane.addTab("Window", WidgetImages.inspectorIcon, createWinConfig(paramBuilder));
		distConfig = new ParameterDiaglogTraceComparison(paramBuilder.getTraceComparisonParamBuilder());
		tabbedpane.addTab("Distance", WidgetImages.inspectorIcon, distConfig);
	}

	/**
	 * Creates the sliding window configuration panel
	 * @param paramBuilder Parameter builder that will contain the final configuration
	 * @return The created sliding window configuration panel
	 */
	public ProMPropertiesPanel createWinConfig(ParameterBuilder paramBuilder) {
		logger.info("Creating Window config property panel");
		// The panel that will finally be returned
		final ProMPropertiesPanel winConfig = new ProMPropertiesPanel("Window Configuration");
		
		// Text boxes for window and stride size input
		ProMTextField tfWinSize = winConfig.addTextField("Select Window Size");	
		ProMTextField tfStrideSize = winConfig.addTextField("Select Stride Size");	
		
		// List containing the added windows (for displaying it to the user)
		DefaultListModel<String> lsWinConfModel = new DefaultListModel<>();		
		
		// Panel containing controls for adding windows and displaying the currently added windows 
		JPanel configGroup = new JPanel();
		configGroup.setOpaque(false);
		configGroup.setLayout(new BoxLayout(configGroup, BoxLayout.PAGE_AXIS));
		// List of currently added windows
		configGroup.add(new ProMList<String>("Specification", lsWinConfModel));
		
		// Add current window defined by the window spec text fields
		JButton btnConfWS = SlickerFactory.instance().createButton("Add");
		btnConfWS.addActionListener(new ActionListener() {
			/**
			 * Reads the current window specification and adds it to display list and parameter builder.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				int winSize = Integer.parseInt(tfWinSize.getText());
				int strideSize = Integer.parseInt(tfStrideSize.getText());
				lsWinConfModel.addElement(String.format("Window - size %d - stride %d", winSize, strideSize));
				paramBuilder.addWindow(winSize, strideSize);
			}
		});	
		configGroup.add(btnConfWS);
		winConfig.addProperty("Current Config", configGroup);
		
		return winConfig;
	}
	
	public ParameterDiaglogTraceComparison getTraceComparisonDiaglog() {
		return distConfig;
	}
}
