package engine.bot.AibotGA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import engine.solvers.BallStatus;
import engine.solvers.GolfGameEngine;

public class AiBotMultiShots {
    private int popSize=50;
    private char[] vocab={'0','1'};
    private double mutationRate=0.10;
    private double[] solution=new double[4];
    private boolean goal=false;
    private double[] des;
    private ArrayList<double[]> shortestPath;
    private MapSearcher mapSearcher;

    private GolfGameEngine game;

    /**
     * Class constructor
     * @param game The engine to simulate the game.
     */
    public AiBotMultiShots(GolfGameEngine game){
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
        while (mapSearcher.isObstacled(x0, game.getHole()) && shotNum<=10) {
            oneShot(x0);

            
            game.shoot(solution.clone(), false);
            if (game.getStatus().equals(BallStatus.Normal) || game.getStatus().equals(BallStatus.Goal)) {
                allSteps.add(solution.clone());
            }

            // check if it stucks at the last 3 steps
            if (allSteps.size()>2) {
                if (isSamePosition(allSteps.get(allSteps.size()-1), allSteps.get(allSteps.size()-2)) && 
                    isSamePosition(allSteps.get(allSteps.size()-2), allSteps.get(allSteps.size()-3))) {
                }
            }
            
            
            x0=game.getStoppoint();
            System.out.println(Arrays.toString(solution));
            shotNum++;
        }
        if (!goal) {
            AiBotGA ai=new AiBotGA(game);
            ai.golfBot(x0);
            allSteps.add(ai.getBest());
        }
        
        return allSteps;
    }

    public void oneShot(double[] x){
        Individual[] population=new Individual[popSize];
        double[] x0=x.clone();

        // initiate population
        initialPopulation(population, x0);
 
        // Run the algorithm
        for (int i = 0; i < 300; i++) {
            int[] slcIndex=selection(population);
            crossover(population[slcIndex[0]], population[slcIndex[1]], population);
            population[popSize-1].setFitness(calculateFitness(population[popSize-1], x.clone()));
            population[popSize-2].setFitness(calculateFitness(population[popSize-2], x.clone()));
            if (this.goal) {
                break;
            }
            HeapSort.sort(population);
            // System.out.println(population[0].getFitness()+"  "+i);
            
        }
        // If the goal is not reached, set the solution to the best solution found
        if (!this.goal) {
            double[] best=x0;
            best[2]=population[0].genoToPhenotype()[0];
            best[3]=population[0].genoToPhenotype()[1];
            this.solution=best.clone();
        }
        
    }

    /**
     * THis method initialises the population for the genetic algorithm
     * @param pop The population size to be initialised.
     * @param x The initial state / position of the ball.
     */
    void initialPopulation(Individual[] pop, double[] x){
       
        Random rand=new Random();
        char[][] indi=new char[2][10];

        // set 1 try of direct shoot 
        double[] hole=game.getHole().clone();
        double cos=(hole[0]-x[0])/game.getHoleBallDistance(x);
        double sin=(hole[1]-x[1])/game.getHoleBallDistance(x);

        for (int k = -2; k<3; k++) {
            char[] vxChrom=Integer.toBinaryString((int)(5*(cos*Math.cos(0.17*k)-sin*Math.sin(0.17*k))*100+500)).toCharArray();
            char[] vyChrom=Integer.toBinaryString((int)(5*(sin*Math.cos(0.17*k)+cos*Math.sin(0.17*k))*100+500)).toCharArray();
            for (int i = 0; i <10; i++) {
                int j=vxChrom.length+i-10;
                if(j>=0){
                    indi[0][i]=vxChrom[j];
                }else{
                    indi[0][i]='0';
                }
                j=vyChrom.length+i-10;
                if(j>=0){
                    indi[1][i]=vyChrom[j];
                }else{
                    indi[1][i]='0';
                }  
            }
            pop[k+2]=new Individual(indi);
            pop[k+2].setFitness(calculateFitness(pop[0], x.clone()));
            
        }
        

        // Random the rest in population
        for (int i = 5; i < popSize; i++) {
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

   

    double calculateFitness(Individual indi, double[] x){
        
        double fit = 1;
        double[] xin=new double[]{x[0],x[1],indi.genoToPhenotype()[0],indi.genoToPhenotype()[1]};
        double[] x0=xin.clone();
        game.shoot(xin,false);
        if (game.isGoal() && !this.goal) {
            this.solution=x0.clone();
            this.goal=true;
            System.out.println("Goal!!!!!!!in calcu fitness");;
        }

        for (int i =0; i<shortestPath.size();i++) {
            if (!mapSearcher.isObstacled(game.getStoppoint(), shortestPath.get(i))) {
                fit = i*5+5;
            }
        }
        return fit;
    }

    /**
     * This method performs selection in the genetic algorithm
     * @param pop The current population.
     * @return The indices of the two selected individuals.
     */
    int[] selection(Individual[] pop){
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

    

    /**
     * This method performs crossover in the genetic algorithm.
     * @param slc1 The first selected individual.
     * @param slc2 The second selected individual.
     * @param pop The current population.
     */
    void crossover(Individual slc1, Individual slc2, Individual[] pop){
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

    /**
     * This method performs mutations in the genetic algorithm.
     * @param indi The individual to be mutated.
     */
    void mutation(Individual indi){
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

    /**
     * Gets the best solution.
     * @return The best solution.
     */
    public double[] getBest(){
        return this.solution;
    }

    private boolean isSamePosition(double[]a, double[] b){
        return a[0]==b[0] && a[1]==b[1];
    }
}