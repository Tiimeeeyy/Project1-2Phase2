package ui;
import javafx.scene.layout.Pane;
import org.jfree.chart3d.Chart3D;
import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.data.function.Function3D;
import org.jfree.chart3d.fx.Chart3DViewer;

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
        Chart3D chart = Chart3DFactory.createSurfaceChart("Surface Plot", "", function, "X", "Y", "Z");

        // Display the chart in a JavaFX Chart3DViewer
        return new Chart3DViewer(chart);
    }

    /**
     * This Method Displays the chart and binds it to the height and width constraints of the Pane they're added to.
     */
    public void display3DChart() {
        Chart3DViewer viewer = createChart();
        viewer.prefWidthProperty().bind(root.widthProperty());
        viewer.prefHeightProperty().bind(root.widthProperty());
        Pane chartPane = new Pane(viewer);
        root.getChildren().add(chartPane);

    }
}