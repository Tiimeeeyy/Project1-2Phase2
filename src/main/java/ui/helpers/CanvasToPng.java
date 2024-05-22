package ui.helpers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Helper class for handling the canvas drawing and saving functionality.
 */
public class CanvasToPng {

    @FXML
    private Canvas drawingCanvas;

    /**
     * Constructor to initialize the CanvasToPng with the specified drawing canvas.
     *
     * @param drawingCanvas the canvas to be used for drawing and saving
     */
    public CanvasToPng(Canvas drawingCanvas) {
        this.drawingCanvas = drawingCanvas;
    }

    /**
     * Saves the current state of the canvas as a PNG file.
     */
    @FXML
    public void saveCanvasAsPNG() {
        try {
            BufferedImage bufferedImage = convertCanvasToBufferedImage();
            drawBordersOnCanvas();
            BufferedImage finalImage = convertCanvasToBufferedImage();

            saveBufferedImageToFile(finalImage);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed", "An error occurred while saving the canvas: " + e.getMessage());
        }
    }

    /**
     * Converts the current state of the canvas to a BufferedImage.
     *
     * @return the BufferedImage representation of the canvas
     */
    private BufferedImage convertCanvasToBufferedImage() {
        WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
        drawingCanvas.snapshot(null, writableImage);

        BufferedImage bufferedImage = new BufferedImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        PixelReader pixelReader = writableImage.getPixelReader();

        for (int y = 0; y < writableImage.getHeight(); y++) {
            for (int x = 0; x < writableImage.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }

    /**
     * Draws borders on the canvas.
     */
    private void drawBordersOnCanvas() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 150));
        gc.setLineWidth(12);

        gc.fillRect(0, 0, drawingCanvas.getWidth(), 2);
        gc.fillRect(0, drawingCanvas.getHeight() - 2, drawingCanvas.getWidth(), 2);
        gc.fillRect(0, 0, 2, drawingCanvas.getHeight());
        gc.fillRect(drawingCanvas.getWidth() - 2, 0, 2, drawingCanvas.getHeight());
    }

    /**
     * Saves the given BufferedImage to a file.
     *
     * @param bufferedImage the BufferedImage to save
     * @throws IOException if an error occurs while saving the file
     */
    private void saveBufferedImageToFile(BufferedImage bufferedImage) throws IOException {
        String userDir = System.getProperty("user.dir");
        File resourcesDir = new File(userDir, "src/main/resources");
        if (!resourcesDir.exists()) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "The directory you are trying to save to does not exist.");
            return;
        }

        File file = new File(resourcesDir, "userInputMap.png");
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to delete existing file: " + file.getAbsolutePath());
        }

        if (!ImageIO.write(bufferedImage, "png", file)) {
            throw new IOException("Failed to write image to file: " + file.getAbsolutePath());
        }
    }

    /**
     * Displays an alert dialog with the specified type, title, and content.
     *
     * @param alertType the type of the alert
     * @param title     the title of the alert
     * @param content   the content of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
