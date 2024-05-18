package ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class CircularSlider extends Region {

    private Canvas canvas;
    private DoubleProperty value;
    private DoubleProperty maxValue;

    public CircularSlider() {
        canvas = new Canvas(100, 100);
        value = new SimpleDoubleProperty(0);
        maxValue = new SimpleDoubleProperty(100);

        getChildren().add(canvas);
        drawSlider();

        canvas.setOnMouseDragged(this::handleMouse);
        canvas.setOnMousePressed(this::handleMouse);
    }

    private void drawSlider() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 5;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        double angle = Math.toRadians(value.get() * 360 / maxValue.get());
        double indicatorX = centerX + radius * Math.cos(angle);
        double indicatorY = centerY + radius * Math.sin(angle);

        gc.setFill(Color.RED);
        gc.fillOval(indicatorX - 3, indicatorY - 3, 6, 6);

        // Drawing the arrow from the center to the indicator
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        gc.strokeLine(centerX, centerY, indicatorX, indicatorY);
    }

    private void handleMouse(MouseEvent event) {
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double dx = event.getX() - centerX;
        double dy = event.getY() - centerY;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        double newValue = angle * maxValue.get() / (2 * Math.PI);
        value.set(newValue);
        drawSlider();
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        this.value.set(value);
        drawSlider();
    }

    public double getMaxValue() {
        return maxValue.get();
    }

    public void setMaxValue(double maxValue) {
        this.maxValue.set(maxValue);
    }

    public double[] getDirectionVector() {
        double angle = Math.toRadians(value.get() * 360 / maxValue.get());
        double x = Math.cos(angle);
        double y = Math.sin(angle);
        return new double[]{x, y};
    }
}
