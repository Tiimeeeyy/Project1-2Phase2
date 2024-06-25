package engine.solvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.parser.ExpressionParser;

import java.awt.Color;

/**
 * The MapHandler class is responsible for reading, processing, and creating golf course maps.
 */
public class MapHandler {
    private double[][][] gradient;
    private TerrainType[][] terrain;

    /**
     * Reads the map from the specified path and stores the terrain and gradient information.
     *
     * @param mapPath the path to the map file
     */
    public void readmap(String mapPath) {
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File inputFile = new File(mapPath);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(inputFile);
            width = image.getWidth();
            height = image.getHeight();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        int[][] gAry = new int[width][height];
        terrain = new TerrainType[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                int red = (rgb >> 16) & 0xFF;
                gAry[i][j] = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                if (rgb == -8897501) {
                    terrain[i][j] = TerrainType.Tree;
                } else if (blue >= 100) {
                    terrain[i][j] = TerrainType.Water;
                } else if (red >= 100) {
                    terrain[i][j] = TerrainType.Sand;
                } else {
                    terrain[i][j] = TerrainType.Grass;
                }
            }
        }

        gradient = new double[width][height][2];
        for (int i = 0; i < width - 1; i++) {
            for (int j = 1; j < height; j++) {
                for (int k = 0; k < 2; k++) {
                    if (terrain[i + 1 - k][j - k].equals(TerrainType.Tree) || gAry[i + 1 - k][j - k] == 0) {
                        gradient[i][j][k] = 0;
                    } else if (terrain[i][j].equals(TerrainType.Tree) || gAry[i][j] == 0) {
                        gradient[i][j][k] = 0;
                    } else {
                        gradient[i][j][k] = Utility.colorToHeight(gAry[i + 1 - k][j - k]) - Utility.colorToHeight(gAry[i][j]);
                    }
                }
            }
        }
    }

    /**
     * Returns the gradient information.
     *
     * @return a 3D array representing the gradient information
     */
    public double[][][] getGradient() {
        return this.gradient;
    }

    /**
     * Returns the terrain information.
     *
     * @return a 2D array representing the terrain information
     */
    public TerrainType[][] getTerrain() {
        return this.terrain;
    }

    /**
     * Plots the trajectory on the map and saves it to a specified file.
     *
     * @param sourceMap  the source map file path
     * @param plotMap    the output map file path
     * @param trajectory the ball trajectory
     */
    public void plotTrajectory(String sourceMap, String plotMap, ArrayList<double[]> trajectory) {
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File inputFile = new File(sourceMap);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(inputFile);
            width = image.getWidth();
            height = image.getHeight();
            System.out.println("map read to plot");
            for (double[] point : trajectory) {
                image.setRGB(Utility.coordinateToPixel_X(point[0]), Utility.coordinateToPixel_Y(point[1]), Color.RED.getRGB());
            }

            File outputFile = new File(plotMap);
            ImageIO.write(image, "png", outputFile);

        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Creates a new map with height values and saves it to a specified file.
     *
     * @param desPath the output file path
     */
    public void createMap(String desPath) {
        int width = 500;
        int height = 500;
        BufferedImage image = null;

        try {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Color color = new Color(0, heightFunction(Utility.pixelToCoordinate_X(i), Utility.pixelToCoordinate_Y(j)), 0);
                    image.setRGB(i, j, color.getRGB());
                }
            }
            File outputFile = new File(desPath);
            ImageIO.write(image, "png", outputFile);

        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Renders the map with initial green values and the hole position.
     *
     * @param initialGreen the initial green values
     * @param mapPath      the map file path
     * @param hole         the hole position
     * @param radius       the radius of the hole
     * @param drawHole     whether to draw the hole
     */
    public void renderMap(int[][] initialGreen, String mapPath, double[] hole, double radius, boolean drawHole) {
        BufferedImage image = null;
        int width = 500;
        int height = 500;
        int[] pixelHole = Utility.coordinateToPixel(hole);
        try {
            File inputFile = new File(mapPath);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(inputFile);
            width = image.getWidth();
            height = image.getHeight();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int rgb = image.getRGB(i, j);
                    int r = (rgb >> 16) & 0xFF;
                    int b = rgb & 0xFF;
                    int g = initialGreen[i][j];
                    Color colorTemp = new Color(0, g, 0);
                    if (r > 30 && b > 30) {
                        colorTemp = new Color(120, 60, 35);
                    } else if (r > 100) {
                        colorTemp = new Color(160, g, 0);
                    } else if (b > 100) {
                        colorTemp = new Color(0, g, 180);
                    }
                    image.setRGB(i, j, colorTemp.getRGB());
                }
            }
            if (drawHole) {
                int intR = (int) Math.floor(radius * Utility.ratio);
                Color black = new Color(0, 0, 0);
                for (int i = -intR; i <= intR; i++) {
                    for (int j = 0; j <= Math.round(Math.sqrt(Math.pow(intR, 2) - Math.pow(i, 2))); j++) {
                        image.setRGB(pixelHole[0] + i, pixelHole[1] + j, black.getRGB());
                        image.setRGB(pixelHole[0] + i, pixelHole[1] - j, black.getRGB());
                    }
                }
            }

            File outputFile = new File(mapPath);
            ImageIO.write(image, "png", outputFile);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Height function to determine the height value for a given coordinate.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the height value
     */
    private int heightFunction(double x, double y) {
        String func = "255 - ((0.4 * (0.9 - e^(-(((x / 50 - 5)^2 + (y / 50 - 5)^2) / 8)))) * 200 + 80)";
        Map<String, Double> initVars = new HashMap<>();
        initVars.put("x", x);
        initVars.put("y", y);
        ExpressionParser parser = new ExpressionParser(func, initVars);

        return (int) parser.evaluate();
    }
}
