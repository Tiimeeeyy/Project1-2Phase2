package ui.helpers;

import org.jfree.chart3d.data.Range;
import org.jfree.chart3d.renderer.ColorScale;

import java.awt.Color;

public class GreenColorScale implements ColorScale {
    @Override
    public Color valueToColor(double value) {
        int g = 255 - (int) (value * 255 / 10.0);
        return new Color(0, g, 0);
    }

    @Override
    public Range getRange() {
        return new Range(-10.0, 10.0);
    }
}