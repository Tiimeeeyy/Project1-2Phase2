package engine.bot.AibotGA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


import engine.solvers.BallStatus;
import engine.solvers.GolfGameEngine;

public class AiBotGA {
    private int popSize=50;
    private char[] vocab={'0','1'};
    private double mutationRate=0.10;
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
    private double duration;

    public AiBotGA(GolfGameEngine game){
        this.game=game;
    }

    public ArrayList<double[]> golfBot(double x[]){
        long startTime = System.currentTimeMillis();
        double[] x0=x.clone();
        mapSearcher=new MapSearcher(game.getMapPath(), x0, game.getHole(), game.getHoleRadius());
        shortestPath=mapSearcher.findShortestPath();
        int shotNum=0;
        ArrayList<double[]> allSteps=new ArrayList<>();
        

        stuckCkecker=false;
        stuckMemory=0;
        stuckCount=0;

        
        while (mapSearcher.isObstacled(x0, game.getHole()) && shotNum<=15) {
            oneShot(x0,TargetType.FARSIGHT,null);

            game.shoot(solution.clone(), false);
            if (game.getStatus().equals(BallStatus.Normal) || game.getStatus().equals(BallStatus.Goal)) {
                allSteps.add(solution.clone());
                stuckCount=0;
            } else {
                stuckCount++;
            }
            x0=game.getStoppoint();
            if (stuckCount>2) {
                double[] target=new double[2];
                target=getFarestPoint(x0, shortestPath);
                oneShot(x0,TargetType.POINT,target);
                
                allSteps.add(solution.clone());
                game.shoot(solution.clone(), false);
                x0=game.getStoppoint();
            }
            System.out.println(Arrays.toString(solution));
            shotNum++;
        }
        if (!goal) {
            oneShot(x0,TargetType.HOLE,null);
            allSteps.add(solution);
        }
        long endTime = System.currentTimeMillis();
        this.duration = (endTime - startTime)/1000.0;
        System.out.println("Algorithm completed in " + (endTime - startTime)/1000.0 + " seconds");
        return allSteps;
    }

    public void oneShot(double[] x, TargetType targetType, double[] target){
        Individual[] population=new Individual[popSize];
        int generations=300;
        switch (targetType) {
            case HOLE:
                this.target=game.getHole();
                this.targetType=TargetType.HOLE;
                generations=500;
                break;
            case POINT:
                this.target=target;
                this.targetType=TargetType.POINT;
                generations=300;
                break;
            case FARSIGHT:
                this.targetType=TargetType.FARSIGHT;
                this.target=getFarestPoint(x, shortestPath);
                generations=200;
                break;
            default:
                System.out.println("something wrong in oneShot()");
                break;
        }

        //clear previuos
        this.solution=new double[4];
        this.goal=false;

        double[] x0=x.clone();
        initialPopulation(population, x0);
        for (int i = 0; i < generations; i++) {
            int[] slcIndex=selection(population);
            crossover(population[slcIndex[0]], population[slcIndex[1]], population);
            population[popSize-1].setFitness(calculateFitness(population[popSize-1], x.clone()));
            population[popSize-2].setFitness(calculateFitness(population[popSize-2], x.clone()));
            if (this.goal) {
                break;
            }
            HeapSort.sort(population);
        }
        if (!this.goal) {
            double[] best=x0;
            best[2]=population[0].genoToPhenotype()[0];
            best[3]=population[0].genoToPhenotype()[1];
            this.solution=best.clone();
        }

        System.out.println(population[0].getFitness());
    }

    private void initialPopulation(Individual[] pop, double[] x){
        Random rand=new Random();
        char[][] indi=new char[2][10];
        double cos=(target[0]-x[0])/game.getDistance(x,target);
        double sin=(target[1]-x[1])/game.getDistance(x,target);

        double[] farest=getFarestPoint(x, shortestPath);
        double powerMean=game.getDistance(x, farest)/5;
        
        int n=0;
        for (int k = -2; k<3; k++) {
            for (int i = 0; i < 5; i++) {
                double power=Math.min(rand.nextGaussian()+powerMean, 5);
                char[] vxChrom=Integer.toBinaryString((int)(power*(cos*Math.cos(0.17*k)-sin*Math.sin(0.17*k))*100+500)).toCharArray();
                char[] vyChrom=Integer.toBinaryString((int)(power*(sin*Math.cos(0.17*k)+cos*Math.sin(0.17*k))*100+500)).toCharArray();

                indi=covertToChromosome(vxChrom, vyChrom);
                
                pop[n]=new Individual(indi);
                pop[n].setFitness(calculateFitness(pop[k+2], x.clone()));
                n++;
            }
            
        }

        for (int i = 25; i < popSize; i++) {
            for (int j = 0; j < 2; j++) {
                for(int k=0;k<10;k++){
                    indi[j][k]=vocab[rand.nextInt(2)];
                }
            }
            pop[i]=new Individual(indi);
            pop[i].setFitness(calculateFitness(pop[i], x.clone()));
        }
        HeapSort.sort(pop);
    }

    private double calculateFitness(Individual indi, double[] x){
        double fit = 1;
        double ball_target_distance=game.getDistance(x,target);
        double[] xin=new double[]{x[0],x[1],indi.genoToPhenotype()[0],indi.genoToPhenotype()[1]};
        double[] x0=xin.clone();
        game.shoot(xin,false);
        if (game.isGoal() && !this.goal) {
            this.solution=x0.clone();
            this.goal=true;
        }
        switch (targetType) {
            case HOLE:
                fit=-Math.log10((game.getMinDistance()+0.01)/(ball_target_distance+0.01))+0.1;
                break;
            case POINT:
                fit=-Math.log10((game.getDistance(game.getStoppoint(),target)+0.01)/(ball_target_distance+0.01))+0.1;
                break;
            case FARSIGHT:
                for (int i =0; i<shortestPath.size();i++) {
                    if (!mapSearcher.isObstacled(game.getStoppoint(), shortestPath.get(i))) {
                        fit = i-Math.log10((game.getDistance(game.getStoppoint(),target)+0.01)/(ball_target_distance+0.01));
                    }
                }
                break;
            default:
                System.out.println("something wrong in calculateFitness()");
                break;
        }

        
        return fit;
    }

    private int[] selection(Individual[] pop){
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
        if (selected[0]==selected[1]) {
            selected=selection(pop);
        }
        return selected;
    }

    private void crossover(Individual slc1, Individual slc2, Individual[] pop){
        Random rnd=new Random();
        int pivot=rnd.nextInt(7)+1;
        Individual child1=slc1.clone();
        Individual child2=slc2.clone();
        for (int i = pivot; i < 10; i++) {
            char temp=child1.getChromosome()[0][i];
            child1.getChromosome()[0][i]=child2.getChromosome()[0][i];
            child2.getChromosome()[0][i]=temp;

            temp=child1.getChromosome()[1][i];
            child1.getChromosome()[1][i]=child2.getChromosome()[1][i];
            child2.getChromosome()[1][i]=temp;
        }
        mutation(child1);
        mutation(child2);
        pop[popSize-1]=child1.clone();
        pop[popSize-2]=child2.clone();
    }

    private void mutation(Individual indi){
        Random rnd=new Random();
        for (int i = 0; i < 10; i++) {
            int r=rnd.nextInt((int) (1/mutationRate)) ;
            if (r==0) {
                if (indi.getChromosome()[0][i]=='0') {
                    indi.getChromosome()[0][i]='1';
                }
                if (indi.getChromosome()[0][i]=='1') {
                    indi.getChromosome()[0][i]='0';
                }
            }
            r=rnd.nextInt((int) (1/mutationRate)) ;
            if (r==0) {
                if (indi.getChromosome()[1][i]=='0') {
                    indi.getChromosome()[1][i]='1';
                }
                if (indi.getChromosome()[1][i]=='1') {
                    indi.getChromosome()[1][i]='0';
                }
            }
        }
    }
    private char[][] covertToChromosome(char[] x, char[]y){
        char[][] indi=new char[2][10];
        for (int i = 0; i <10; i++) {
            int j=x.length+i-10;
            if(j>=0){
                indi[0][i]=x[j];
            }else{
                indi[0][i]='0';
            }
            j=y.length+i-10;
            if(j>=0){
                indi[1][i]=y[j];
            }else{
                indi[1][i]='0';
            }  
        }
        return indi;
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
    public double[] getBest(){
        return this.solution;
    }
    public double getDuration(){
        return this.duration;
    }

    public boolean isGoal(){
        return this.goal;
    }
    

    private enum TargetType{
        HOLE,
        POINT,
        FARSIGHT
    }
}


