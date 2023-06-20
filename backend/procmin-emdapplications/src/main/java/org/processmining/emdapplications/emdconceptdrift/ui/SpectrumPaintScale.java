package org.processmining.emdapplications.emdconceptdrift.ui;

import java.awt.Color;
import java.awt.Paint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.data.Range;

public class SpectrumPaintScale implements PaintScale {
	
	private final Logger logger = LogManager.getLogger( SpectrumPaintScale.class );

    private static final float H1 = 0.75f;
    private static final float H2 = -0.2f;
    private Range range;

    public SpectrumPaintScale(Range r) {
        this.range = r;
    }
    
    public void setRange(Range r) {
    	this.range = r;
    }

    @Override
    public double getLowerBound() {
        return range.getLowerBound();
    }

    @Override
    public double getUpperBound() {
        return range.getUpperBound();
    }

    @Override
    public Paint getPaint(double value) {
        float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
        float scaledH = H1 + scaledValue * (H2 - H1);
        return Color.getHSBColor(scaledH, 1f, 1f);
    }
}
