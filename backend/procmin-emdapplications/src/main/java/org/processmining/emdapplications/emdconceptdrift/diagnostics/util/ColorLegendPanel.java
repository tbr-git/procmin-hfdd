package org.processmining.emdapplications.emdconceptdrift.diagnostics.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.title.PaintScaleLegend;

public class ColorLegendPanel extends JPanel {

	private final Logger logger = LogManager.getLogger( ColorLegendPanel.class );
	/**
	 * 
	 */
	private static final long serialVersionUID = 348429655300457843L;
	
	private PaintScale paintScale;
	
	public ColorLegendPanel(PaintScale paintScale) {
		this.paintScale = paintScale;
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		NumberAxis scaleAxis = new NumberAxis("Clip");
		PaintScaleLegend psLegend = new PaintScaleLegend(paintScale, scaleAxis);
		psLegend.setSubdivisionCount(128);
		psLegend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
		psLegend.setPadding(10, 15, 10, 15);
		psLegend.setStripWidth(20);
//		psLegend.setPosition(RectangleEdge.RIGHT);
		psLegend.setBackgroundPaint(Color.WHITE);
		
		Dimension d = getSize();
		psLegend.draw(g2, new Rectangle(0, 0, d.width, d.height));
	}


	@Override
	public Dimension getPreferredSize() {
		return new Dimension(400, 50);
	}
	
	

}
