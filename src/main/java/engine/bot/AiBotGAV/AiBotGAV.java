package engine.bot.AiBotGAV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import engine.bot.AibotGA.MapSearcher;
import engine.solvers.BallStatus;
import engine.solvers.GolfGameEngine;

public class AiBotGAV {
    private int popSize=240;
    private double[] solution=new double[4];
    private boolean goal=false;
    private ArrayList<double[]> shortestPath;
    private MapSearcher mapSearcher;

    private double[] target;
    private TargetType targetType;
    
    private boolean stuckCkecker=false;
    private int stuckCount=0;
    private double stuckMemory=0;

    private GolfGameEngine game;
    
    /**
     * Class constructor
     * @param game The engine to simulate the game.
     */
    public AiBotGAV(GolfGameEngine game){
        this.game=game;
        
    }

    /**
     * this method "plays" the golf game
     * @param x Initial state / initial position of the ball.
     */
   
    public ArrayList<double[]> golfBot(double x[]){
        double[] x0=x.clone();
        mapSearcher=new MapSearcher(game.getMapPath(), x0, game.getHole(), game.getHoleRadius());
        shortestPath=mapSearcher.findShortestPath();
        int shotNum=0;
        ArrayList<double[]> allSteps=new ArrayList<>();
        
        stuckCkecker=false;
        stuckMemory=0;
        stuckCount=0;
        while (mapSearcher.isObstacled(x0, game.getHole()) && shotNum<=20) {
            oneShot(x0,TargetType.FARSIGHT,null);
            game.shoot(solution.clone(), false);
            if (game.getStatus().equals(BallStatus.Normal) || game.getStatus().equals(BallStatus.Goal)) {
                allSteps.add(solution.clone());
            }
            x0=game.getStoppoint();

            // check if it stucks at the last 3 steps
            if (stuckCkecker) {
                System.out.println("stucked checked");
                target= getFarestPoint(game.getStoppoint(), shortestPath);
                oneShot(x0, TargetType.POINT, target);
                allSteps.add(solution.clone());
                game.shoot(solution.clone(), false);
                x0=game.getStoppoint();
            }
            
            System.out.println(Arrays.toString(solution));
            shotNum++;
        }
        if (!goal) {
            oneShot(x0, TargetType.HOLE, null);
            allSteps.add(solution.clone());
        }
        
        return allSteps;
    }
    /**
     * Calculate one step
     * 
     * @param x     ball position
     * @param targetType
     * @param target    will be ignored if targetTyoe is POINT or FARSIGHT
     */
    public void oneShot(double[] x, TargetType targetType, double[] target){
        IndividualV[] population=new IndividualV[popSize];
        double[] x0=x.clone();

        //clear previuos
        this.solution=new double[4];
        this.goal=false;

        switch (targetType) {
            case HOLE:
                this.target=game.getHole();
                this.targetType=TargetType.HOLE;
                break;
            case POINT:
                this.target=target;
                this.targetType=TargetType.POINT;
                break;
            case FARSIGHT:
                this.targetType=TargetType.FARSIGHT;
                break;
            default:
                System.out.println("something wrong in oneShot()");
                break;
        }
        
        
        
        // initiate population
        initialPopulation(population, x0);
        exploration(population[0],population[1], population, x0);
        
        for (int i = 0; i < 5; i++) {
            int[] selected=selection(population);
            followTrend(population[0], population[selected[0]], population, x0);
        }
        HeapSortV.sort(population);
        System.out.println(population[0].getFitness()+"  ");
        
        if (!this.goal) {
            double[] best=x0;
            best[2]=population[0].getChromosome()[0];
            best[3]=population[0].getChromosome()[1];
            this.solution=best.clone();
        }
        
        checkStuck(population[0]);
        
    }

    /**
     * THis method initialises the population for the genetic algorithm
     * @param pop The population size to be initialised.
     * @param x The initial state / position of the ball.
     */
    private void initialPopulation(IndividualV[] pop, double[] x){
       
        Random rand=new Random();
        int n=0;
        double[] farest=getFarestPoint(x, shortestPath);
        double powerMean=game.getDistance(x, farest)/5;
        for (int i = 0; i < 30; i++) {
            for (int j=0; j < 8; j++) {
                double power=Math.min(rand.nextGaussian()+powerMean, 5);
                pop[n]=new IndividualV(new double[]{power*Math.cos(2*i*Math.PI/30),power*Math.sin(2*i*Math.PI/30)});
                pop[n].setFitness(calculateFitness(pop[n], x));
                n++;
            }
        }
        
        HeapSortV.sort(pop);
        
    }

   

    private double calculateFitness(IndividualV indi, double[] x){
        double ball_target_distance;
        double fit = 0.1;
        double[] xin=new double[]{x[0],x[1],indi.getChromosome()[0],indi.getChromosome()[1]};
        double[] x0=xin.clone();
        game.shoot(xin,false);
        if (game.isGoal() && !this.goal) {
            this.solution=x0.clone();
            this.goal=true;
            System.out.println("Goal!!!!!!!in calcu fitness");;
        }

        switch (targetType) {
            case HOLE:
                ball_target_distance=game.getDistance(x,target);
                fit=-Math.log10((game.getMinDistance()+0.01)/(ball_target_distance+0.01))+0.1;
                break;
            case POINT:
                ball_target_distance=game.getDistance(x,target);
                fit=-Math.log10((game.getDistance(game.getStoppoint(),target)+0.01)/(ball_target_distance+0.01))+0.1;
                break;
            case FARSIGHT:
                for (int i =0; i<shortestPath.size();i++) {
                    if (!mapSearcher.isObstacled(game.getStoppoint(), shortestPath.get(i))) {
                        fit = i;
                    }
                }
                break;
            default:
                System.out.println("something wrong in calculateFitness()");
                break;
        }
        return fit;
    }

    /**
     * This method performs selection in the genetic algorithm
     * @param pop The current population.
     * @return The indices of the two selected individuals.
     */
    private int[] selection(IndividualV[] pop){
        double sum=0;
        int[] selected={0,0};
        Random rnd= new Random();
        for (int i = 0; i < pop.length; i++) {
            sum=sum+pop[i].getFitness();
        }
        double s1=rnd.nextDouble()*sum;
        double s2=rnd.nextDouble()*sum;

        for (int i = 0; i < pop.length; i++) {
            s1=s1-pop[i].getFitness();
            if (s1<=0) {
                selected[0]=i;
                break;
            }
        }
        for (int i = 0; i < pop.length; i++) {
            s2=s2-pop[i].getFitness();
            if (s2<=0) {
                selected[1]=i;
                break;
            }
        }
        // check whether duplicated choices. 
        if (selected[0]==selected[1]) {
            selected=selection(pop);
        }
        return selected;
    }

    
    private void exploration(IndividualV indiA, IndividualV indiB, IndividualV[] pop,double[] x){
        Random rnd=new Random();
        for (int i = 0; i < 10; i++) {
            pop[pop.length-1-i]=new IndividualV(new double[]{indiA.getChromosome()[0]+rnd.nextGaussian(),indiA.getChromosome()[1]+rnd.nextGaussian()});
            pop[pop.length-1-i].setFitness(calculateFitness(pop[pop.length-1-i], x));
        }
        // for (int i = 5; i < 10; i++) {
        //     pop[pop.length-1-i]=new IndividualV(new double[]{indiB.getChromosome()[0]+rnd.nextGaussian(),indiB.getChromosome()[1]+rnd.nextGaussian()});
        //     pop[pop.length-1-i].setFitness(calculateFitness(pop[pop.length-1-i], x));
        // }
    }
    private double[] getFarestPoint(double[] point, List<double[]> path){
        double[] outPoint=new double[2];
        for (int i =0; i<shortestPath.size();i++) {
            if (!mapSearcher.isObstacled(point,path.get(i))) {
                outPoint=shortestPath.get(i).clone();
            }
        }
        return outPoint;
    }
    private void followTrend(IndividualV indiA, IndividualV indiB, IndividualV[] pop,double[] x){
        double[] eP;
        double[] sP;
        if (indiA.getFitness()>=indiB.getFitness()) {
            eP=indiA.getChromosome();
            sP=indiB.getChromosome();
        }else{
            eP=indiB.getChromosome();
            sP=indiA.getChromosome();
        }

        double[] newPoint=new double[]{eP[0]+(eP[0]-sP[0])*0.1,eP[1]+(eP[1]-sP[1])*0.1};
        IndividualV temp=new IndividualV(newPoint);
        double newFit=calculateFitness(temp, x);
        System.out.println(newFit);
        if (newFit>Math.max(indiA.getFitness(), indiB.getFitness())) {
            pop[pop.length-1]=temp;
        }
    }

    

    /**
     * Gets the best solution.
     * @return The best solution.
     */
    public double[] getBest(){
        return this.solution;
    }

    private void checkStuck(IndividualV indi){
        if (this.stuckMemory==indi.getFitness()) {
            stuckCount++;
        }else{
            stuckCount=0;
        }
        if (stuckCount>=2) {
            this.stuckCkecker= true;
        }else{
            this.stuckCkecker= false;
        }
        stuckMemory=indi.getFitness();
        
    }
    private enum TargetType{
        HOLE,
        POINT,
        FARSIGHT
    }

    public static void main(String[] args) {
        Random r=new Random();
        for (int i = 0; i < 10; i++) {
            // System.out.println(r.nextDouble()*5);
            System.out.println(Math.sin(15*Math.PI/30));
        }
    }
}