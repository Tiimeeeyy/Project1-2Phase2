package engine.bot.rule_based;

import engine.solvers.golfphysics;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * This class is responsible for checking collisions and height differences for the Bot's decision-making process.
 * It uses the RungeKutta4Void solver to simulate the physics of the golf ball.
 */
public class CheckCollisionAndHeight {
    // Constants that are used a lot, and don't change are declared here for maintainability and flexibility.
    private final double DEGREES_POS = Math.toRadians(30);
    private final double DEGREES_NEG = Math.toRadians(-30);
    private final double MIN_SPEED = 0.5;
    private final RungeKutta4Void rk4;
    private final PredictVelocity predictVelocity;

    /**
     * Class Constructor
     *
     * @param rk4 The RungeKutta4Void solver to be used for physics simulation.
     */
    public CheckCollisionAndHeight(RungeKutta4Void rk4, PredictVelocity predictVelocity) {
        this.rk4 = rk4;
        this.predictVelocity = predictVelocity;
    }



    /**
     * Calculate the direction vector from the current position to the hole.
     *
     * @param x    The current position
     * @param hole The position of the hole.
     * @return The direction Vector.
     */
    public double[] calculateDirection(double[] x, double[] hole) {

        double[] direction = {hole[0] - x[0], hole[1] - x[1]};
        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        direction[0] /= magnitude;
        direction[1] /= magnitude;

        return direction;
    }


    /**
     * Calculate the velocity Vectors that don't have collisions.
     *
     * @param info     The information about the golf course.
     * @param x        The current position.
     * @param friction The frictions of the golf course.
     * @param hole     The position of the hole.
     * @return The velocity vectors that don't have collisions.
     */
    public double[][] calculateCollisionVectors(double[][] info, double[] x, double[] friction, double[] hole) {
        double[] direction = calculateDirection(x, hole);
        double[][] velocities = createVelocityVectors(direction);
        return filterCollisionFreeVelocities(velocities, info, x, friction);
    }

    /**
     * Filters Velocity Vectors and return those that don't have collisions.
     *
     * @param velocities The velocity Vectors.
     * @param info       Information about the golf course.
     * @param x          The current position.
     * @param friction   The frictions of the golf course.
     * @return The velocity Vectors that don't have collisions.
     */
    private double[][] filterCollisionFreeVelocities(double[][] velocities, double[][] info, double[] x, double[] friction) {
        double speed = predictVelocity.assumeVelocity(x);
        ArrayList<double[]> noCollisionVelocities = new ArrayList<>();

        for (double[] velocity : velocities) {
            if (!hasCollision(info, x, friction, velocity)) {
                noCollisionVelocities.add(velocity);
            }
        }
        return noCollisionVelocities.toArray(new double[noCollisionVelocities.size()][]);
    }

    /**
     * Checks if a Velocity Vector leads to a Collision
     *
     * @param info     The information about the golf course
     * @param x        The current position.
     * @param friction The frictions of the golf course
     * @param velocity The velocity vectors.
     * @return True if the Vector leads to a collision, false otherwise.
     */
    private boolean hasCollision(double[][] info, double[] x, double[] friction, double[] velocity) {
        double speed = predictVelocity.assumeVelocity(x);
        double[] position = {x[0], x[1]};

        while (speed > MIN_SPEED) {
            double[] a = {friction[1], friction[0]};
            double[] dh = {0, 0};

            rk4.nextstep(new golfphysics(), new double[]{position[0], position[1], velocity[0], velocity[1]}, a, dh, 0.1);

            position[0] += velocity[0] * 0.1;
            position[1] += velocity[1] * 0.1;

            if (infoCollision(info, position)) {
                return true;
            }
            speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);
        }
        return false;
    }

    /**
     * Checks if a position leads to a collision.
     *
     * @param info     The information about the golf course.
     * @param position The position to check.
     * @return True if the position leads to a collision, false otherwise.
     */
    private boolean infoCollision(double[][] info, double[] position) {

        int x = (int) Math.floor(position[0]);
        int y = (int) Math.floor(position[1]);

        return x >= 0 && x < info.length && y >= 0 && y < info[0].length && info[x][y] != 0;

    }

    /**
     * Creates Velocity vectors based on a direction (in this case, the hole).
     *
     * @param direction The direction the vectors are facing.
     * @return The velocity Vectors.
     */
    public double[][] createVelocityVectors(double[] direction) {

        double[][] velocities = new double[3][2];
        velocities[0] = new double[]{5 * direction[0], 5 * direction[1]};
        velocities[1] = new double[]{5 * Math.cos(DEGREES_POS) * direction[0] - Math.sin(DEGREES_POS) * direction[1], 5 * Math.sin(DEGREES_POS) * direction[0] + Math.cos(DEGREES_POS) * direction[1]};
        velocities[2] = new double[]{5 * Math.cos(DEGREES_NEG) * direction[0] - Math.sin(DEGREES_NEG) * direction[1], 5 * Math.sin(DEGREES_NEG) * direction[0] + Math.cos(DEGREES_NEG) * direction[1]};

        return velocities;

    }

    /**
     * Checks height differences for each velocity Vector.
     *
     * @param map      The 3d (x,y,z) map of the golf course.
     * @param x        The current position.
     * @param friction The frictions of the golf course.
     * @param hole     The position of the hole.
     * @return The velocity Vectors sorted by the height they have to traverse.
     */
    public double[][] heightChecker(double[][][] map, double[] x, double[] friction, double[] hole) {
        double speed = predictVelocity.assumeVelocity(x);

        double[] direction = calculateDirection(x, hole);

        double[][] velocities = createVelocityVectors(direction);
        ArrayList<double[]> heightVelocities = new ArrayList<>();

        for (double[] velocity : velocities) {

            double[] position = {x[0], x[1]};


            while (speed > MIN_SPEED) {

                double[] a = {friction[1], friction[0]};
                double[] dh = {0, 0};

                rk4.nextstep(new golfphysics(), new double[]{position[0], position[1], velocity[0], velocity[1]}, a, dh, 0.1);

                position[0] += velocity[0] * 0.1;
                position[1] += velocity[1] * 0.1;

                speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

            }

            int finalX = (int) Math.floor(position[0]);
            int finalY = (int) Math.floor(position[1]);

            if (heightDifference(map, position)) {
                double height = map[finalX][finalY][2];
                heightVelocities.add(new double[]{position[0], position[1], velocity[0], velocity[1], height});

            }

        }

        heightVelocities.sort(Comparator.comparingDouble(a -> a[4]));

        double[][] heightVelocitiesArray = new double[heightVelocities.size()][5];
        return heightVelocities.toArray(heightVelocitiesArray);

    }

    /**
     * Checks if there is a significant height difference at a position.
     *
     * @param map      The 3d (x,y,z) map of the golf course.
     * @param position The position to check.
     * @return True if there is a significant height difference.
     */
    private boolean heightDifference(double[][][] map, double[] position) {

        int finalX = (int) Math.floor(position[0]);
        int finalY = (int) Math.floor(position[1]);

        return finalX >= 0 && finalX < map.length && finalY >= 0 && finalY < map[0].length;
    }

}
