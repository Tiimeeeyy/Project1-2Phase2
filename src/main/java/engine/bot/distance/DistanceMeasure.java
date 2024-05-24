package engine.bot.distance;

import engine.solvers.GolfGameEngine;
import engine.solvers.odeSolvers.RK4;

import java.util.ArrayList;

public class DistanceMeasure {
    private final GolfGameEngine golfGame;
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

        this.golfGame = new GolfGameEngine(new RK4(), friction, 0.1, hole, radius, "src/main/resources/userInputMap.png");
    }


    /**
     * This method chooses a velocity based on certain rules.
     *
     * @param x The position of the ball.
     * @return the "assumed" velocity.
     */
    public double assumeVelocity(double[] x) {
        double distance = golfGame.getDistance(x, hole);
        if (distance >= 40) {
            return 5;
        } else if (distance >= 30) {
            return 4;
        } else if (distance >= 20) {
            return 3;
        } else if (distance >= 10) {
            return 2;
        } else if (distance >= 5) {
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
     * Calculates one play for the Rule based bot based on the distance to the hole
     * after the play
     * @param position The position of the ball.
     * @param hole The position of the hole.
     * @return A list containing the best play from the position (velocityX, velocityY, power)
     */
    public double[] getOnePlay (double[] position, double[] hole) {
        double[] direction = calculateDirection(position, hole);
        double[][] velocities = createVelocityVectors(direction);

        double maxDistances = 0;
        double[] bestShot = null;

        for (double[] velocityVector : velocities) {
            double[] point = position.clone();

            ArrayList<double[]> trajectory = golfGame.shoot(new double[]{point[0], point[1], velocityVector[0] * assumeVelocity(point), velocityVector[1] * assumeVelocity(point)}, true);

            double distance = golfGame.getDistance(position, trajectory.getLast());

            if (distance > maxDistances) {
                maxDistances = distance;
                bestShot = new double[]{velocityVector[0], velocityVector[1], assumeVelocity(point)};
            }
        }
        if (bestShot != null) {
            return bestShot;
        }
        return new double[0];
    }
    public ArrayList<double[]> playGame (double[] position, double[] hole, boolean reachedHole) {
        ArrayList<double[]> gameplay = new ArrayList<>();
        if (!reachedHole) {
            System.out.println("While loop entered");
            double[] play = getOnePlay(position,hole);
            gameplay.add(play);
            if (checkHole(position, hole)) {
                System.out.println("If entered");
                double[] lastPlay = lastShot(position, hole);
                gameplay.add(lastPlay);
            }
        }
        return gameplay;
    }
}
