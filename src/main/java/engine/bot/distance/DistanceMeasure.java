package engine.bot.distance;

import engine.solvers.GolfGame;
import engine.solvers.odeSolvers.RK4;
import ui.controller.ThirdScreenController;

import java.util.ArrayList;

public class DistanceMeasure  {
    private final GolfGame golfGame;
    private final double[] hole;
    private final double[] position;
    private final boolean reachedHole;

    /**
     * Constructor for the class of the Rule-Based bot.
     *
     * @param position    Position of the ball.
     * @param friction    Frictions of the Surfaces.
     * @param hole        Position of the hole.
     * @param radius      Radius of the hole.
     * @param reachedHole True if the ball has reached the hole, false otherwise.
     */
    public DistanceMeasure(double[] position, double[] friction, double[] hole, double radius, boolean reachedHole) {
        this.reachedHole = reachedHole;
        this.position = position;
        this.hole = hole;

        this.golfGame = new GolfGame(new RK4(), friction, 0.1, hole, radius, "src/main/resources/userInputMap.png");
    }

    /**
     * This method calculates the "best" trajectory from shooting the ball
     *
     * @param position Position of the ball.
     * @param hole     Position of the hole.
     * @return A list containing the best trajectory.
     */
    public ArrayList<double[]> bestDistance(double[] position, double[] hole) {
        double[] direction = calculateDirection(position, hole);
        double[][] velocities = createVelocityVectors(direction);

        double maxDistance = 0;

        ArrayList<double[]> farthestTrajectory = null;

        for (double[] velocityVector : velocities) {
            double[] point = position.clone();

            ArrayList<double[]> trajectory = golfGame.shoot(new double[]{point[0], point[1], velocityVector[0], velocityVector[1]}, true);

            double distance = golfGame.getDistance(position, trajectory.getLast());

            if (distance > maxDistance) {
                maxDistance = distance;
                farthestTrajectory = trajectory;
            }
        }
        return farthestTrajectory;
    }

    /**
     * This method recursively creates trajectories and adds them to the list to be animated later.
     *
     * @param x    The position of the ball
     * @param hole The position of the Hole.
     * @return List containing a list of all Trajectories.
     */
    public ArrayList<ArrayList<double[]>> recursiveDistances(double[] x, double[] hole) {
        ArrayList<ArrayList<double[]>> allTrajectories = new ArrayList<>();
        ArrayList<double[]> farthestTrajectory = bestDistance(x, hole);

        if (farthestTrajectory != null) {
            allTrajectories.add(farthestTrajectory);
        }

        double[] finalPosition = farthestTrajectory.getLast();

        if (!checkHole(finalPosition, hole)) {
            ArrayList<ArrayList<double[]>> recursiveTrajectories = recursiveDistances(finalPosition, hole);
            if (recursiveTrajectories != null) {
                allTrajectories.addAll(recursiveTrajectories);
            }
        } else {
            double[] lastVelocity = lastShot(x, hole);
            ArrayList<double[]> finalTrajectory = golfGame.shoot(new double[]{finalPosition[0], finalPosition[1], lastVelocity[0], lastVelocity[1]}, true);
            if (finalTrajectory != null) {
                allTrajectories.add(finalTrajectory);
            }
        }
        return allTrajectories;
    }

    /**
     * This method chooses a velocity based on certain rules.
     *
     * @param x The position of the ball.
     * @return the "assumed" velocity.
     */
    public double assumeVelocity(double[] x) {
        double distance = golfGame.getDistance(x, hole);
        if (distance >= 400) {
            return 5;
        } else if (distance >= 300) {
            return 4;
        } else if (distance >= 200) {
            return 3;
        } else if (distance >= 100) {
            return 2;
        } else if (distance >= 50) {
            return 1;
        } else return 0.5;

    }

    /**
     * Calculates the direction / angle from the ball position to the hole.
     *
     * @param x    The position of the Ball.
     * @param hole The position of the Hole.
     * @return A vector containing the direction.
     */
    public double[] calculateDirection(double[] x, double[] hole) {

        double[] direction = {hole[0] - x[0], hole[1] - x[1]};
        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        direction[0] /= magnitude;
        direction[1] /= magnitude;

        return direction;
    }

    /**
     * This Method calculates the last shot, which is used, when the ball is 5m away from the hole.
     *
     * @param x    The position of the Ball.
     * @param hole The position of the Hole.
     * @return The array containing the information containing the position and Velocity for the last shot.
     */
    public double[] lastShot(double[] x, double[] hole) {
        double distance = golfGame.getDistance(x, hole);

        double[] direction = {hole[0] - x[0], hole[1] - x[1]};

        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);

        direction[0] /= magnitude;
        direction[1] /= magnitude;

        return new double[]{distance * direction[0], distance * direction[1]};
    }

    /**
     * This method creates Velocity vectors.
     *
     * @param direction The direction from the position to the hole.
     * @return An array containing all the Velocity vectors.
     */
    public double[][] createVelocityVectors(double[] direction) {
        double velo = assumeVelocity(position);
        double[] currentPos = position.clone();
        double degreesPos = Math.toRadians(30);
        double degreesNeg = Math.toRadians(-30);
        double[][] velocities = new double[3][2];
        velocities[0] = new double[]{velo * currentPos[0], velo * currentPos[1]};
        velocities[1] = new double[]{velo * Math.cos(degreesPos) * direction[0] - Math.sin(degreesPos) * direction[1], velo * Math.sin(degreesPos) * direction[0] + Math.cos(degreesPos) * direction[1]};
        velocities[2] = new double[]{velo * Math.cos(degreesNeg) * direction[0] - Math.sin(degreesNeg) * direction[1], velo * Math.sin(degreesNeg) * direction[0] + Math.cos(degreesNeg) * direction[1]};

        return velocities;

    }

    /**
     * Checks if the ball is a certain distance away from the hole.
     *
     * @param x    The position of the Ball.
     * @param hole The position of the Hole.
     * @return True if the ball is closer than 5m to the hole, false otherwise.
     */
    public boolean checkHole(double[] x, double[] hole) {
        double distance = golfGame.getDistance(x, hole);
        return distance <= 5;
    }

    /**
     * This Method "plays" the game and animates the ball movement.
     *
     * @param hole        The position of the Hole.
     * @param position    The position of the Ball.
     * @param reachedHole Boolean describing if the ball already has reached the hole.
     */
    public ArrayList<double[]> playGameGame(double[] hole, double[] position, boolean reachedHole) {
        if (reachedHole) {
            return null;
        }
        double[] x = new double[]{position[0], position[1]};
        ArrayList<ArrayList<double[]>> trajectories = recursiveDistances(x, hole);
        for (ArrayList<double[]> trajectory : trajectories) {
            if (reachedHole || trajectory.isEmpty()) {
                break;
            }
            return trajectory;
        }
        return null;
    }

}
