package engine.bot.hillClimbingBot.upd;

import java.util.ArrayList;
import java.util.Random;
import engine.solvers.GolfGameEngine;
import engine.bot.AibotGA.MapSearcher;

public class HillClimbingBotNEW {
    private boolean goal = false;
    private GolfGameEngine game;
    private double[] startBallPosition;
    private double[] holePosition;
    private double[] velocity;
    private String message = "";
    private boolean useAnotherAlgorithm = false;

    private static final int MAX_ITERATIONS = 10;
    private static final double INITIAL_STEP_SIZE = 1.0;
    private static final double TOLERANCE = 0.01;
    private static final int RANDOM_RESTARTS = 3;

    private MapSearcher mapSearcher;
    private ArrayList<double[]> visitedPositions = new ArrayList<>();
    private static final int MAX_VISITED_COUNT = 3;

    public HillClimbingBotNEW(GolfGameEngine game, double[] startBallPosition, double[] holePosition, String mapPath, double radius) {
        this.game = game;
        this.startBallPosition = startBallPosition.clone();
        this.holePosition = holePosition;
        this.velocity = initializeVelocity();
        System.out.println(mapPath);
        this.mapSearcher = new MapSearcher(mapPath, startBallPosition, holePosition, radius);
    }

    public ArrayList<double[]> hillClimbingAlgorithm() {
        ArrayList<double[]> shots = new ArrayList<>();
        this.goal = false;

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

            if (!message.contains("water")) {
                double[] currentShot = {startBallPosition[0], startBallPosition[1], shot[0], shot[1]};
                shots.add(currentShot.clone());

                startBallPosition = getTrajectory(startBallPosition, shot).clone();
                if (calculateDistance(startBallPosition, holePosition) <= TOLERANCE) {
                    this.goal = true;
                }

                // Добавление проверки на застревание
                if (isStuck(startBallPosition)) {
                    System.out.println("Bot is stuck, reinitializing velocity.");
                    this.velocity = initializeVelocity();
                } else {
                    visitedPositions.add(startBallPosition.clone());
                }
            }
        }

        return shots;
    }

    private boolean isStuck(double[] position) {
        int count = 0;
        for (double[] pos : visitedPositions) {
            if (calculateDistance(pos, position) < TOLERANCE) {
                count++;
            }
        }
        return count >= MAX_VISITED_COUNT;
    }

    private double[] hillClimbing() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestVelocity = new double[2];

        for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
            this.velocity = initializeVelocity();
            double currentFitness = evaluateFitness(startBallPosition, velocity);
            double stepSize = INITIAL_STEP_SIZE;
            int iterationsWithoutImprovement = 0;

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                double[][] neighbors = generateNeighbors(velocity, stepSize);
                boolean foundBetter = false;
                for (double[] neighbor : neighbors) {
                    double fitness = evaluateFitness(startBallPosition, neighbor);
                    if (fitness > currentFitness) {
                        currentFitness = fitness;
                        velocity = neighbor;
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

                if (iterationsWithoutImprovement >= 3) {
                    break;
                }

                System.out.println("Restart " + restart + ", Iteration " + i + ": Velocity = [" + velocity[0] + ", " + velocity[1] + "], Fitness = " + currentFitness);
            }
            if (currentFitness > bestFitness) {
                bestFitness = currentFitness;
                bestVelocity = velocity.clone();
            }
        }
        return bestVelocity;
    }

    private double[] hillClimbingFinalShot() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestVelocity = new double[2];

        for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
            this.velocity = initializeVelocity();
            double currentFitness = evaluateFinalShotFitness(startBallPosition, velocity);
            double stepSize = INITIAL_STEP_SIZE;

            for (int i = 0; i < MAX_ITERATIONS; i++) {
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
                if (Math.abs(currentFitness) <= TOLERANCE || bestFitness > -0.2) {
                    this.goal = true;
                    break;
                }

                System.out.println("Final Shot - Restart " + restart + ", Iteration " + i + ": Velocity = [" + velocity[0] + ", " + velocity[1] + "], Fitness = " + currentFitness);
            }
            if (currentFitness > bestFitness) {
                bestFitness = currentFitness;
                bestVelocity = velocity.clone();
            }
        }
        return bestVelocity;
    }

    private double evaluateFinalShotFitness(double[] ballPosition, double[] velocity) {
        double[] finalPosition = getTrajectory(ballPosition, velocity);
        double distanceToHole = calculateDistance(finalPosition, holePosition);
        return -distanceToHole;
    }

    private double[][] generateNeighbors(double[] currentVelocity, double stepSize) {
        double[][] neighbors = new double[8][2];

        int index = 0;
        for (double dx : new double[]{-stepSize, 0, stepSize}) {
            for (double dy : new double[]{-stepSize, 0, stepSize}) {
                if (dx != 0 || dy != 0) {
                    neighbors[index][0] = clamp(currentVelocity[0] + dx, -5, 5);
                    neighbors[index][1] = clamp(currentVelocity[1] + dy, -5, 5);
                    index++;
                }
            }
        }
        return neighbors;
    }

    private double evaluateFitness(double[] ballPosition, double[] velocity) {
        double[] finalPosition = getTrajectory(ballPosition, velocity);
        ArrayList<double[]> shortestPath = mapSearcher.findShortestPath();
        double fitness = mapSearcher.howFarItSee(shortestPath, finalPosition);
        return fitness;
    }

    private double[] getTrajectory(double[] ballPosition, double[] velocity) {
        double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
        game.shoot(inputEngine, false);
        message = game.getMessage();
        return game.getStoppoint();
    }

    private static double calculateDistance(double[] finalPosition, double[] targetPosition) {
        return Math.sqrt(Math.pow(finalPosition[0] - targetPosition[0], 2) + Math.pow(finalPosition[1] - targetPosition[1], 2));
    }

    private static double[] initializeVelocity() {
        Random rand = new Random();
        double[] velocity = new double[2];
        velocity[0] = rand.nextDouble() * 10 - 5;
        velocity[1] = rand.nextDouble() * 10 - 5;
        return velocity;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
