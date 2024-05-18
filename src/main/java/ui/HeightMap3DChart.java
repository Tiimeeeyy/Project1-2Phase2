package ui;

import javafx.scene.layout.Pane;
import org.jfree.chart3d.Chart3D;
import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.data.Range;
import org.jfree.chart3d.data.function.Function3D;
import org.jfree.chart3d.fx.Chart3DViewer;
import org.jfree.chart3d.plot.XYZPlot;
import org.jfree.chart3d.renderer.xyz.SurfaceRenderer;
// import org.jfree.chart3d.paint.ColorScale;

import java.awt.Color;

public class HeightMap3DChart {
    private double[][] heightStorage;
    private Pane root;

    public HeightMap3DChart(double[][] heightStorage, Pane root) {
        this.heightStorage = heightStorage;
        this.root = root;
    }

    /**
     * This Method takes the inputted height values and creates a 3D plot from them.
     * @return Chart3DViewer A Chart Object.
     */
    public Chart3DViewer createChart() {
        // Create a Function3D from heightStorage
        Function3D function = new Function3D() {
            @Override
            public double getValue(double x, double y) {
                int xi = (int) Math.round(x);
                int yi = (int) Math.round(y);
                if (xi >= 0 && xi < heightStorage.length && yi >= 0 && yi < heightStorage[0].length) {
                    return heightStorage[xi][yi];
                } else {
                    return Double.NaN;
                }
            }
        };

        // Create a chart with the function
        Chart3D chart = Chart3DFactory.createSurfaceChart("", "", function, "X", "Y", "Z");

        XYZPlot plot = (XYZPlot) chart.getPlot();
        SurfaceRenderer renderer = (SurfaceRenderer) plot.getRenderer();
        SingleColorScale colorScale = new SingleColorScale(Color.GREEN);
        renderer.setColorScale(colorScale);
        chart.setChartBoxColor(new Color(255, 255, 255, 0));

        // Display the chart in a JavaFX Chart3DViewer
        return new Chart3DViewer(chart);
    }

    /**
     * This Method Displays the chart and binds it to the height and width constraints of the Pane they're added to.
     */
    public void display3DChart() {
        Chart3DViewer viewer = createChart();
        viewer.prefWidthProperty().bind(root.widthProperty());
        viewer.prefHeightProperty().bind(root.heightProperty());
        Pane chartPane = new Pane(viewer);
        root.getChildren().add(chartPane);
    }

    /**
     * A simple color scale implementation to use a single color for the surface.
     */
    private static class SingleColorScale implements org.jfree.chart3d.renderer.ColorScale {
        private Color color;

        public SingleColorScale(Color color) {
            this.color = color;
        }

        @Override
        public Color valueToColor(double value) {
            return color;
        }

        @Override
        public Range getRange() {
            return new Range(0, 1);
        }
    }
}
