package engine.solvers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import engine.parser.ExpressionParser;
import javafx.scene.image.Image;

import java.awt.Color;

public class MapHandler {
    /**
     * Read the map and store the gradient.
     *
     * @param mappath the dir path
     * @return the gradient array double [ ] [ ] [ ]
     */
    public double[][][] readmap(String mappath){
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File input_file = new File(mappath);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(input_file);
            width=image.getWidth();
            height=image.getHeight();
            System.out.println("map readed");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        int[][] gAry=new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j); // Get the RGB value at a specific pixel
                int r = (rgb >> 16) & 0xFF;   // Red component, Red is the friction, 0-255
                gAry[i][j] = (rgb >> 8) & 0xFF;    // Green component, Green is the height, 0-255. 
                int b = rgb & 0xFF; 
            }
        }

        double[][][] gradient=new double[width][height][2];
        for (int i = 0; i < width-1; i++) {
            for (int j = 0; j < height-1; j++) {
                for(int k=0;k<2;k++){
                    gradient[i][j][k]=Utility.colorToHeight(gAry[i+1-k][j+k])-Utility.colorToHeight(gAry[i][j]) ; //scaled down, if (0-255)/12.75 then (0-20)
                }
            }
        }
        return gradient;
        
    }

    /**
     * Plot trajectory.
     *
     * @param sourceMap  the input path
     * @param plotMap    the output path 
     * @param trajectory the ball trajectory
     */
    public void plotTrajectory(String sourceMap,String plotMap, ArrayList<double[]> trajectory){
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File input_file = new File(sourceMap);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(input_file);
            width=image.getWidth();
            height=image.getHeight();
            System.out.println("map readed to plot");
            for (int i = 0; i < trajectory.size(); i++) {
                image.setRGB((int) Math.floor(trajectory.get(i)[0]*10), (int) Math.floor(trajectory.get(i)[1]*10), Color.RED.getRGB());
            }
            
            File outputfile=new File(plotMap);
            ImageIO.write(image, "png", outputfile);

        } catch (IOException e) {
            System.out.println("Error: " + e);
        }


    }

    /**
     * Create map.
     *
     * @param desPath the output path
     */
    public void createMap(String desPath){
        int width= 500;
        int height=500;
        BufferedImage image = null;

        try {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < 500; i++) {
                for (int j = 0; j < 500; j++) {
                    Color color=new Color(0,heightFunction(i, j),0);
                    image.setRGB(i, j, color.getRGB());
                }
            }
            System.out.println("map created");
            File outputfile=new File(desPath);
            ImageIO.write(image, "png", outputfile);

        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
    public void renderMap(int[][] initialGreen, String mappath, double[] hole,double radius){
        BufferedImage image = null;
        int width=500;
        int height=500;
        int[] pixelHole=Utility.coordinateToPixel(hole);
        try {
            File input_file = new File(mappath);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(input_file);
            width=image.getWidth();
            height=image.getHeight();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int rgb = image.getRGB(i, j); // Get the RGB value at a specific pixel
                    int r = (rgb >> 16) & 0xFF;   // Red component
                    int b = rgb & 0xFF; 
                    int g= (int) initialGreen[i][j]; // get the green
                    Color colortemp=new Color(0,g,0);
                    if (r>30 && b>30) {
                        colortemp=new Color(100,67,33);
                    }else if (r>100) {
                        colortemp=new Color(180,g,0);
                    }else if (b>100) {
                        colortemp=new Color(0,g,180);
                    }
                    image.setRGB(i,j,colortemp.getRGB());
                }
            }
            int intR=(int) Math.ceil(radius*10);
            Color black=new Color(0,0,0);
            for (int i = -intR; i <= intR; i++) {
                for (int j =0; j <= intR-Math.abs(i); j++) {
                    image.setRGB(pixelHole[0]+i, pixelHole[1]+j, black.getRGB());
                    image.setRGB(pixelHole[0]+i, pixelHole[1]-j, black.getRGB());
                }
            }
            File outputfile=new File(mappath);
            ImageIO.write(image, "png", outputfile);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private int heightFunction(double x, double y){
        // translate x,y from (0,500) to (-10,10), 
        int h=(int) (255-(((0.4*(0.9-Math.exp(-(Math.pow(x/50-5, 2)+Math.pow(y/50-5, 2))/8))))*200+80));


        String func = "255 - ((0.4 * (0.9 - e^(-(((x / 50 - 5)^2 + (y / 50 - 5)^2) / 8)))) * 200 + 80)";
        Map<String, Double> initVars = new HashMap<>();
        initVars.put("x", x);
        initVars.put("y", y);
        ExpressionParser parser = new ExpressionParser(func, initVars);
        
        return (int) parser.evaluate();
    }

}
