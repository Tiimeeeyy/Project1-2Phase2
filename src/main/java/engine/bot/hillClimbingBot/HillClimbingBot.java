package engine.bot.hillClimbingBot;

import java.util.ArrayList;
import java.util.Random;
import engine.solvers.GolfGameEngine;

/**
 * HillClimbingBot implements a simple hill climbing algorithm to find
 * the best velocity to shoot a golf ball to a hole in a golf game simulation.
 */
public class HillClimbingBot {
    private boolean goal = false;
    private GolfGameEngine game;
    private double[] startBallPosition;
    private double[] holePosition;
    private double[] velocity;

    private static final int MAX_ITERATIONS = 10;
    private static final double INITIAL_STEP_SIZE = 1.0;
    private static final double TOLERANCE = 0.01;
    private static final int RANDOM_RESTARTS = 3;

    /**
     * Constructs a HillClimbingBot with the given game engine, start ball position, and hole position.
     *
     * @param game the GolfGameEngine instance
     * @param startBallPosition the initial position of the golf ball
     * @param holePosition the position of the hole
     */
    public HillClimbingBot(GolfGameEngine game, double[] startBallPosition, double[] holePosition) {
        this.game = game;
        this.startBallPosition = startBallPosition.clone();
        this.holePosition = holePosition;
        this.velocity = initializeVelocity();
    }

    /**
     * Executes the hill climbing algorithm to find the best velocity.
     *
     * @return the best velocity found
     */
    public ArrayList<double[]> hillClimbingAlgorithm() {
        ArrayList<double[]> shots = new ArrayList<>();
        this.goal = false;

        while (!this.goal) {
            double[] shot = hillClimbing();
            double[] currentShot = {startBallPosition[0], startBallPosition[1], shot[0], shot[1]};
            shots.add(currentShot.clone());
            startBallPosition = getTrajectory(startBallPosition, shot).clone();
            System.out.println(shots.get(0)[0]);
        }
        return shots;
    }

    private double[] hillClimbing() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestVelocity = new double[2];

        for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
            this.velocity = initializeVelocity();
            double currentFitness = evaluateFitness(startBallPosition, velocity);
            double stepSize = INITIAL_STEP_SIZE;

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                double[][] neighbors = generateNeighbors(velocity, stepSize);
                boolean foundBetter = false;
                for (double[] neighbor : neighbors) {
                    double fitness = evaluateFitness(startBallPosition, neighbor);
                    if (fitness > currentFitness) {
                        currentFitness = fitness;
                        velocity = neighbor;
                        foundBetter = true;
                    }
                }
                if (!foundBetter) {
                    stepSize /= 2;  // make a step size smaller (adaptive)
                } else {
                    stepSize = INITIAL_STEP_SIZE;
                }
                if (Math.abs(currentFitness) <= TOLERANCE || bestFitness > -0.2) {
                    this.goal = true;
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

    /**
     * Generates neighbor velocities around the current velocity.
     *
     * @param currentVelocity the current velocity
     * @param stepSize the step size to generate neighbors
     * @return an array of neighboring velocities
     */
    private double[][] generateNeighbors(double[] currentVelocity, double stepSize) {
        double[][] neighbors = new double[8][2];
        int index = 0;
        for (double dx : new double[] {-stepSize, 0, stepSize}) {
            for (double dy : new double[] {-stepSize, 0, stepSize}) {
                if (dx != 0 || dy != 0) {
                    neighbors[index][0] = clamp(currentVelocity[0] + dx, -5, 5);
                    neighbors[index][1] = clamp(currentVelocity[1] + dy, -5, 5);
                    index++;
                }
            }
        }
        return neighbors;
    }

    /**
     * Evaluates the fitness of a given velocity.
     *
     * @param ballPosition the position of the ball
     * @param velocity the velocity to evaluate
     * @return the fitness value, which is the negative distance to the hole
     */
    private double evaluateFitness(double[] ballPosition, double[] velocity) {
        double[] finalPosition = getTrajectory(ballPosition, velocity);
        double distanceToTarget = calculateDistance(finalPosition, holePosition);
        return -distanceToTarget;
    }

    private double[] getTrajectory(double[] ballPosition, double[] velocity) {
        double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
        game.shoot(inputEngine, false);
        return game.getStoppoint();
    }

    /**
     * Calculates the Euclidean distance between two points.
     *
     * @param finalPosition the final position of the ball
     * @param targetPosition the target position (hole position)
     * @return the Euclidean distance between the points
     */
    private static double calculateDistance(double[] finalPosition, double[] targetPosition) {
        return Math.sqrt(Math.pow(finalPosition[0] - targetPosition[0], 2) + Math.pow(finalPosition[1] - targetPosition[1], 2));
    }

    /**
     * Initializes a random velocity within the range [-5, 5].
     *
     * @return a randomly initialized velocity
     */
    private static double[] initializeVelocity() {
        Random rand = new Random();
        double[] velocity = new double[2];
        velocity[0] = rand.nextDouble() * 10 - 5; 
        velocity[1] = rand.nextDouble() * 10 - 5; 
        return velocity;
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value the value to clamp
     * @param min the minimum value
     * @param max the maximum value
     * @return the clamped value
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
