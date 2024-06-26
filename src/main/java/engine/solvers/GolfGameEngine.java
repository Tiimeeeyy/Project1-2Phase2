package engine.solvers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import engine.solvers.odeFunctions.FunctionInterface;
import engine.solvers.odeFunctions.golfphysics;
import engine.solvers.odeSolvers.SolverInterface;

/** 
 * The GolfGameEngine class is the main engine for the golf game simulation.
 * It utilises the SolverInterface to solve the ordinary differential equations (ODEs) that model the physics of the game.
 * The engine keeps track of the state of the game including the position and velocity of the ball,
 * the position of the hole and whether the ball has hit the hole.
*/
public class GolfGameEngine {
    private double minDis=100;
    private boolean goal=false;
    private double[] minCoordinate=new double[4];
    private double[] stopCoordinate=new double[4];
    private String message;
    private boolean treeHit=false;
    private BallStatus status=BallStatus.Normal;

    private SolverInterface solver;
    private double[] a;
    private double dt;
    private double[] hole;
    private double r;
    private String mappath;
    
    //about map
    private double[][][] mapgradient;
    private TerrainType[][] terrain;

    /**
     * Construct the game engine
     * 
     * @param solver    The ODE solver to be used.
     * @param a         The friction coefficients, [kinetic fraction, static friction]
     * @param dt        The time step
     * @param hole      The position of the hole
     * @param r         The radius of the hole
     * @param mappath   The path of the map
     */
    public GolfGameEngine(SolverInterface solver, double[] a, double dt,double[] hole, double r, String mappath){
        this.solver=solver;
        this.a=a;
        this.dt=dt;
        this.hole=hole;
        this.r=r;
        this.mappath=mappath;

        MapHandler map=new MapHandler();
        map.readmap(mappath);
        mapgradient=map.getGradient();
        terrain=map.getTerrain();
        
        
    }


    /**
     * The shoot method simulates a shot in the golf game.
     * It calculates the trajectory of the ball and checks if the ball has hit a tree, landed in water, or reached the hole.
     * 
     * @param x             The starting position and velocity of the ball
     * @param recording     Whether save the trajectory. Disable recording in AI bot calculation to save memory.
     * @return              The trajectory of the ball. It is null if recording is false.
     */
    public ArrayList<double[]> shoot(double[] x,Boolean recording){
        
        ArrayList<double[]> xtrac=new ArrayList<double[]>();
        double[] x0=x.clone();
        MapHandler map=new MapHandler();
        FunctionInterface golfPhysics=new golfphysics();
        double[] fric=a.clone();
        
        //clear previous
        this.message="";
        this.goal=false;
        this.treeHit=false;
        this.stopCoordinate=new double[4];
        this.minCoordinate=new double[4];
        this.minDis=100;
        xtrac.clear();
        xtrac.add(x.clone());
        
        //Read the map 
        map.readmap(mappath);
        
        boolean bounceCheck=false;
        this.minDis=getDistance(x, hole);
        double dis=100;

        int pixelX=Utility.coordinateToPixel_X(x[0]);
        int pixelY=Utility.coordinateToPixel_Y(x[1]);
        //loop untill ball stop or out of court
        while (!solver.nextstep(golfPhysics,x,fric,mapgradient[pixelX][pixelY],dt)) {
            pixelX=Utility.coordinateToPixel_X(x[0]);
            pixelY=Utility.coordinateToPixel_Y(x[1]);
            //check whether out of court
            if (pixelX>=mapgradient.length || pixelY>=mapgradient[0].length || pixelX<0 || pixelY<0) {
                this.message="Out of boundary!";
                status=BallStatus.OutOfBoundary;
                x=x0.clone();
                this.stopCoordinate=x0.clone();
                if (recording) {
                    xtrac.add(x.clone());
                }
                break;
            }
            //check goal or not
            dis=getDistance(x, this.hole);
            if(dis<r){
                this.message="Goal!!!";
                status=BallStatus.Goal;
                this.minDis=0;
                this.goal=true;
                break;
            }
            //check whether in water.
            if (terrain[pixelX][pixelY].equals(TerrainType.Water)) {
                this.message="In Water! Start again.";
                status=BallStatus.HitWater;
                x=x0.clone();
                this.stopCoordinate=x0.clone();
                if (recording) {
                    xtrac.add(x.clone());
                }
                break;
            }
            //ckeck hit the tree
            if (terrain[pixelX][pixelY].equals(TerrainType.Tree)) {
                if (!bounceCheck) {
                    this.treeHit=true;
                    double[] normVec=findTreeNormVec(pixelX, pixelY, terrain);
                    bouncingHandler(x, normVec);
                    bounceCheck=true;
                }
            }else{
                bounceCheck=false;
            }
            //check if in sand
            if (terrain[pixelX][pixelY].equals(TerrainType.Sand)) {
                fric[0]=3*a[0];
                fric[1]=3*a[1];
            }else{
                fric[0]=a[0];
                fric[1]=a[1];
            }
            
            //log closet
            if(dis<this.minDis){
                this.minDis=dis;
                this.minCoordinate=x;
            }
            if (recording) {
                xtrac.add(x.clone());
            }
            this.stopCoordinate=x.clone();
            status=BallStatus.Normal;
        }
        return xtrac;
    }

    public double getDistance (double[] src, double[] des){
        return Math.sqrt(Math.pow(des[0]-src[0], 2)+Math.pow(des[1]-src[1], 2));
    }

    /**
     * Gets the minimum distance.
     * @return The minimum distance.
     */
    public double getMinDistance(){
        return this.minDis;
    }

    /**
     * Gets the minimum coordinate.
     * @return The minimum coordinate.
     */
    public double[] getMinCoordinate(){
        return this.minCoordinate;
    }

    /**
     * Checks if the ball has hit a tree.
     * @return True if the ball hit a tree, false otherwise.
     */
    public boolean getTreeHit(){
        return this.treeHit;
    }

    /**
     * Checks if the ball has reached the goal.
     * @return True if it has reached the goal, false otherwise.
     */
    public boolean isGoal(){
        return this.goal;
    }

    public double getStoppedToHoleDistance(){
        return getHoleBallDistance(this.stopCoordinate);
    }

    public String getMapPath(){
        return this.mappath;
    }

    /**
     * Calculates the distance between the hole and the ball.
     * @param x Current position of the ball.
     * @return The distance between the hole and the ball.
     */
    public double getHoleBallDistance(double[] x){
        return Math.sqrt(Math.pow(x[0]-hole[0], 2)+Math.pow(x[1]-hole[1], 2));
    }

    /**
     * Gets the location of the hole.
     * @return The location of the hole.
     */
    public double[] getHole(){
        return this.hole;
    }

    /**
     * Gets the point at which the ball has stopped moving
     * @return The location at which the ball has stopped moving
     */
    public double[] getStoppoint(){
        return this.stopCoordinate;
    }

    /**
     * Gets the message.
     * @return The message.
     */
    public String getMessage(){
        return this.message;
    }

    /**
     * Gets the status of the ball.
     * @return The status of the ball.
     */
    public BallStatus getStatus(){
        return this.status;
    }

    public double getHoleRadius(){
        return this.r;
    }

    /**
     * Calculate bouncing. adjust x object directly.
     * 
     * @param x inbound ball status
     * @param normVec   norm vector for the bouncing surface
     */
    private void bouncingHandler(double[] x, double[] normVec){
        double coe=0.8;                                             //velocity lose ratio from bouncing
        double[] inVec={-x[2],-x[3]};
        double[] inReflect=dotProduct(normVec, 2*dotProduct(inVec, normVec)/Math.pow(norm(normVec),2));
        x[2]=(inReflect[0]-inVec[0])*coe;
        x[3]=(inReflect[1]-inVec[1])*coe;
    }

    /**
     * Find the norm vector for hitting surface of a tree object
     * @param x The pixel coordinate (x) of the hitting point.
     * @param y The pixel coordinate (y) of the hitting point.
     * @param treeAry The locations of the trees.
     * @return The norm-vector of the collision surface.
     */
    private double[] findTreeNormVec(int x, int y,TerrainType[][] terrain){
        double[] normVec ={Utility.pixelToCoordinate_X(x),Utility.pixelToCoordinate_Y(y)};
        Set<Integer> xSet=new HashSet<>();
        Set<Integer> ySet=new HashSet<>();
        // int pixelX=Utility.coordinateToPixel_X(x);
        // int pixelY=Utility.coordinateToPixel_Y(y);
        findTree(x, y, xSet, ySet, terrain);
        int xSum=0;
        for (Integer i : xSet) {
            xSum=xSum+i;
        }
        int ySum=0;
        for (Integer i : ySet) {
            ySum=ySum+i;
        }
        int xCenter=(int) xSum/xSet.size();
        int yCenter=(int) ySum/ySet.size();
        normVec[0]=normVec[0]-Utility.pixelToCoordinate_X(xCenter);
        normVec[1]=normVec[1]-Utility.pixelToCoordinate_Y(yCenter);
        return normVec;
    }

    /**
     * Calculate the center of the currenct hitting tree
     * 
     * @param x the hitting point. indicate which tree is hitting. in pixel coordinate
     * @param y "-"
     * @param xSet  The collection of pixels' x coordinate for the hitting tree
     * @param ySet  The collection of pixels' y coordinate for the hitting tree
     * @param treeAry The location information of the trees in the map.
     */
    private void findTree(int x, int y, Set<Integer> xSet, Set<Integer> ySet,TerrainType[][] terrain){
        if (xSet.contains(x) && ySet.contains(y)) {
            return;
        }
        if (terrain[x][y].equals(TerrainType.Tree)) {
            xSet.add(x);
            ySet.add(y);
            findTree(x-1, y, xSet, ySet, terrain);
            findTree(x, y-1, xSet, ySet, terrain);
            findTree(x+1, y, xSet, ySet, terrain);
            findTree(x, y+1, xSet, ySet, terrain);
        }else{
            return;
        }
    }

    /**
     * Case specific dot product calculation.
     * @param a The first vector.
     * @param b The second vector.
     * @return The dot product of a and b.
     */
    private double dotProduct(double[] a, double[] b){
        return a[0]*b[0]+a[1]*b[1];
    }

    /**
     * Calculates the dot product of a vector with a value.
     * @param a The vector.
     * @param b The value.
     * @return The dot product of a and b.
     */
    private double[] dotProduct(double[] a, double b){
        double[] c={a[0]*b,a[1]*b};
        return c;
    }

    /**
     * Normalises the inputted vector.
     * @param a The input vector
     * @return The norm of that input vector.
     */
    private double norm(double[] a){
        return Math.sqrt(Math.pow(a[0], 2)+Math.pow(a[1], 2));
    }

}

