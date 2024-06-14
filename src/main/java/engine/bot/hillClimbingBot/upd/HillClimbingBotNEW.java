package engine.bot.hillClimbingBot.upd;

import java.util.ArrayList;
import java.util.Random;
import engine.solvers.GolfGameEngine;
import engine.bot.AibotGA.MapSearcher; 
import java.util.Random;


public class HillClimbingBotNEW {
    private boolean goal = false;
    private GolfGameEngine game;
    private double[] startBallPosition;
    private double[] holePosition;
    private double[] velocity;
    private String message = "";

    private static final int MAX_ITERATIONS = 10;
    private static final double INITIAL_STEP_SIZE = 1.0;
    private static final double TOLERANCE = 0.01;
    private static final int RANDOM_RESTARTS = 3;

    private MapSearcher mapSearcher;
    private ArrayList<double[]> turningPoints;
    private int currentTargetIndex = 0;

    public HillClimbingBotNEW(GolfGameEngine game, double[] startBallPosition, double[] holePosition, String mapPath, double radius) {
        this.game = game;
        this.startBallPosition = startBallPosition.clone();
        this.holePosition = holePosition;
        this.velocity = initializeVelocity();
        System.out.println(mapPath);
        this.mapSearcher = new MapSearcher(mapPath, startBallPosition, holePosition, radius);
        this.turningPoints = mapSearcher.getTurningPoints(mapSearcher.findShortestPath());
        this.turningPoints.add(holePosition);  
    }

    public ArrayList<double[]> hillClimbingAlgorithm() {
        ArrayList<double[]> shots = new ArrayList<>();
        this.goal = false;

        while (!this.goal && currentTargetIndex < turningPoints.size()) {
            double[] shot = hillClimbing();
            if (!message.contains("water")) {
                double[] currentShot = {startBallPosition[0], startBallPosition[1], shot[0], shot[1]};
                shots.add(currentShot.clone());
                
                startBallPosition = getTrajectory(startBallPosition, shot).clone();
                System.out.println(turningPoints.get(currentTargetIndex)[0] + " " + turningPoints.get(currentTargetIndex)[1]);
                if (calculateDistance(startBallPosition, turningPoints.get(currentTargetIndex)) <= TOLERANCE) {
                    currentTargetIndex++;
                    if (currentTargetIndex < turningPoints.size()) {
                        holePosition = turningPoints.get(currentTargetIndex); 
                    } else {
                        this.goal = true;
                    }
                }
            }
        }
        return shots;
    }

    private double[] hillClimbing() {
        double bestFitness = Double.NEGATIVE_INFINITY;
        double[] bestVelocity = new double[2];

        for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
            this.velocity = initializeVelocityTowardsNextTarget();  
            // this.velocity = initializeVelocity();
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
                    stepSize /= 2;
                } else {
                    stepSize = INITIAL_STEP_SIZE;
                }
                if (Math.abs(currentFitness) <= TOLERANCE || bestFitness > -0.2) {
                    this.goal = true;
                    break;
                }

                if (message != null && message.contains("water")) {
                    this.velocity = initializeVelocityTowardsNextTarget();
                    stepSize = INITIAL_STEP_SIZE;
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

    // make the ball go towards the next target(water radius)

    private double[] initializeVelocityTowardsNextTarget() {
        if (currentTargetIndex < turningPoints.size()) {
            double[] nextPoint = turningPoints.get(currentTargetIndex);
            double[] randomPoint = getRandomPointNear(nextPoint, 5);
            double dx = randomPoint[0] - startBallPosition[0];
            double dy = randomPoint[1] - startBallPosition[1];
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            System.out.println("WATER");
            return new double[]{dx / distance, dy / distance};
        } else {
            return initializeVelocity();
        }
    }
    
    private double[] getRandomPointNear(double[] point, double radius) {
        Random rand = new Random();
        double angle = 2 * Math.PI * rand.nextDouble();
        double r = radius * Math.sqrt(rand.nextDouble());
        double x = point[0] + r * Math.cos(angle);
        double y = point[1] + r * Math.sin(angle);
        return new double[]{x, y};
    }
    
    

    private double[][] generateNeighbors(double[] currentVelocity, double stepSize) {
        double[][] neighbors;
        if (message != null && message.contains("water")) {
            neighbors = new double[16][2];
        } else {
            neighbors = new double[8][2];
        }

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

        if (message != null && message.contains("water")) {
            for (double dx : new double[]{-2 * stepSize, 2 * stepSize}) {
                for (double dy : new double[]{-2 * stepSize, 2 * stepSize}) {
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
