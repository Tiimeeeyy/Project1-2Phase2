package solvers;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import java.awt.Color;

public class golfgame {
    private double minDis=100;
    private boolean goal=false;
    private double[] minCoordinate=new double[2];

    public ArrayList<double[]> shoot(MySolver solver, double[] x, double[] a, double dt,double[] hole, double r, String mappath){
        ArrayList<double[]> xtrac=new ArrayList<double[]>();
        xtrac.clear();
        xtrac.add(x.clone());
        
        
        
        //Read the map, store the gradient 
        double[][][] mapgradient=readmap(mappath);
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

    public double getDistance (double[] src, double[] des){
        return Math.sqrt(Math.pow(des[0]-src[0], 2)+Math.pow(des[1]-src[1], 2));
    }
    double getMinDistance(){
        return this.minDis;
    }
    double[] getMinCoordinate(){
        return this.minCoordinate;
    }
    boolean isGoal(){
        return this.goal;
    }

    public double[][][] readmap(String mappath){
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File input_file = new File(mappath);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(getClass().getResource(mappath));
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
                    gradient[i][j][k]=(double) (gAry[i+1-k][j+k]-gAry[i][j])/30; //scaled down, if (0-255)/12.75 then (0-20)
                }
            }
        }
        return gradient;
        
    }

    public void plotTrajectory(String sourceMap,String plotMap, ArrayList<double[]> trajectory){
        int width = 20;
        int height = 20;
        BufferedImage image = null;

        try {
            File input_file = new File(sourceMap);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(getClass().getResource(sourceMap));
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
}
