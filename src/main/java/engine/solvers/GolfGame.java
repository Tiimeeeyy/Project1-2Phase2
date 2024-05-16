package engine.solvers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * The main engine
*/
public class GolfGame {
    private double minDis=100;
    private boolean goal=false;
    private double[] minCoordinate=new double[4];
    private double[] stopCoordinate=new double[4];

    private MySolver solver;
    private double[] a;
    private double dt;
    private double[] hole;
    private double r;
    private String mappath;

    /**
     * Construct the game engine
     * 
     * @param solver    ODE sovler
     * @param a         friction coefficients, [knetic fraction, static friction]
     * @param dt        time step
     * @param hole      position of the hole
     * @param r         radius of the hole
     * @param mappath   the path of the map
     */
    public GolfGame(MySolver solver, double[] a, double dt,double[] hole, double r, String mappath){
        this.solver=solver;
        this.a=a;
        this.dt=dt;
        this.hole=hole;
        Utility.coordinateConvertor(this.hole);
        this.r=r;
        this.mappath=mappath;
    }

    /**
     * shoot!
     * 
     * @param x             the starting position and velocity of the ball
     * @param recording     whether save the trajectory. Disable recording in AI bot calculation to save memory. 
     * @return              The trajectory of the ball. It is null if recording is false.
     */
    public ArrayList<double[]> shoot(double[] x,Boolean recording){
        // cooridnateConvertor(x);
        Utility.coordinateConvertor(x);
        ArrayList<double[]> xtrac=new ArrayList<double[]>();
        xtrac.clear();
        xtrac.add(x.clone());
        MapHandler map=new MapHandler();
        MyFunction golfPhysics=new golfphysics();
        
        
        //Read the map, store the gradient 
        double[][][] mapgradient=map.readmap(mappath);
        this.minDis=getDistance(x, hole);
        double dis=100;
        //loop untill ball stop or out of court
        while (!solver.nextstep(golfPhysics,x,a,mapgradient[(int)Math.floor(x[0]*10)][(int)Math.floor(x[1]*10)],dt)) {
            //check whether out of court
            if ((int)Math.floor(x[0]*10)>=mapgradient.length || (int)Math.floor(x[1]*10)>=mapgradient.length || (int)Math.floor(x[0]*10) <0 || (int)Math.floor(x[1]*10)<0) {
                break;
            }
            dis=getDistance(x, this.hole);
            if(dis<r){
                System.out.println("Goal!!!");
                this.minDis=0;
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
            this.stopCoordinate=x.clone();
        }
        return xtrac;
    }
    /**
     * get the distance between two points
     * 
     * @param src   one of the point. Only read the src[0] and src[1] as coordination. 
     * @param des   the other point.
     * @return      the distance
     */
    public double getDistance (double[] src, double[] des){
        return Math.sqrt(Math.pow(des[0]-src[0], 2)+Math.pow(des[1]-src[1], 2));
    }

    /**
     * 
     * @return  the miminal distance throughout the whole trajectory
     */
    public double getMinDistance(){
        return this.minDis;
    }

    /**
     * 
     * @return  the nearst point
     */
    public double[] getMinCoordinate(){
        return this.minCoordinate;
    }

    /**
     * 
     * @return  whether goaled in this shot
     */
    public boolean isGoal(){
        return this.goal;
    }

    /**
     * 
     * @param x     the coordination of the ball
     * @return      the distance between the ball and the hole
     */
    public double getHoleBallDistance(double[] x){
        return Math.sqrt(Math.pow(x[0]-hole[0], 2)+Math.pow(x[1]-hole[1], 2));
    }

    /**
     * 
     * @return      the position of the ball
     */
    public double[] getHole(){
        return this.hole;
    }

    /**
     * 
     * @return      the ball stop position
     */
    public double[] getStoppoint(){
        return this.stopCoordinate;
    }

    
    
}
