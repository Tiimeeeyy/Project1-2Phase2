package engine.solvers;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.imageio.ImageIO;

import engine.parser.ExpressionParser;

import java.awt.Color;

/**
 * The type Golfgame.
 */
public class golfgame {
    private double minDis=100;
    private boolean goal=false;
    private double[] minCoordinate=new double[2];

    /**
     * Simulate the ball movement
     *
     * @param solver  the solver is going to be used. 
     * @param x       the ball current status, coordinate and the velocity
     * @param a       the frictions
     * @param dt      the time step
     * @param hole    the hole location
     * @param r       the radius of hole
     * @param mappath the dir path for the map image
     * @return the array list of the trajectory of the ball movement
     */
    public ArrayList<double[]> shoot(MySolver solver, double[] x, double[] a, double dt,double[] hole, double r, String mappath){
        ArrayList<double[]> xtrac=new ArrayList<double[]>();
        MapHandler map=new MapHandler();
        xtrac.clear();
        xtrac.add(x.clone());
        
        
        
        //Read the map, store the gradient 
        double[][][] mapgradient=map.readmap(mappath);
        double dis=100;

        //loop untill ball stop or out of court
        while (!solver.nextstep(new golfphysics(),x,a,mapgradient[(int)Math.floor(x[0]*10)][(int)Math.floor(x[1]*10)],dt)) {
            //check whether out of court
            if ((int)Math.floor(x[0]*10)>=mapgradient.length || (int)Math.floor(x[1]*10)>=mapgradient.length || (int)Math.floor(x[0]*10) <0 || (int)Math.floor(x[1]*10)<0) {
                break;
            }
            dis=getDistance(x, hole);
            if(dis<r){
                System.out.println("Goal!!!");
                this.goal=true;
                break;
            }
            if(dis<this.minDis){
                this.minDis=dis;
                this.minCoordinate=x;
            }
            xtrac.add(x.clone());
        }

        return xtrac;
    }

    /**
     * Get distance between two points.
     *
     * @param src the cource
     * @param des the desination
     * @return the distance
     */
    public double getDistance (double[] src, double[] des){
        return Math.sqrt(Math.pow(des[0]-src[0], 2)+Math.pow(des[1]-src[1], 2));
    }

    /**
     * Get min distance between the ball and the hole thoughout the whole trajectory.
     *
     * @return the minimune distance
     */
    double getMinDistance(){
        return this.minDis;
    }

    /**
     * Get the location where the ball is the nearest to the hole.
     *
     * @return the coordinate in array
     */
    double[] getMinCoordinate(){
        return this.minCoordinate;
    }

    /**
     * Is goal?.
     *
     * @return the boolean
     */
    boolean isGoal(){
        return this.goal;
    }

    /**
     * Readmap double [ ] [ ] [ ].
     *
     * @param mappath the mappath
     * @return the double [ ] [ ] [ ]
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
                    gradient[i][j][k]=(double) -(gAry[i+1-k][j+k]-gAry[i][j])/12.75; //scaled down, if (0-255)/12.75 then (0-20)
                }
            }
        }
        return gradient;
        
    }

    /**
     * Plot trajectory.
     *
     * @param sourceMap  the source map
     * @param plotMap    the plot map
     * @param trajectory the trajectory
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
     * @param desPath the des path
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

    private int heightFunction(double x, double y){
        // translate x,y from (0,500) to (-10,10), 
        int h=(int) (255-(((0.4*(0.9-Math.exp(-(Math.pow(x/50-5, 2)+Math.pow(y/50-5, 2))/8))))*200+80));


        String func = "255 - ((0.4 * (0.9 - e^(-(((x / 50 - 5)^2 + (y / 50 - 5)^2) / 8)))) * 200 + 80)";
        Map<String, Double> initVars = new HashMap<>();
        initVars.put("x", x);
        initVars.put("y", y);
        ExpressionParser parser = new ExpressionParser(func, initVars);
        // System.out.println(parser.evaluate());
        return (int) parser.evaluate();
    }
}
