package ui;
import javafx.scene.layout.Pane;
import org.jfree.chart3d.Chart3D;
import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.data.function.Function3D;
import org.jfree.chart3d.data.function.Function3DUtils;
import org.jfree.chart3d.data.xyz.XYZDataset;
import org.jfree.chart3d.fx.Chart3DViewer;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HeightMap3DChart {
    private double[][] heightStorage;
    private Pane root;

    public HeightMap3DChart(double[][] heightStorage, Pane root) {
        this.heightStorage = heightStorage;
        this.root = root;
    }

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
    public void display3DChart() {
        Chart3DViewer viewer = createChart();
        viewer.prefWidthProperty().bind(root.widthProperty());
        viewer.prefHeightProperty().bind(root.widthProperty());
        Pane chartPane = new Pane(viewer);
        root.getChildren().add(chartPane);

    }
}