package engine.bot.AibotGA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import engine.solvers.BallStatus;
import engine.solvers.GolfGameEngine;

/**
 * The AiBotGA class implements a genetic algorithm-based AI bot for playing a golf game.
 */
public class AiBotGA {
    private int popSize = 100;
    private char[] vocab = {'0', '1'};
    private double mutationRate = 0.10;
    private double[] solution = new double[4];
    private boolean goal = false;
    private ArrayList<double[]> shortestPath;
    private MapSearcher mapSearcher;

    private double[] target;
    private TargetType targetType;

    private boolean stuckChecker = false;
    private int stuckCount = 0;
    private double stuckMemory = 0;

    private GolfGameEngine game;
    private double duration;

    /**
     * Constructs an AiBotGA instance.
     *
     * @param game the golf game engine
     */
    public AiBotGA(GolfGameEngine game) {
        this.game = game;
    }

    /**
     * Executes the genetic algorithm to find the best sequence of shots to reach the hole.
     *
     * @param x the initial position and velocity of the ball
     * @return a list of optimal shots
     */
    public ArrayList<double[]> golfBot(double[] x) {
        long startTime = System.currentTimeMillis();
        double[] x0 = x.clone();
        mapSearcher = new MapSearcher(game.getMapPath(), x0, game.getHole(), game.getHoleRadius());
        shortestPath = mapSearcher.findShortestPath();
        int shotNum = 0;
        ArrayList<double[]> allSteps = new ArrayList<>();

        stuckChecker = false;
        stuckMemory = 0;
        stuckCount = 0;

        while (mapSearcher.isObstacled(x0, game.getHole()) && shotNum <= 15) {
            oneShot(x0, TargetType.FARSIGHT, null);

            game.shoot(solution.clone(), false);
            if (game.getStatus().equals(BallStatus.Normal) || game.getStatus().equals(BallStatus.Goal)) {
                allSteps.add(solution.clone());
                stuckCount = 0;
            } else {
                stuckCount++;
            }
            x0 = game.getStoppoint();
            if (stuckCount > 2) {
                double[] target = getFarestPoint(x0, shortestPath);
                oneShot(x0, TargetType.POINT, target);

                allSteps.add(solution.clone());
                game.shoot(solution.clone(), false);
                x0 = game.getStoppoint();
            }
            System.out.println(Arrays.toString(solution));
            shotNum++;
        }
        if (!goal) {
            oneShot(x0, TargetType.HOLE, null);
            allSteps.add(solution);
        }
        long endTime = System.currentTimeMillis();
        this.duration = (endTime - startTime) / 1000.0;
        System.out.println("Algorithm completed in " + (endTime - startTime) / 1000.0 + " seconds");
        return allSteps;
    }

    /**
     * Executes a single shot using the genetic algorithm.
     *
     * @param x         the current position and velocity of the ball
     * @param targetType the type of target (HOLE, POINT, FARSIGHT)
     * @param target    the target position if the target type is POINT
     */
    public void oneShot(double[] x, TargetType targetType, double[] target) {
        Individual[] population = new Individual[popSize];
        int generations = 300;
        switch (targetType) {
            case HOLE:
                this.target = game.getHole();
                this.targetType = TargetType.HOLE;
                generations = 500;
                break;
            case POINT:
                this.target = target;
                this.targetType = TargetType.POINT;
                generations = 300;
                break;
            case FARSIGHT:
                this.targetType = TargetType.FARSIGHT;
                this.target = getFarestPoint(x, shortestPath);
                generations = 200;
                break;
            default:
                System.out.println("Something wrong in oneShot()");
                break;
        }

        // Clear previous solution
        this.solution = new double[4];
        this.goal = false;

        double[] x0 = x.clone();
        initialPopulation(population, x0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < generations; i++) {
            int[] slcIndex = selection(population);
            List<Future<?>> futures = new ArrayList<>();
            crossover(population[slcIndex[0]], population[slcIndex[1]], population);
            futures.add(executor.submit(() -> {
                population[popSize - 1].setFitness(calculateFitness(population[popSize - 1], x.clone()));
            }));
            futures.add(executor.submit(() -> {
                population[popSize - 2].setFitness(calculateFitness(population[popSize - 2], x.clone()));
            }));

            for (Future<?> future : futures) {
                try {
                    future.get(); // This will block until the task completes
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (this.goal) {
                break;
            }
            HeapSort.sort(population);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                System.err.println("Tasks did not finish in 20 seconds!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!this.goal) {
            double[] best = x0;
            best[2] = population[0].genoToPhenotype()[0];
            best[3] = population[0].genoToPhenotype()[1];
            this.solution = best.clone();
        }
        System.out.println(population[0].getFitness());
    }

    /**
     * Initializes the population for the genetic algorithm.
     *
     * @param pop the population array
     * @param x   the current position and velocity of the ball
     */
    private void initialPopulation(Individual[] pop, double[] x) {
        Random rand = new Random();
        double cos = (target[0] - x[0]) / game.getDistance(x, target);
        double sin = (target[1] - x[1]) / game.getDistance(x, target);

        double[] farest = getFarestPoint(x, shortestPath);
        double powerMean = game.getDistance(x, farest) / 5;

        ExecutorService executor = Executors.newFixedThreadPool(10);

        int n = 0;
        for (int k = -2; k < 3; k++) {
            int kf = k;
            for (int i = 0; i < 5; i++) {
                final int index = n;
                executor.submit(() -> {
                    double power = Math.min(rand.nextGaussian() + powerMean, 5);
                    char[] vxChrom = Integer.toBinaryString((int) (power * (cos * Math.cos(0.17 * kf) - sin * Math.sin(0.17 * kf)) * 100 + 500)).toCharArray();
                    char[] vyChrom = Integer.toBinaryString((int) (power * (sin * Math.cos(0.17 * kf) + cos * Math.sin(0.17 * kf)) * 100 + 500)).toCharArray();

                    char[][] indi = covertToChromosome(vxChrom, vyChrom);

                    pop[index] = new Individual(indi);
                    pop[index].setFitness(calculateFitness(pop[index], x.clone()));
                });

                n++;
            }
        }

        for (int i = 25; i < popSize; i++) {
            int fi = i;
            executor.submit(() -> {
                char[][] indi = new char[2][10];
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 10; k++) {
                        indi[j][k] = vocab[rand.nextInt(2)];
                    }
                }
                pop[fi] = new Individual(indi);
                pop[fi].setFitness(calculateFitness(pop[fi], x.clone()));
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                System.err.println("Tasks did not finish in 20 seconds!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HeapSort.sort(pop);
    }

    /**
     * Calculates the fitness of an individual.
     *
     * @param indi the individual
     * @param x    the current position and velocity of the ball
     * @return the fitness value
     */
    private double calculateFitness(Individual indi, double[] x) {
        double fit = 1;
        double ballTargetDistance = game.getDistance(x, target);
        double[] xin = new double[]{x[0], x[1], indi.genoToPhenotype()[0], indi.genoToPhenotype()[1]};
        double[] x0 = xin.clone();
        game.shoot(xin, false);
        if (game.isGoal() && !this.goal) {
            this.solution = x0.clone();
            this.goal = true;
        }
        switch (targetType) {
            case HOLE:
                fit = -Math.log10((game.getMinDistance() + 0.01) / (ballTargetDistance + 0.01)) + 0.1;
                break;
            case POINT:
                fit = -Math.log10((game.getDistance(game.getStoppoint(), target) + 0.01) / (ballTargetDistance + 0.01)) + 0.1;
                break;
            case FARSIGHT:
                for (int i = 0; i < shortestPath.size(); i++) {
                    if (!mapSearcher.isObstacled(game.getStoppoint(), shortestPath.get(i))) {
                        fit = i - Math.log10((game.getDistance(game.getStoppoint(), target) + 0.01) / (ballTargetDistance + 0.01));
                    }
                }
                break;
            default:
                System.out.println("Something wrong in calculateFitness()");
                break;
        }
        return fit;
    }

    /**
     * Selects individuals for crossover.
     *
     * @param pop the population array
     * @return an array of selected individual indices
     */
    private int[] selection(Individual[] pop) {
        double sum = 0;
        int[] selected = {0, 0};
        Random rnd = new Random();
        for (Individual individual : pop) {
            sum = sum + individual.getFitness();
        }
        double s1 = rnd.nextDouble() * sum;
        double s2 = rnd.nextDouble() * sum;
        for (int i = 0; i < pop.length; i++) {
            s1 = s1 - pop[i].getFitness();
            if (s1 <= 0) {
                selected[0] = i;
                break;
            }
        }
        for (int i = 0; i < pop.length; i++) {
            s2 = s2 - pop[i].getFitness();
            if (s2 <= 0) {
                selected[1] = i;
                break;
            }
        }
        if (selected[0] == selected[1]) {
            selected = selection(pop);
        }
        return selected;
    }

    /**
     * Performs crossover between two selected individuals.
     *
     * @param slc1 the first selected individual
     * @param slc2 the second selected individual
     * @param pop  the population array
     */
    private void crossover(Individual slc1, Individual slc2, Individual[] pop) {
        Random rnd = new Random();
        int pivot = rnd.nextInt(7) + 1;
        Individual child1 = slc1.clone();
        Individual child2 = slc2.clone();
        for (int i = pivot; i < 10; i++) {
            char temp = child1.getChromosome()[0][i];
            child1.getChromosome()[0][i] = child2.getChromosome()[0][i];
            child2.getChromosome()[0][i] = temp;

            temp = child1.getChromosome()[1][i];
            child1.getChromosome()[1][i] = child2.getChromosome()[1][i];
            child2.getChromosome()[1][i] = temp;
        }
        mutation(child1);
        mutation(child2);
        pop[popSize - 1] = child1.clone();
        pop[popSize - 2] = child2.clone();
    }

    /**
     * Mutates an individual.
     *
     * @param indi the individual to mutate
     */
    private void mutation(Individual indi) {
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            int r = rnd.nextInt((int) (1 / mutationRate));
            if (r == 0) {
                if (indi.getChromosome()[0][i] == '0') {
                    indi.getChromosome()[0][i] = '1';
                } else if (indi.getChromosome()[0][i] == '1') {
                    indi.getChromosome()[0][i] = '0';
                }
            }
            r = rnd.nextInt((int) (1 / mutationRate));
            if (r == 0) {
                if (indi.getChromosome()[1][i] == '0') {
                    indi.getChromosome()[1][i] = '1';
                } else if (indi.getChromosome()[1][i] == '1') {
                    indi.getChromosome()[1][i] = '0';
                }
            }
        }
    }

    /**
     * Converts velocity components to a binary chromosome representation.
     *
     * @param x the x-component of the velocity
     * @param y the y-component of the velocity
     * @return a 2D array representing the chromosome
     */
    private char[][] covertToChromosome(char[] x, char[] y) {
        char[][] indi = new char[2][10];
        for (int i = 0; i < 10; i++) {
            int j = x.length + i - 10;
            if (j >= 0) {
                indi[0][i] = x[j];
            } else {
                indi[0][i] = '0';
            }
            j = y.length + i - 10;
            if (j >= 0) {
                indi[1][i] = y[j];
            } else {
                indi[1][i] = '0';
            }
        }
        return indi;
    }

    /**
     * Finds the farthest point from the current position along the shortest path.
     *
     * @param point the current position
     * @param path  the shortest path
     * @return the farthest point on the path
     */
    private double[] getFarestPoint(double[] point, List<double[]> path) {
        double[] outPoint = new double[2];
        for (int i = 0; i < shortestPath.size(); i++) {
            if (!mapSearcher.isObstacled(point, path.get(i))) {
                outPoint = shortestPath.get(i).clone();
            }
        }
        return outPoint;
    }

    /**
     * Gets the best solution found by the genetic algorithm.
     *
     * @return the best solution
     */
    public double[] getBest() {
        return this.solution;
    }

    /**
     * Gets the duration of the algorithm execution.
     *
     * @return the duration in seconds
     */
    public double getDuration() {
        return this.duration;
    }

    /**
     * Checks if the goal has been reached.
     *
     * @return true if the goal is reached, false otherwise
     */
    public boolean isGoal() {
        return this.goal;
    }

    /**
     * Enumeration for different target types.
     */
    private enum TargetType {
        HOLE,
        POINT,
        FARSIGHT
    }
}
