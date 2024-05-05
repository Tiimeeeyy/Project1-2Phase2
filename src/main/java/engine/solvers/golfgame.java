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
    public ArrayList<double[]> shoot(MySolver solver, double[] x, double[] a, double dt,double[] hole, double r, String mappath, boolean recording){
        ArrayList<double[]> xtrac=new ArrayList<double[]>();
        MapHandler map=new MapHandler();
        xtrac.clear();
        xtrac.add(x.clone());
        
        
        //Read the map, store the gradient 
        double[][][] mapgradient=map.readmap(mappath);
        this.minDis=getDistance(x, hole);
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
            if (recording) {
                xtrac.add(x.clone());
            }
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

}
