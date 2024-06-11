package engine.bot.hillClimbingBot;

import java.util.Random;
import engine.solvers.GolfGameEngine;

public class HillClimbingBot {
    private boolean goal = false;
    private GolfGameEngine game;
    private double[] startBallPosition;
    private double[] holePosition;
    private double[] velocity;

    private static final int MAX_ITERATIONS = 100;
    private static final double STEP_SIZE = 0.1;
    private static final double TOLERANCE = 0.01;

    public HillClimbingBot(GolfGameEngine game, double[] startBallPosition, double[] holePosition) {
        this.game = game;
        this.startBallPosition = startBallPosition;
        this.holePosition = holePosition;
        this.velocity = initializeVelocity();
    }

    public double[] hillClimbingAlgorithm() {
        double bestFitness = evaluateFitness(startBallPosition, velocity);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double[][] neighbors = generateNeighbors(velocity);
            for (double[] neighbor : neighbors) {
                double fitness = evaluateFitness(startBallPosition, neighbor);
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                    velocity = neighbor;
                }
            }
            if (Math.abs(bestFitness) <= TOLERANCE) {
                this.goal = true;
                break;
            }
        }
        return velocity;
    }

    private double[][] generateNeighbors(double[] currentVelocity) {
        double[][] neighbors = new double[8][2];
        int index = 0;
        for (double dx : new double[] {-STEP_SIZE, 0, STEP_SIZE}) {
            for (double dy : new double[] {-STEP_SIZE, 0, STEP_SIZE}) {
                if (dx != 0 || dy != 0) {
                    neighbors[index][0] = currentVelocity[0] + dx;
                    neighbors[index][1] = currentVelocity[1] + dy;
                    index++;
                }
            }
        }
        return neighbors;
    }

    private double evaluateFitness(double[] ballPosition, double[] velocity) {
        double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
        game.shoot(inputEngine, false);
        double[] finalPosition = game.getStoppoint();
        double distanceToTarget = calculateDistance(finalPosition, holePosition);
        return -distanceToTarget;
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
}
