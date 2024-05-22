package ui.helpers;

import org.jfree.chart3d.data.Range;
import org.jfree.chart3d.renderer.ColorScale;

import java.awt.Color;

/**
 * A color scale that maps values to shades of green.
 */
public class GreenColorScale implements ColorScale {

    /**
     * Converts a value to a color.
     *
     * @param value the value to convert
     * @return the corresponding color
     */
    @Override
    public Color valueToColor(double value) {
        int g = 255 - (int) (value * 255 / 10.0);
        return new Color(0, g, 0);
    }

    /**
     * Gets the range of values for this color scale.
     *
     * @return the range of values
     */
    @Override
    public Range getRange() {
        return new Range(-10.0, 10.0);
    }
}
