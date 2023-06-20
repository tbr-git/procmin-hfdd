package org.processmining.emdapplications.emdconceptdrift.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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
import org.processmining.emdapplications.emdconceptdrift.config.EMDTraceCompParamBuilder;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.GroundDistances;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.EdgeCalculatorType;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimeBinType;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ParameterDiaglogTraceComparison extends ProMPropertiesPanel {
	private final static Logger logger = LogManager.getLogger( ParameterDiaglogTraceComparison.class );


	private JPanel cardsDistParam;
	
	/**
	 * Radio button enabled if Levenshtein should be used
	 */
	private JRadioButton rbtnDistLvs;

	/**
	 * Radio button enabled if weighted Levenshtein for time binning should be used
	 */
	private JRadioButton rbtnDistBinned;

	/**
	 * Radio button enabled if TWED should be used
	 */
	private JRadioButton rbtnDistTWED;
	
	private LevenstheinParameterPanel panelLVS;

	private TWEDParameterPanel panelTEWD;

	private BinnedLVSParameterPanel panelBinnedLVS;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4515587962532736453L;

	/**
	 * Create the plugin configuration dialog
	 * @param log The input log to the plugin
	 * @param paramBuilder
	 */
	public ParameterDiaglogTraceComparison(EMDTraceCompParamBuilder paramBuilder) {
		super("Trace Comparison Configuration");
		logger.info("Creating property panel for distance configuration");
		
		// Radiobuttons for selecting the distance function
		final JPanel buttonGroup = new JPanel();
		buttonGroup.setOpaque(false);
		buttonGroup.setLayout(new BoxLayout(buttonGroup, BoxLayout.PAGE_AXIS));
		final ButtonGroup groupDist = new ButtonGroup();
		rbtnDistLvs = createRadioButton(buttonGroup, groupDist, "Levensthein", false);
		rbtnDistLvs.setSelected(true);
		rbtnDistBinned = createRadioButton(buttonGroup, groupDist, "Levensthein Time Binned", false);
		rbtnDistTWED = createRadioButton(buttonGroup, groupDist, "TWED", false);
		
		// Listener that controls the actions when a distance is selected 
		// --> open the corresponding additional configuration panel
		DistSelectorListener listener = new DistSelectorListener(paramBuilder);
		rbtnDistLvs.addActionListener(listener);
		rbtnDistBinned.addActionListener(listener);
		rbtnDistTWED.addActionListener(listener);
		// Currently selected option is taken from the internal state of the parameter builder
		selectDistButtonFromPara(paramBuilder);
		
		this.addProperty("Ground Distance", buttonGroup);
		
		// A card layout that contains the distance function specific configuration panels
		logger.info("Adding specific distance parameter selection panels.");
		cardsDistParam = new JPanel(new CardLayout());
		panelLVS = new LevenstheinParameterPanel(paramBuilder);
		panelTEWD = new TWEDParameterPanel(paramBuilder);
		panelBinnedLVS = new BinnedLVSParameterPanel(paramBuilder);
		cardsDistParam.add(panelLVS, GroundDistances.LEVENSTHEIN.toString());
		cardsDistParam.add(panelTEWD, GroundDistances.TWED.toString());
		cardsDistParam.add(panelBinnedLVS, GroundDistances.TIMEBINNEDLVS.toString());
		// Show the panel for the currently selected distance function 
		((CardLayout) cardsDistParam.getLayout()).show(cardsDistParam, paramBuilder.getDistance().toString());
		
		this.add(cardsDistParam);
		
	}
	
	/**
	 * Create a radiobutton and add it to group
	 * @param component Panel to which the button is added
	 * @param buttonGroup Button group to which this button will belong
	 * @param label Button label
	 * @param leftAdjust Left adjust the button
	 * @return The created radiobutton
	 */
	protected JRadioButton createRadioButton(final JPanel component, final ButtonGroup buttonGroup, final String label,
			final boolean leftAdjust) {
		// Use slicker factory to create a button with ProM style
		final JRadioButton button = SlickerFactory.instance().createRadioButton(label);
		button.setForeground(WidgetColors.TEXT_COLOR);
		buttonGroup.add(button);
		if (leftAdjust) {
			final JPanel lefty = new JPanel();
			lefty.setOpaque(false);
			lefty.setLayout(new BoxLayout(lefty, BoxLayout.X_AXIS));
			lefty.add(button);
			lefty.add(Box.createHorizontalGlue());
			component.add(lefty);
		} else {
			component.add(button);
		}
		return button;
	}
	
	/**
	 * Select the radio button from the internal state of the parameter builder and activate it in the GUI
	 * @param paramBuilder
	 */
	private void selectDistButtonFromPara(EMDTraceCompParamBuilder paramBuilder) {
		switch(paramBuilder.getDistance()) {
		case LEVENSTHEIN:
			rbtnDistLvs.setSelected(true);
			rbtnDistBinned.setSelected(false);
			rbtnDistTWED.setSelected(false);
			break;
		case TWED:
			rbtnDistLvs.setSelected(false);
			rbtnDistBinned.setSelected(false);
			rbtnDistTWED.setSelected(true);
			break;
		case TIMEBINNEDLVS:
			rbtnDistLvs.setSelected(false);
			rbtnDistTWED.setSelected(false);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Listener for a distance function change.
	 * It updates the internal state of the parameter builder and updates the 
	 * distance configuration panel showing the panel that corresponds to chosen distance function.
	 * @author brockhoff
	 *
	 */
	class DistSelectorListener implements ActionListener {
		
		private final EMDTraceCompParamBuilder paramBuilder;
		
		public DistSelectorListener(EMDTraceCompParamBuilder paramBuilder) {
			this.paramBuilder = paramBuilder;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Update parameter builder
			if(e.getSource() == rbtnDistLvs) {
				paramBuilder.setDistance(GroundDistances.LEVENSTHEIN);
			}
			else if(e.getSource() == rbtnDistBinned) {
				paramBuilder.setDistance(GroundDistances.TIMEBINNEDLVS);
			}
			else if(e.getSource() == rbtnDistTWED) {
				paramBuilder.setDistance(GroundDistances.TWED);
			}
			// Show the corresponding distance function configuration panel
			((CardLayout) cardsDistParam.getLayout()).show(cardsDistParam, paramBuilder.getDistance().toString());
		}
		
	}
	
	/**
	 * Distance function configuration panel for the binned LVS distance.
	 * 
	 * Manages two additional panels for k-means or percentile-based bin edge computation.
	 * @author brockhoff
	 *
	 */
	public class BinnedLVSParameterPanel extends ProMPropertiesPanel {
		
		/**
		 * Panel for configuring percentile-based edge computation
		 */
		private BinnedLVSPercentilePanel panelPerc;

		/**
		 * Panel for configuring kmeans-based edge computation
		 */
		private BinnedLVSKMeansPanel panelKMeans;

		/**
		 * CardLayout for switching between the percentile-based and kmeans-based edge computation configuration panel 
		 */
		private JPanel cardsTimeClusterParam;

		/**
		 * Use activity service time perspective
		 */
		private JRadioButton rbtnDuration;

		/**
		 * Use sojourn time perspective
		 */
		private JRadioButton rbtnSojourn;
		
		/**
		 * Use percentile-based bin edge computation
		 */
		private JRadioButton rbtnPerc;
		
		/**
		 * Use kmeans-based bin edge computation
		 */
		private JRadioButton rbtnKMeans;
		/**
		 * 
		 */
		private static final long serialVersionUID = -6906255961417797500L;
		
		public BinnedLVSParameterPanel(EMDTraceCompParamBuilder paramBuilder) {
			super("Binned LVS Parameters");
			logger.info("Creating BinnedLVSParameterPanel");

			// Radiobutton group for choosing between kmeans- and percentile-based bin edge computation
			final JPanel buttonGroup = new JPanel();
			buttonGroup.setOpaque(false);
			buttonGroup.setLayout(new BoxLayout(buttonGroup, BoxLayout.PAGE_AXIS));
			final ButtonGroup groupTC = new ButtonGroup();
			rbtnPerc = createRadioButton(buttonGroup, groupTC, "Percentile", false);
			rbtnPerc.setSelected(true);
			rbtnKMeans = createRadioButton(buttonGroup, groupTC, "k-means", false);
			
			// Add listener that updates the configuration panel if bin edge compuation method is selected
			TimeClusterMethodSelector listener = new TimeClusterMethodSelector(paramBuilder);
			rbtnPerc.addActionListener(listener);
			rbtnKMeans.addActionListener(listener);
			selectClusterMethodFromPara(paramBuilder);
			
			this.addProperty("Time Cluster Method", buttonGroup);
			
			// Duration or Sojourn binning
			final JPanel pTimeType = new JPanel();
			pTimeType.setOpaque(false);
			pTimeType.setLayout(new BoxLayout(pTimeType, BoxLayout.PAGE_AXIS));
			final ButtonGroup gTimeType = new ButtonGroup();
			rbtnDuration = createRadioButton(pTimeType, gTimeType, "Duration", false);
			rbtnDuration.setSelected(true);
			rbtnSojourn = createRadioButton(pTimeType, gTimeType, "Sojourn", false);
			this.addProperty("Time" , pTimeType);
			
			// Card layout with configuration panels for kmeans- and percentile-based bin edge computation
			cardsTimeClusterParam = new JPanel(new CardLayout());
			panelPerc = new BinnedLVSPercentilePanel(paramBuilder);
			panelKMeans = new BinnedLVSKMeansPanel(paramBuilder);
			cardsTimeClusterParam.add(panelPerc, EdgeCalculatorType.PERCENTILE.toString());
			cardsTimeClusterParam.add(panelKMeans, EdgeCalculatorType.KMEANS.toString());
			((CardLayout) cardsTimeClusterParam.getLayout()).show(this.cardsTimeClusterParam, paramBuilder.getEdgeCalculator().toString());
			
			this.add(cardsTimeClusterParam);
			
		}
		
		/**
		 * Select radiobutton for the bin edge computation method according to the internal state of the parameter builder
		 * @param paramBuilder
		 */
		private void selectClusterMethodFromPara(EMDTraceCompParamBuilder paramBuilder) {
			switch(paramBuilder.getEdgeCalculator()) {
			case PERCENTILE:
				rbtnPerc.setSelected(true);
				rbtnKMeans.setSelected(false);
				break;
			case KMEANS:
				rbtnPerc.setSelected(false);
				rbtnKMeans.setSelected(true);
				break;
			default:
				break;
			}
		}

		/**
		 * Getter for the currently chosen time perspective (activity service time, sojourn time)
		 * @return
		 */
		public TimeBinType getTimeBinType() {
			if(rbtnDuration.isSelected()) {
				return TimeBinType.DURATION;
			}
			else {
				return TimeBinType.SOJOURN;
			}
		}

		/**
		 * Get the current number of bins (cluster for k-means, or nbr percentiles + 1) 
		 * @return
		 */
		public int getNbrBins() {
			if(rbtnPerc.isSelected()) {
				return panelPerc.getNbrBins();
			}
			else if(rbtnKMeans.isSelected()) {
				return panelKMeans.getK();
			}
			else {
				return -1;
			}
			
		}
		
		/**
		 * Get percentile-based binning panel
		 * @return
		 */
		public BinnedLVSPercentilePanel getPercentilePanel() {
			return this.panelPerc;
		}
		
		/**
		 * Get kmeans-based binning panel
		 * @return
		 */
		public BinnedLVSKMeansPanel getKMeansPanel() {
			return this.panelKMeans;
		}

		public class BinnedLVSPercentilePanel extends ProMPropertiesPanel {

			DefaultListModel<Integer> lsQuantileModel;
			/**
			 * 
			 */
			private static final long serialVersionUID = -6342268719572769390L;
			
			public BinnedLVSPercentilePanel(EMDTraceCompParamBuilder paramBuilder) {
				super("Percentile Bins Parameters");
				logger.info("Creating BinnedLVSPercentilePanel");

				// Quantiles
				ProMTextField tfQuantile = this.addTextField("Add Bin (Quantile)");	
				
				lsQuantileModel = new DefaultListModel<>();		
				
				JPanel configGroup = new JPanel();
				configGroup.setOpaque(false);
				configGroup.setLayout(new BoxLayout(configGroup, BoxLayout.LINE_AXIS));
				
				ProMList<Integer> lsQuantile = new ProMList<>("Bin Quantiles", lsQuantileModel);
				lsQuantile.setPreferredSize(new Dimension(200, 125));

				configGroup.add(lsQuantile);
				
				JButton btnAddQuant = SlickerFactory.instance().createButton("Add");
				btnAddQuant.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						int quantile = Integer.parseInt(tfQuantile.getText());
						lsQuantileModel.addElement(quantile);
					}
				});	
				configGroup.add(btnAddQuant);
				configGroup.add(Box.createHorizontalGlue());
				this.addProperty("Current Bin Quantiles", configGroup);
			}
			
			public int[] getQuantiles() {
				return Arrays.stream(lsQuantileModel.toArray()).mapToInt(v -> (Integer)v).toArray();
			}

			public int getNbrBins() {
				return lsQuantileModel.getSize() + 1;
			}
			
		}

		/**
		 * Panel for choosing the k in k-means binning for time.
		 * @author brockhoff
		 *
		 */
		public class BinnedLVSKMeansPanel extends ProMPropertiesPanel {

			private ProMTextField tfQuantile;

			/**
			 * 
			 */
			private static final long serialVersionUID = -2270604654881385082L;
			
			public BinnedLVSKMeansPanel(EMDTraceCompParamBuilder paramBuilder) {
				super("Percentile Bins Parameters");
				logger.info("Creating BinnedLVSPercentilePanel");
				tfQuantile = this.addTextField("Number clusters (k)");	
			}
			
			public int getK() {
				return Integer.parseInt(tfQuantile.getText());
			}
			
		}
				
		class TimeClusterMethodSelector implements ActionListener {
			
			private final EMDTraceCompParamBuilder paramBuilder;
			
			public TimeClusterMethodSelector(EMDTraceCompParamBuilder paramBuilder) {
				this.paramBuilder = paramBuilder;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == rbtnPerc) {
					paramBuilder.setEdgeCalculator(EdgeCalculatorType.PERCENTILE);
				}
				else if(e.getSource() == rbtnKMeans) {
					paramBuilder.setEdgeCalculator(EdgeCalculatorType.KMEANS);
				}
				((CardLayout) cardsTimeClusterParam.getLayout()).show(cardsTimeClusterParam, paramBuilder.getEdgeCalculator().toString());
			
			}
			
		}
	}
	
	class LevenstheinParameterPanel extends ProMPropertiesPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5494889430129123777L;

		public LevenstheinParameterPanel(EMDTraceCompParamBuilder paramBuilder) {
			super("Levensthein Parameter");
			logger.info("Creating LevenstheinParameterPanel");
			ProMTextField tfPara = this.addTextField("Parameters");
			tfPara.setEditable(false);
			tfPara.setText("None");
		}
		
	}	
	
	public class TWEDParameterPanel extends ProMPropertiesPanel {
		
		private final ProMTextField tfNu;
		
		private final ProMTextField tfLambda;
		
		private JRadioButton rbtnDuration;
		
		private JRadioButton rbtnSojourn;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4284965013065532513L;

		public TWEDParameterPanel(EMDTraceCompParamBuilder paramBuilder) {
			super("TWED Parameter");
			logger.info("Creating TWEDParameterPanel");
			
			// Duration or Sojourn binning
			final JPanel pTimeType = new JPanel();
			pTimeType.setOpaque(false);
			pTimeType.setLayout(new BoxLayout(pTimeType, BoxLayout.PAGE_AXIS));
			final ButtonGroup gTimeType = new ButtonGroup();
			rbtnDuration = createRadioButton(pTimeType, gTimeType, "Duration", false);
			rbtnDuration.setSelected(true);
			rbtnSojourn = createRadioButton(pTimeType, gTimeType, "Sojourn", false);
			this.addProperty("Time" , pTimeType);
			
			tfNu = this.addTextField("Select Nu");	
			tfLambda = this.addTextField("Select Lambda");	
			
			tfNu.setText("0.001");
			tfLambda.setText("1");
		}
		
		public double getNu() {
			return Double.valueOf(tfNu.getText());
			
		}
		
		public double getLambda() {
			return Double.valueOf(tfLambda.getText());
		}
		
		public TimeBinType getTimeBinType() {
			if(rbtnDuration.isSelected()) {
				return TimeBinType.DURATION;
			}
			else {
				return TimeBinType.SOJOURN;
			}
		}
		
	}

	public LevenstheinParameterPanel getPanelLVS() {
		return panelLVS;
	}

	public TWEDParameterPanel getPanelTEWD() {
		return panelTEWD;
	}

	public BinnedLVSParameterPanel getPanelBinnedLVS() {
		return panelBinnedLVS;
	}

}
