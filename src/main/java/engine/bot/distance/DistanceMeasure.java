package engine.bot.distance;

import engine.solvers.BallStatus;
import engine.solvers.GolfGameEngine;
import engine.solvers.odeSolvers.RK4;

import java.util.ArrayList;

public class DistanceMeasure {
    private final GolfGameEngine golfGame;
    private final double[] hole;
    private final double[] position;
    private  boolean reachedHole = false;

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
        this.reachedHole = false;
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
        if (distance >= 20) {
            return 5;
        } else if (distance > 15) {
            return 4;
        } else if (distance < 10) {
            return 3;
        }
        return 4;
    }

    public BallStatus getBallStatus(){
        return golfGame.getStatus();
    }


    /**
     * Calculates the direction / angle from the ball position to the hole.
     *
     * @param end   The position of the Ball.
     * @param start The position of the Hole.
     * @return A vector containing the direction.
     */
    public static double[] calculateDirection(double[] start, double[] end) {
        // Calculate the direction vector
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];

        // Print intermediate steps for debugging
        // System.out.println("dx: " + dx + ", dy: " + dy);

        // Calculate the magnitude of the direction vector
        double magnitude = Math.sqrt(dx * dx + dy * dy);

        // Print magnitude for debugging
        // System.out.println("magnitude: " + magnitude);

        // Normalize the direction vector
        if (magnitude != 0) {
            dx /= magnitude;
            dy /= magnitude;
        }

        // Print normalized direction for debugging
        // System.out.println("Normalized direction: (" + dx + ", " + dy + ")");

        return new double[]{dx, dy};
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
        if (magnitude != 0) {
            direction[0] /= magnitude;
            direction[1] /= magnitude;
        }else {
            direction[0] = 0;
            direction[1] = 0;
        }
        return new double[]{distance * direction[0], distance * direction[1]};
    }

    /**
     * This method creates Velocity vectors.
     *
     * @param direction The direction from the position to the hole.
     * @return An array containing all the Velocity vectors.
     */
    public double[][] createVelocityVectors(double[] direction) {
        double degreesPos = Math.toRadians(30);
        double degreesNeg = Math.toRadians(-30);
        double[][] velocities = new double[3][2];
        velocities[0] = new double[]{direction[0],direction[1]};
        velocities[1] = new double[]{Math.cos(degreesPos) * direction[0] - Math.sin(degreesPos) * direction[1],Math.sin(degreesPos) * direction[0] + Math.cos(degreesPos) * direction[1]};
        velocities[2] = new double[]{Math.cos(degreesNeg) * direction[0] - Math.sin(degreesNeg) * direction[1],Math.sin(degreesNeg) * direction[0] + Math.cos(degreesNeg) * direction[1]};

        return velocities;
        // hello

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
        return distance <= 1;
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

        double maxDistances = Double.MAX_VALUE;
        double[] bestShot = null;

        for (double[] velocityVector : velocities) {
            double[] point = position.clone();

            ArrayList<double[]> trajectory = golfGame.shoot(new double[]{point[0], point[1], velocityVector[0] * assumeVelocity(point), velocityVector[1] * assumeVelocity(point)}, true);
            BallStatus status = getBallStatus();
            double distance = golfGame.getDistance(position, trajectory.getLast());
            if (status == BallStatus.HitWater || status == BallStatus.OutOfBoundary) {
                continue;
            }
            if (distance < maxDistances) {
                maxDistances = distance;
                bestShot = new double[]{velocityVector[0], velocityVector[1], assumeVelocity(point)};
            }
        }
        if (bestShot != null) {
            return bestShot;
        }
        return new double[0];
    }
    public ArrayList<double[]> playGame (double[] position, double[] hole) {
        ArrayList<double[]> gameplay = new ArrayList<>();
        this.reachedHole=false;

        while (this.reachedHole==false) {
            if (checkHole(position, hole)) {
                this.reachedHole = true;
                System.out.println("If entered");
                double[] lastPlay = lastShot(position, hole);
                gameplay.add(lastPlay);
                return gameplay;
            } else{
                System.out.println("else entered");

                // System.out.println("While loop entered");
                double[] play = getOnePlay(position,hole);
                gameplay.add(play);
            }

        }
        return null;

    }
}
