package engine.bot.newtonRaphsonBot;

import java.util.ArrayList;
import java.util.Random;
import engine.solvers.GolfGameEngine;

public class NewtonRaphsonBot {
    private boolean goal = false;
    private GolfGameEngine game;
    private double[] startBallPosition;
    private double[] holePosition;
    private double[] velocity;
    private String message;

    private static final int MAX_ITERATIONS = 20;
    private static final int MAX_STAGNANT_ITERATIONS = 5;
    private static final double INITIAL_STEP_SIZE = 1.0;
    private static final double TOLERANCE = 0.01;


    public NewtonRaphsonBot(GolfGameEngine game, double[] startBallPosition, double[] holePosition) {
        this.game = game;
        this.startBallPosition = startBallPosition.clone();
        this.holePosition = holePosition;
        this.velocity = initializeVelocity();
    }

    public ArrayList<double[]> NewtonRaphsonMethod() {
        ArrayList<double[]> shots = new ArrayList<>();
        this.goal = false;

        while (!this.goal) {
            double[] shot = newtonRaphson();
            if (message == null || !message.contains("water")){
                double[] currentShot = {startBallPosition[0], startBallPosition[1], shot[0], shot[1]};
                shots.add(currentShot.clone());

                startBallPosition = getTrajectory(startBallPosition, shot);
            }
        }
        return shots;
    }

    public double[] newtonRaphson() {
        double[] velocity = initializeVelocity();
        double stepSize = INITIAL_STEP_SIZE;
        int iteration = 0;
        int stagnantIterations = 0;
        double lastFitness = Double.MAX_VALUE;

        while (iteration < MAX_ITERATIONS) {
            double[] grad = computeGradient(velocity);

            // Update velocity using Newton-Raphson method
            for (int i = 0; i < 2; i++) {
                velocity[i] -= stepSize * grad[i];
                velocity[i] = clamp(velocity[i], -5, 5);
            }

            double fitness = evaluateFitness(startBallPosition, velocity);
            if (Math.abs(fitness) <= TOLERANCE) {
                break;
            }

            // Adaptive step size adjustment
            if (fitness < lastFitness) {
                stepSize *= 1.1; // Increase step size if fitness improves
                stagnantIterations = 0; // Reset stagnant iteration counter
            } else {
                stepSize *= 0.5; // Decrease step size if fitness does not improve
                stagnantIterations++;
            }

            // Add random perturbation to escape local minima
            if (stagnantIterations >= MAX_STAGNANT_ITERATIONS) {
                System.out.println("Stagnation detected, trying new initial velocity.");
                velocity = initializeVelocity();
                stepSize = INITIAL_STEP_SIZE;
                stagnantIterations = 0;
            }

            // Print intermediate values for debugging
            System.out.println("Iteration " + iteration + ": Velocity = [" + velocity[0] + ", " + velocity[1] + "], Gradient = [" + grad[0] + ", " + grad[1] + "], Fitness = " + fitness + ", Step Size = " + stepSize);

            lastFitness = fitness;
            iteration++;
        }
        return velocity;
    }

    private double[] computeGradient(double[] velocity) {
        double h = 1e-5;
        double[] grad = new double[2];
        double originalFitness = evaluateFitness(startBallPosition, velocity);

        // Compute partial derivatives using central difference
        for (int i = 0; i < 2; i++) {
            double[] velocityPlusH = velocity.clone();
            double[] velocityMinusH = velocity.clone();
            velocityPlusH[i] += h;
            velocityMinusH[i] -= h;
            double fitnessPlusH = evaluateFitness(startBallPosition, velocityPlusH);
            double fitnessMinusH = evaluateFitness(startBallPosition, velocityMinusH);
            grad[i] = (fitnessPlusH - fitnessMinusH) / (2 * h);
        }
        return grad;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double evaluateFitness(double[] ballPosition, double[] velocity) {
        double[] finalPosition = getTrajectory(ballPosition, velocity);
        double distanceToTarget = calculateDistance(finalPosition, holePosition);
        if (message != null && message.contains("water")) {
            return -100;
        }
        return -distanceToTarget;
    }

    private double[] getTrajectory(double[] ballPosition, double[] velocity) {
        double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
        game.shoot(inputEngine, false);
        message = game.getMessage();
        return game.getStoppoint();
    }

    private double calculateDistance(double[] pos1, double[] pos2) {
        return Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2));
    }

    private double[] initializeVelocity() {
        Random random = new Random();
        double[] velocity = new double[2];
        velocity[0] = (random.nextDouble() * 10) - 5;
        velocity[1] = (random.nextDouble() * 10) - 5;
        return velocity;
    }
}
