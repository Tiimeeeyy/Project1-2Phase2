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

public class MapHandler {
    private int[][] redAry;
    private int[][] blueAry;
    private double[][][] gradient;
    private boolean[][] treeAry;
    /**
     * Read the map and store the gradient.
     *
     * @param mappath the dir path
     */
    public void readmap(String mappath){
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File input_file = new File(mappath);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(input_file);
            width=image.getWidth();
            height=image.getHeight();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        int[][] gAry=new int[width][height];
        redAry=new int[width][height];
        blueAry=new int[width][height];
        treeAry=new boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j); // Get the RGB value at a specific pixel
                redAry[i][j] = (rgb >> 16) & 0xFF;   // Red component, Red is the friction, 0-255
                gAry[i][j] = (rgb >> 8) & 0xFF;    // Green component, Green is the height, 0-255. 
                blueAry[i][j] = rgb & 0xFF; 
                if (rgb==-8897501) {
                    treeAry[i][j]=true;
                }else{
                    treeAry[i][j]=false;
                }
            }
        }

        gradient=new double[width][height][2];
        for (int i = 0; i < width-1; i++) {
            for (int j = 1; j < height; j++) {
                for(int k=0;k<2;k++){
                    if(treeAry[i+1-k][j-k] || gAry[i+1-k][j-k]==0){
                        gradient[i][j][k]=0;
                    }else if(treeAry[i][j] || gAry[i][j]==0){
                        gradient[i][j][k]=0;
                    }
                    else{
                        gradient[i][j][k]=Utility.colorToHeight(gAry[i+1-k][j-k])-Utility.colorToHeight(gAry[i][j]) ;
                    }
                }
            }
        }
        
    }
    public int[][] getRed(){
        return this.redAry;
    }
    public int[][] getBlue(){
        return this.blueAry;
    }
    public double[][][] getGradient(){
        return this.gradient;
    }
    public boolean[][] getTree(){
        return this.treeAry;
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
                image.setRGB(Utility.coordinateToPixel_X(trajectory.get(i)[0]), Utility.coordinateToPixel_Y(trajectory.get(i)[1]), Color.RED.getRGB());
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
                    Color color=new Color(0,heightFunction(Utility.pixelToCoordinate_X(i), Utility.pixelToCoordinate_Y(j)),0);
                    image.setRGB(i, j, color.getRGB());
                }
            }
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
                        colortemp=new Color(120,60,35);
                    }else if (r>100) {
                        colortemp=new Color(160,g,0);
                    }else if (b>100) {
                        colortemp=new Color(0,g,180);
                    }
                    image.setRGB(i,j,colortemp.getRGB());
                }
            }
            int intR=(int) Math.floor(radius*Utility.ratio);
            Color black=new Color(0,0,0);
            for (int i = -intR; i <= intR; i++) {
                for (int j =0; j <= Math.round(Math.sqrt(Math.pow(intR, 2)-Math.pow(i, 2))); j++) {
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
        // int h=(int) (255-(((0.4*(0.9-Math.exp(-(Math.pow(x/50-5, 2)+Math.pow(y/50-5, 2))/8))))*200+80));


        String func = "255 - ((0.4 * (0.9 - e^(-(((x / 50 - 5)^2 + (y / 50 - 5)^2) / 8)))) * 200 + 80)";
        Map<String, Double> initVars = new HashMap<>();
        initVars.put("x", x);
        initVars.put("y", y);
        ExpressionParser parser = new ExpressionParser(func, initVars);
        
        return (int) parser.evaluate();
    }

}
