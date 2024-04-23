package engine.bot;

// Import the RK4 Solver
/**
 * The Trial and Error bot
 */


/**
 * Notes:
 * If no Obstacle -> velocity straight to the hole
 * Else -> Check 30-40° from the initial point for collisions
 * Then -> Go 30-40° from the point with velocity 5 m/s
 * If collision is not avoidable -> run it and calculate the collision
 * Maybe: If collision can be used as an advantage
 */
import engine.bot.CollisionChecker;
import engine.solvers.golfgame;

/**
 * The type Trial and error bot.
 */
public class Trial_and_error_Bot implements BotInterface {
    /**
     * The Collision checker.
     */
    CollisionChecker collisionChecker;
    /**
     * The Golfgame.
     */
    golfgame golfgame;
    // Gameplan: Compare the resulting Checkers and find the vector that fulfills both properties

    public double[] Comparator(double[][][] map, double[][] info, double[] x, double[] friction, double[] hole) {
        double[][] heightVec = collisionChecker.heightChecker(map, x, friction, hole);
        double[][] collisionVec = collisionChecker.collisionChecker(info, x, friction, hole);
        return null;
    }


}
