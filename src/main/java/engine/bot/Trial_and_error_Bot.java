package engine.bot;
// TODO: Make the bot have a "neural network" to find path to hole
// TODO: Take the inputs used in the other solver classes
// -> output of the map is a 3 Dimensional double array
// TODO: Handle cases for different lengths of certain terrain
// TODO: Get the putts to under 5 m/s per putt
// TODO: Take gravitational force into account

// Import the RK4 Solver
/**
 * The Trial and Error bot
 */

import engine.solvers.RK4;

/**
 * Notes:
 * If no Obstacle -> velocity straight to the hole
 * Else -> Check 30-40° from the initial point for collisions
 * Then -> Go 30-40° from the point with velocity 5 m/s
 * If collision is not avoidable -> run it and calculate the collision
 * Maybe: If collision can be used as an advantage
 */

public class Trial_and_error_Bot implements BotInterface {
    double gravity = g;
    private RK4 rk4;

    public Trial_and_error_Bot() {
        this.rk4 = new RK4();
    }

    // Double[] x is the Array that contains the current position and the current Velocity
    public boolean collisionChecker(double[][][] map, double[] x, double[] friction, double[] hole, double gravity) {

        double[] direction = {hole[0] - x[0], hole[1] - x[1]};

        return false;
    }

    public boolean heightChecker(double[][][] map, double[] x) {
        return false;
    }

}
