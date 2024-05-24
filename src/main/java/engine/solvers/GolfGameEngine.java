package engine.solvers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import engine.solvers.odeFunctions.FunctionInterface;
import engine.solvers.odeFunctions.golfphysics;
import engine.solvers.odeSolvers.SolverInterface;

/** 
 * The main engine
*/
public class GolfGameEngine {
    private double minDis=100;
    private boolean goal=false;
    private double[] minCoordinate=new double[4];
    private double[] stopCoordinate=new double[4];
    private String message;
    private boolean treeHit=false;

    private SolverInterface solver;
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
    public GolfGameEngine(SolverInterface solver, double[] a, double dt,double[] hole, double r, String mappath){
        this.solver=solver;
        this.a=a;
        this.dt=dt;
        this.hole=hole;
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
        
        ArrayList<double[]> xtrac=new ArrayList<double[]>();
        double[] x0=x.clone();
        MapHandler map=new MapHandler();
        FunctionInterface golfPhysics=new golfphysics();
        double[] fric=a.clone();
        
        //clear previous
        this.message="";
        this.goal=false;
        this.treeHit=false;
        xtrac.clear();
        xtrac.add(x.clone());
        
        //Read the map 
        map.readmap(mappath);
        double[][][] mapgradient=map.getGradient();
        int[][] redElm=map.getRed();
        int[][] blueElm=map.getBlue();
        boolean[][] treeAry=map.getTree();
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
                System.out.println("Out of boundary!");
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
                this.minDis=0;
                this.goal=true;
                break;
            }
            //check whether in water.
            if (blueElm[pixelX][pixelY]>=100) {
                this.message="In Water! Start again.";
                x=x0.clone();
                this.stopCoordinate=x0.clone();
                if (recording) {
                    xtrac.add(x.clone());
                }
                break;
            }
            //ckeck hit the tree
            if (treeAry[pixelX][pixelY]) {
                if (!bounceCheck) {
                    this.treeHit=true;
                    double[] normVec=findTreeNormVec(pixelX, pixelY, treeAry);
                    bouncingHandler(x, normVec);
                    bounceCheck=true;
                }
            }else{
                bounceCheck=false;
            }
            //check if in sand
            if (redElm[pixelX][pixelY]>=100 && blueElm[pixelX][pixelY]==0) {
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
        }
        return xtrac;
    }

    public double getDistance (double[] src, double[] des){
        return Math.sqrt(Math.pow(des[0]-src[0], 2)+Math.pow(des[1]-src[1], 2));
    }

    public double getMinDistance(){
        return this.minDis;
    }

    public double[] getMinCoordinate(){
        return this.minCoordinate;
    }
    
    public boolean getTreeHit(){
        return this.treeHit;
    }

    public boolean isGoal(){
        return this.goal;
    }

    public double getHoleBallDistance(double[] x){
        return Math.sqrt(Math.pow(x[0]-hole[0], 2)+Math.pow(x[1]-hole[1], 2));
    }

    public double[] getHole(){
        return this.hole;
    }

    public double[] getStoppoint(){
        return this.stopCoordinate;
    }

    public String getMessage(){
        return this.message;
    }

    /**
     * Calculate boucning. adjust x object directly.
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
     * Find the norm vector for hiting surface of a tree object
     * @param x the pixel coordinate of the hitting point
     * @param y
     * @param treeAry
     * @return
     */
    private double[] findTreeNormVec(int x, int y,boolean[][] treeAry){
        double[] normVec ={Utility.pixelToCoordinate_X(x),Utility.pixelToCoordinate_Y(y)};
        Set<Integer> xSet=new HashSet<>();
        Set<Integer> ySet=new HashSet<>();
        // int pixelX=Utility.coordinateToPixel_X(x);
        // int pixelY=Utility.coordinateToPixel_Y(y);
        findTree(x, y, xSet, ySet, treeAry);
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
     * @param y
     * @param xSet  collection of pixels' x coordinate for the hitting tree
     * @param ySet  collection of pixels' y coordinate for the hitting tree
     * @param treeAry
     */
    private void findTree(int x, int y, Set<Integer> xSet, Set<Integer> ySet,boolean[][] treeAry){
        if (xSet.contains(x) && ySet.contains(y)) {
            return;
        }
        if (treeAry[x][y]) {
            xSet.add(x);
            ySet.add(y);
            findTree(x-1, y, xSet, ySet, treeAry);
            findTree(x, y-1, xSet, ySet, treeAry);
            findTree(x+1, y, xSet, ySet, treeAry);
            findTree(x, y+1, xSet, ySet, treeAry);
        }else{
            return;
        }
    }

    private double dotProduct(double[] a, double[] b){
        return a[0]*b[0]+a[1]*b[1];
    }

    private double[] dotProduct(double[] a, double b){
        double[] c={a[0]*b,a[1]*b};
        return c;
    }

    private double norm(double[] a){
        return Math.sqrt(Math.pow(a[0], 2)+Math.pow(a[1], 2));
    }

}
