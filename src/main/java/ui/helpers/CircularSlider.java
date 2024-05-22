package ui.helpers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * A custom control representing a circular slider.
 */
public class CircularSlider extends Region {

    private Canvas canvas; // Canvas for drawing the slider
    private DoubleProperty value; // Current value of the slider
    private DoubleProperty maxValue; // Maximum value of the slider

    /**
     * Constructor for CircularSlider.
     */
    public CircularSlider() {
        canvas = new Canvas(100, 100);
        value = new SimpleDoubleProperty(0);
        maxValue = new SimpleDoubleProperty(100);

        getChildren().add(canvas);
        drawSlider();

        // Set mouse event handlers
        canvas.setOnMouseDragged(this::handleMouse);
        canvas.setOnMousePressed(this::handleMouse);
    }

    /**
     * Draws the slider on the canvas.
     */
    private void drawSlider() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 5;

        // Draw the circular slider track
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Calculate the angle for the current value
        double angle = Math.toRadians((value.get() / maxValue.get()) * 360);
        double indicatorX = centerX + radius * Math.cos(angle - Math.PI / 2);
        double indicatorY = centerY + radius * Math.sin(angle - Math.PI / 2);

        // Draw the indicator
        gc.setFill(Color.RED);
        gc.fillOval(indicatorX - 3, indicatorY - 3, 6, 6);

        // Draw the arrow from the center to the indicator
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        gc.strokeLine(centerX, centerY, indicatorX, indicatorY);
    }

    /**
     * Handles mouse events for the slider.
     *
     * @param event the mouse event
     */
    private void handleMouse(MouseEvent event) {
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double dx = event.getX() - centerX;
        double dy = event.getY() - centerY;

        // Calculate the angle based on mouse position
        double angle = Math.atan2(dy, dx) + Math.PI / 2;
        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        // Convert the angle to a value
        double newValue = (angle / (2 * Math.PI)) * maxValue.get();
        value.set(newValue);
        drawSlider();
    }

    /**
     * Gets the value property.
     *
     * @return the value property
     */
    public DoubleProperty valueProperty() {
        return value;
    }

    /**
     * Gets the current value of the slider.
     *
     * @return the current value
     */
    public double getValue() {
        return value.get();
    }

    /**
     * Sets the current value of the slider.
     *
     * @param value the new value
     */
    public void setValue(double value) {
        this.value.set(value);
        drawSlider();
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public double getMaxValue() {
        return maxValue.get();
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param maxValue the new maximum value
     */
    public void setMaxValue(double maxValue) {
        this.maxValue.set(maxValue);
    }

    /**
     * Gets the direction vector based on the current value.
     *
     * @return the direction vector
     */
    public double[] getDirectionVector() {
        double angle = Math.toRadians((value.get() / maxValue.get()) * 360 - 90);
        double x = Math.cos(angle);
        double y = Math.sin(angle);
        return new double[]{x, -y};
    }
}
