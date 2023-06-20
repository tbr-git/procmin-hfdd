package org.processmining.emdapplications.emdconceptdrift.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.MultiDimSlidingEMDOutput;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.SlidingEMDOutput;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.UIUtilities;

public class DisplayResultsManager {
	
	private MultiDimSlidingEMDOutput mdsemd;
	private DefaultXYDataset datasetSingle;
	private	Font font_label = new Font("Dialog", Font.BOLD, 18);
	private Font font_tick = new Font("Dialog", Font.PLAIN, 14);

	private JPanel optionPanel;
	
	public DisplayResultsManager(MultiDimSlidingEMDOutput mdsemd) {
		this.mdsemd = mdsemd;
		this.datasetSingle = new DefaultXYDataset();
	}
	
	public JComponent createOutputPanel() {
		mdsemd.sortAndPad();
		JComponent compDiagrams;
		JPanel top = createTopPanel();
		
		ChartPanel panelHeatmap = null;
		if(mdsemd.sizeDim() > 1) {
			panelHeatmap = createHeatmap();
		}
		
		if(mdsemd.sizeDim() > 1) {
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, panelHeatmap);
			splitPane.setResizeWeight(0.5);
			compDiagrams = splitPane;
		}
		else {
			compDiagrams = top;
		}

		this.optionPanel = new JPanel();
		this.optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.PAGE_AXIS));
		this.addControls(optionPanel);

//		JPanel controlPanel = createSingleChartControls();
//		this.optionPanel.add(controlPanel);
		JPanel root = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.99;
		c.weighty = 0.99;
		c.fill = GridBagConstraints.BOTH;
		root.add(compDiagrams, c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.01;
		c.fill = GridBagConstraints.NONE;
		root.add(this.optionPanel, c);
		return root;
	}


	private ChartPanel createHeatmap() {
		// x-axis for time
		NumberAxis xAxis = new NumberAxis("Trace");
		xAxis.setAutoRangeIncludesZero(false);
		
		// visible y-axis with symbols
		String labels[] = new String[mdsemd.sizeDim()];
		for (int i = 0; i < mdsemd.sizeDim(); i++)
		    labels[i] = String.valueOf(mdsemd.getOutput(i).getWinSize());
		SymbolAxis yAxis = new SymbolAxis(null, labels);
		yAxis.setTickUnit(new NumberTickUnit(1));
		
		XYZDataset dataHeatMap = createVizHeatmapDataset(mdsemd);
		Range range = UIUtilities.getRange(mdsemd);
		
		SpectrumPaintScale ps = new SpectrumPaintScale(range);
		XYBlockRenderer blockRenderer = new XYBlockRenderer();
		blockRenderer.setBlockHeight(1);
		blockRenderer.setPaintScale(ps);
		XYPlot heatmap = new XYPlot(dataHeatMap, xAxis, yAxis, blockRenderer);
		
		heatmap.getRangeAxis().setLabelFont(font_label);	
		heatmap.getRangeAxis().setTickLabelFont(font_tick);
		heatmap.getDomainAxis().setTickLabelFont(font_tick);
		heatmap.getDomainAxis().setLabelFont(font_label);
		heatmap.getRangeAxis().setAttributedLabel("Window Size");
		JFreeChart chartHeatmap = new JFreeChart(null, null, heatmap, false);
		ChartPanel panelHeatmap = new ChartPanel(chartHeatmap);
		panelHeatmap.setBorder(BorderFactory.createEmptyBorder(0, 33, 0, 0));
//		panelHeatmap.setBorder(BorderFactory.createEmptyBorder(0, 46, 0, 0));
		return panelHeatmap;
	}

	
	private JPanel createTopPanel() {
		return createSingleChart();
		
//		JPanel top = new JPanel(new GridBagLayout());
//		this.optionPanel = new JPanel();
//		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.PAGE_AXIS));
//		JPanel controlPanel = createSingleChartControls();
//
//		optionPanel.add(controlPanel);
//		
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridx = 0;
//		c.gridy = 0;
//		c.weightx = 0.99;
//		c.weighty = 0.99;
//		c.fill = GridBagConstraints.BOTH;
//		top.add(panelSingle, c);
//		c.gridx = 1;
//		c.gridy = 0;
//		c.weightx = 0.01;
//		c.fill = GridBagConstraints.NONE;
//      top.add(optionPanel, c);
		
	}
	
	private ChartPanel createSingleChart() {
		SlidingEMDOutput output0 = mdsemd.getOutput(0);
		ArrayList<Double> arrEMDVal = output0.getEmdVals();
		
		updateVizDataset(arrEMDVal, mdsemd.getTraceOffset(0), output0.getStrideSize());
		
		JFreeChart chartSingle = ChartFactory.createXYLineChart("EMD Trace Distribution Comparison",
                "Trace", "EMD", datasetSingle, PlotOrientation.VERTICAL, false, true,
                false);
		((XYPlot) chartSingle.getPlot()).getRangeAxis().setLabelFont(font_label);	
		((XYPlot) chartSingle.getPlot()).getRangeAxis().setTickLabelFont(font_tick);
		((XYPlot) chartSingle.getPlot()).getDomainAxis().setTickLabelFont(font_tick);
		((XYPlot) chartSingle.getPlot()).getDomainAxis().setLabelFont(font_label);
		
		ChartPanel panelSingle = new ChartPanel(chartSingle);
		
		return panelSingle;
	}

	
	private JPanel createSingleChartControls() {
		DefaultComboBoxModel<Integer> listData = new DefaultComboBoxModel<>();
		for(SlidingEMDOutput o : mdsemd) {
			listData.addElement(o.getWinSize());
		}
		JComboBox<Integer> cbDim = new JComboBox<>(listData);
		cbDim.setSelectedIndex(0);
		cbDim.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int ind = cbDim.getSelectedIndex();
				SlidingEMDOutput output = mdsemd.getOutput(ind);
				ArrayList<Double> arrEMDVal = output.getEmdVals();
				
				datasetSingle.removeSeries("EMD");
				updateVizDataset(arrEMDVal, mdsemd.getTraceOffset(ind), output.getStrideSize());		
				
			}
		});
		

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel cbPane = new JPanel();
        cbPane.setLayout(new BoxLayout(cbPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Dimension");
        label.setLabelFor(cbDim);
        cbPane.add(label);
        cbPane.add(Box.createRigidArea(new Dimension(0,5)));
        cbPane.add(cbDim);
        cbPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        return cbPane;
	}
	

	/**
	 * Create a dataset that can be displayed by {@code JFreeChart}.
	 * @param arrEMDVal EMD values that should be displayed
	 * @return JFree Dataset
	 */
	private void updateVizDataset(ArrayList<Double> arrEMDVal, int offset, int stride_size) {
        double[][] data = new double[2][arrEMDVal.size()];
        
        for(int i = 0; i < arrEMDVal.size(); i++) {
        	data[0][i] = 1 + offset + i * stride_size;
        	data[1][i] = arrEMDVal.get(i);
        }

        datasetSingle.addSeries("EMD", data);
    }

	
	private static XYZDataset createVizHeatmapDataset(MultiDimSlidingEMDOutput mdEMD) {
		DefaultXYZDataset dataset = new DefaultXYZDataset();
		double[][] data = new double[3][mdEMD.sizeDim() * mdEMD.getMaxLen()];
		
//		FileWriter writer = null;
//		try {
//			writer = new FileWriter("C:\\temp\\ConceptDriftEMDPromDump\\emd_val.csv");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		int i = 0;
		int y = 0;
		for(SlidingEMDOutput semd : mdEMD) {
			int x = 0;
			for(double v : semd.getEmdVals()) {
				data[0][i] = 1 + mdEMD.getTraceOffset(0) + x * mdEMD.getOutput(0).getStrideSize();
				data[1][i] = y;
				data[2][i] = v;
				x++;
				i++;
//				if(writer != null) {
//					try {
//						writer.append(String.valueOf(v));
//						writer.append("\n");
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
				
			}
			y++;
		}
		dataset.addSeries("EMD Heatmap", data);
//		if(writer != null) {
//			try {
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		return dataset;
	}
	
	public void addControls(JPanel controlPanel) {
		JPanel singleChartControlPanel = createSingleChartControls();
		addWrappedControl(singleChartControlPanel, "Select Win Size", controlPanel);
//		controlPanel.add(singleChartControlPanel);
		
	}
	
	
	public void addWrappedControl(JPanel controlPanel, String title, Container container) {
		TitledBorder border = BorderFactory.createTitledBorder(title);
		JPanel comp = new JPanel(new GridLayout(1, 1), false);
		comp.add(controlPanel);

		comp.setBorder(border);
		container.add(Box.createRigidArea(new Dimension(0, 10)));
		container.add(comp);

	}

}
