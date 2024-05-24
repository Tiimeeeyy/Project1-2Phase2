package ui.helpers;

import javafx.scene.layout.Pane;
import org.jfree.chart3d.Chart3D;
import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.data.Range;
import org.jfree.chart3d.data.function.Function3D;
import org.jfree.chart3d.fx.Chart3DViewer;
import org.jfree.chart3d.plot.XYZPlot;
import org.jfree.chart3d.renderer.xyz.SurfaceRenderer;

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
                int width = heightStorage.length;
                int height = heightStorage[0].length;
                
                double xf = x * (width - 1); 
                double yf = y * (height - 1);                 
                int x1 = (int) Math.floor(xf);
                int x2 = (int) Math.ceil(xf);
                int y1 = (int) Math.floor(yf);
                int y2 = (int) Math.ceil(yf);

                if (x1 >= 0 && x2 < width && y1 >= 0 && y2 < height) {
                    double q11 = heightStorage[x1][y1];
                    double q21 = heightStorage[x2][y1];
                    double q12 = heightStorage[x1][y2];
                    double q22 = heightStorage[x2][y2];

                    double r1 = (x2 - xf) * q11 + (xf - x1) * q21;
                    double r2 = (x2 - xf) * q12 + (xf - x1) * q22;

                    return (y2 - yf) * r1 + (yf - y1) * r2;
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
