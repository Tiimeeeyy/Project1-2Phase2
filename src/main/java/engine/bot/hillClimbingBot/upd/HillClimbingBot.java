package engine.bot.hillClimbingBot.upd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import engine.solvers.GolfGameEngine;
import engine.bot.AibotGA.MapSearcher;

/**
 * The HillClimbingBot class implements a hill climbing algorithm to find the optimal shots 
 * in a golf game simulation.
 */
public class HillClimbingBot {
    private boolean goal = false;
    private GolfGameEngine game;
    private double[] startBallPosition;
    private double[] holePosition;
    private double[] velocity;
    private boolean useAnotherAlgorithm = false;

    private static final int MAX_ITERATIONS = 5;
    private static final int MAX_ITERATIONS_FINAL = 8;

    private static final double INITIAL_STEP_SIZE = 1.0;
    private static final double TOLERANCE = 0.02;
    private static final int RANDOM_RESTARTS = 4;

    private MapSearcher mapSearcher;
    private ArrayList<double[]> visitedPositions = new ArrayList<>();
    private static final int MAX_VISITED_COUNT = 3;
    private ArrayList<double[]> shortestPath;
    private ArrayList<double[]> turningPoints;
    private double duration;

    /**
     * Constructs a HillClimbingBot instance.
     *
     * @param game              the golf game engine
     * @param startBallPosition the starting position of the ball
     * @param holePosition      the position of the hole
     * @param mapPath           the path to the map file
     * @param radius            the radius used for map searching
     */
    public HillClimbingBot(GolfGameEngine game, double[] startBallPosition, double[] holePosition, String mapPath, double radius) {
        this.game = game;
        this.startBallPosition = startBallPosition.clone();
        this.holePosition = holePosition;
        this.mapSearcher = new MapSearcher(mapPath, startBallPosition, holePosition, radius);
        this.shortestPath = mapSearcher.findShortestPath();
        this.turningPoints = mapSearcher.getTurningPoints(shortestPath);
        this.velocity = initializeVelocity();
        mapSearcher.createImage(shortestPath);
    }

    /**
     * Executes the hill climbing algorithm to find the optimal shots.
     *
     * @return a list of optimal shots
     */
    public ArrayList<double[]> hillClimbingAlgorithm() {
        ArrayList<double[]> shots = new ArrayList<>();
        this.goal = false;
        int numOfShots = 0;
        long startTime = System.currentTimeMillis(); 
        int totalIterations = 0; 
        double bestFitness = Double.NEGATIVE_INFINITY;

        while (!this.goal) {
            double[] shot;

            if (!mapSearcher.isObstacled(startBallPosition, holePosition)) {
                shot = hillClimbingFinalShot();
                this.useAnotherAlgorithm = true;
            } else if (!this.useAnotherAlgorithm) {
                shot = hillClimbing();
            } else {
                shot = hillClimbingFinalShot();
            }

            if (checkNoWater(startBallPosition, shot)) {
                double[] currentShot = {startBallPosition[0], startBallPosition[1], shot[0], shot[1]};
                numOfShots++;
                System.out.println("SHOT: " + numOfShots + " " + shot[0] + ", " + shot[1]);
                startBallPosition = getTrajectory(startBallPosition, shot).clone();
                if (calculateDistance(startBallPosition, holePosition) <= TOLERANCE) {
                    this.goal = true;
                }

                if (isStuck(startBallPosition)) {
                    System.out.println("Bro is stuck, reinitializing velocity.");
                    this.velocity = initializeVelocity();
                } else {
                    visitedPositions.add(startBallPosition.clone());
                    updateTurningPoints();
                }
                shots.add(currentShot.clone());
            } else {
                System.out.println("NO SHOT. WATER!");
            }

            totalIterations++; 
        }

        long endTime = System.currentTimeMillis(); 
        duration =( endTime - startTime)/1000.0;

        System.out.println("Algorithm completed in " + duration  + " seconds");
        System.out.println("Total iterations: " + totalIterations);

        return shots;
    }

    /**
     * Gets the duration of the algorithm execution.
     *
     * @return the duration in seconds
     */
    public double getDuration(){
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
     * Checks if the ball is stuck in a position.
     *
     * @param position the current position of the ball
     * @return true if the ball is stuck, false otherwise
     */
    private boolean isStuck(double[] position) {
        int count = 0;
        for (double[] pos : visitedPositions) {
            if (calculateDistance(pos, position) < TOLERANCE) {
                count++;
            }
        }
        return count >= MAX_VISITED_COUNT;
    }

    /**
     * Executes the main hill climbing algorithm to find the optimal shot velocity.
     *
     * @return the optimal shot velocity
     */
    private double[] hillClimbing() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestVelocity = new double[2];

        ExecutorService executor = Executors.newFixedThreadPool(100);
        Map<double[], Double> neighborResults = new HashMap<>();
        
        for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
            this.velocity = initializeVelocity();
            double currentFitness = evaluateFitness(startBallPosition, velocity);
            double stepSize = INITIAL_STEP_SIZE;
            int iterationsWithoutImprovement = 0;
            
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                double[][] neighbors = generateNeighbors(velocity, stepSize);
                boolean foundBetter = false;
                neighborResults.clear();
                List<Future<?>> futures = new ArrayList<>();

                for (double[] neighbor : neighbors) {
                    futures.add(executor.submit(() -> {
                        double fitness = evaluateFitness(startBallPosition, neighbor);
                        neighborResults.put(neighbor, fitness);
                    }));
                }

                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (Map.Entry<double[], Double> entry : neighborResults.entrySet()) {
                    if (entry.getValue() > currentFitness) {
                        currentFitness = entry.getValue();
                        velocity = entry.getKey();
                        foundBetter = true;
                        iterationsWithoutImprovement = 0;
                    }
                }

                if (!foundBetter) {
                    stepSize /= 2;
                    iterationsWithoutImprovement++;
                } else {
                    stepSize = INITIAL_STEP_SIZE;
                }

                if (iterationsWithoutImprovement >= 4) {
                    break;
                }
            }

            if (currentFitness > bestFitness) {
                bestFitness = currentFitness;
                bestVelocity = velocity.clone();
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                System.err.println("Tasks did not finish in 20 seconds!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return bestVelocity;
    }

    /**
     * Executes the hill climbing algorithm for the final shot.
     *
     * @return the optimal shot velocity for the final shot
     */
    private double[] hillClimbingFinalShot() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestVelocity = new double[2];

        for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
            this.velocity = initializeVelocity();
            double currentFitness = evaluateFinalShotFitness(startBallPosition, velocity);
            double stepSize = INITIAL_STEP_SIZE;

            for (int i = 0; i < MAX_ITERATIONS_FINAL; i++) {
                double[][] neighbors = generateNeighbors(velocity, stepSize);
                boolean foundBetter = false;
                for (double[] neighbor : neighbors) {
                    double fitness = evaluateFinalShotFitness(startBallPosition, neighbor);
                    if (fitness > currentFitness) {
                        currentFitness = fitness;
                        velocity = neighbor;
                        foundBetter = true;
                    }
                }
                if (!foundBetter) {
                    stepSize /= 2;
                } else {
                    stepSize = INITIAL_STEP_SIZE;
                }
                if ((Math.abs(currentFitness) <= TOLERANCE || bestFitness > -0.2)) {
                    this.goal = true;
                    break;
                }
            }
            if (currentFitness > bestFitness) {
                bestFitness = currentFitness;
                bestVelocity = velocity.clone();
            }
        }
        return bestVelocity;
    }

    /**
     * Evaluates the fitness of a final shot.
     *
     * @param ballPosition the current position of the ball
     * @param velocity     the shot velocity
     * @return the fitness value of the final shot
     */
    private double evaluateFinalShotFitness(double[] ballPosition, double[] velocity) {
        double[] finalPosition = getTrajectory(ballPosition, velocity);
        double distanceToHole = calculateDistance(finalPosition, holePosition);

        if (distanceToHole <= 4) {
            double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
            game.shoot(inputEngine, false);
            if (game.getMessage().contains("Goal!!!")) {
                return 0.001;
            }
        }
        return -distanceToHole;
    }

    /**
     * Generates neighboring velocities for the hill climbing algorithm.
     *
     * @param currentVelocity the current shot velocity
     * @param stepSize        the step size for generating neighbors
     * @return a 2D array of neighboring velocities
     */
    private double[][] generateNeighbors(double[] currentVelocity, double stepSize) {
        ArrayList<double[]> neighborsList = new ArrayList<>();

        for (double dx : new double[]{-stepSize, 0, stepSize}) {
            for (double dy : new double[]{-stepSize, 0, stepSize}) {
                if (dx != 0 || dy != 0) {
                    double[] neighbor = {clamp(currentVelocity[0] + dx, -5, 5), clamp(currentVelocity[1] + dy, -5, 5)};
                    neighborsList.add(neighbor);
                }
            }
        }

        return neighborsList.toArray(new double[neighborsList.size()][]);
    }

    /**
     * Evaluates the fitness of a shot.
     *
     * @param ballPosition the current position of the ball
     * @param velocity     the shot velocity
     * @return the fitness value of the shot
     */
    private double evaluateFitness(double[] ballPosition, double[] velocity) {
        double[] finalPosition = getTrajectory(ballPosition, velocity);
        int fit = (int) mapSearcher.howFarItSee(shortestPath, finalPosition);

        return fit - Math.log10((game.getDistance(game.getStoppoint(), shortestPath.get(fit)) + 0.01) / (calculateDistance(startBallPosition, shortestPath.get(fit)) + 0.01));
    }

    /**
     * Checks if a shot avoids water hazards.
     *
     * @param ballPosition the current position of the ball
     * @param velocity     the shot velocity
     * @return true if the shot avoids water, false otherwise
     */
    private boolean checkNoWater(double[] ballPosition, double[] velocity) {
        double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
        game.shoot(inputEngine, false);
        if (game.getMessage().contains("Water!")) {
            return false;
        }
        return true;
    }

    /**
     * Calculates the trajectory of a shot.
     *
     * @param ballPosition the current position of the ball
     * @param velocity     the shot velocity
     * @return the final position of the ball after the shot
     */
    private double[] getTrajectory(double[] ballPosition, double[] velocity) {
        double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
        game.shoot(inputEngine, false);
        return game.getStoppoint();
    }

    /**
     * Calculates the distance between two positions.
     *
     * @param finalPosition  the final position
     * @param targetPosition the target position
     * @return the distance between the two positions
     */
    private static double calculateDistance(double[] finalPosition, double[] targetPosition) {
        return Math.sqrt(Math.pow(finalPosition[0] - targetPosition[0], 2) + Math.pow(finalPosition[1] - targetPosition[1], 2));
    }

    /**
     * Initializes a random shot velocity.
     *
     * @return the initialized shot velocity
     */
    private double[] initializeVelocity() {
        Random rand = new Random();
        double[] velocity = new double[2];

        if (!turningPoints.isEmpty()) {
            double[] nearestTurningPoint = turningPoints.get(0);
            for (double[] point : turningPoints) {
                if (Math.abs(point[1] - startBallPosition[1]) < Math.abs(nearestTurningPoint[1] - startBallPosition[1])) {
                    nearestTurningPoint = point;
                }
            }

            if (Math.abs(nearestTurningPoint[1] - startBallPosition[1]) < 2) {
                if (nearestTurningPoint[0] > startBallPosition[0]) {
                    if (rand.nextBoolean()) {
                        velocity[0] = rand.nextDouble() * 5;
                        velocity[1] = rand.nextDouble() * 5;
                    } else {
                        velocity[0] = rand.nextDouble() * 5;
                        velocity[1] = -(rand.nextDouble() * 5);
                    }
                } else {
                    if (rand.nextBoolean()) {
                        velocity[0] = -(rand.nextDouble() * 5);
                        velocity[1] = rand.nextDouble() * 5;
                    } else {
                        velocity[0] = -(rand.nextDouble() * 5);
                        velocity[1] = -(rand.nextDouble() * 5);
                    }
                }
            } else {
                if (nearestTurningPoint[1] > startBallPosition[1]) {
                    if (rand.nextBoolean()) {
                        velocity[0] = rand.nextDouble() * 5;
                        velocity[1] = rand.nextDouble() * 5;
                    } else {
                        velocity[0] = -(rand.nextDouble() * 5);
                        velocity[1] = rand.nextDouble() * 5;
                    }
                } else {
                    if (rand.nextBoolean()) {
                        velocity[0] = rand.nextDouble() * 5;
                        velocity[1] = -(rand.nextDouble() * 5);
                    } else {
                        velocity[0] = -(rand.nextDouble() * 5);
                        velocity[1] = -(rand.nextDouble() * 5);
                    }
                }
            }
        } else {
            int quadrantIndex = rand.nextInt(4);
            switch (quadrantIndex) {
                case 0:
                    velocity[0] = -(rand.nextDouble() * 5);
                    velocity[1] = -(rand.nextDouble() * 5);
                    break;
                case 1:
                    velocity[0] = rand.nextDouble() * 5;
                    velocity[1] = -(rand.nextDouble() * 5);
                    break;
                case 2:
                    velocity[0] = rand.nextDouble() * 5;
                    velocity[1] = rand.nextDouble() * 5;
                    break;
                case 3:
                    velocity[0] = -(rand.nextDouble() * 5);
                    velocity[1] = rand.nextDouble() * 5;
                    break;
            }
        }

        return velocity;
    }

    /**
     * Updates the list of turning points by removing those close to the current position.
     */
    private void updateTurningPoints() {
        turningPoints.removeIf(point -> calculateDistance(point, startBallPosition) < TOLERANCE);
    }

    /**
     * Clamps a value within a specified range.
     *
     * @param value the value to clamp
     * @param min   the minimum value
     * @param max   the maximum value
     * @return the clamped value
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
