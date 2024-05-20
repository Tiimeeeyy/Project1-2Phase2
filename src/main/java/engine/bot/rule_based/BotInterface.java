package engine.bot.rule_based;

/**
 * The interface Bot interface for the bot classes
 * These methods are the ones used by all bot classes, the "Dumb" bot and the "Smart" bot.
 */
public interface BotInterface {

    /**
     * Calculates a score for Vectors based on whether they lead to a Collision or a height gain with different weights.
     *
     * @param vector       The Vector to be Scored.
     * @param heightVec    The array of vectors that lead to a height gain.
     * @param collisionVec The array of vectors that lead to a collision.
     * @return The score of the Vector.
     */
    double calculateScore(double[] vector, double[][] heightVec, double[][] collisionVec);

    /**
     * Compares different Vectors and return the one with the highest score.
     *
     * @param map      The 3d (x,y,z) map of the golf game.
     * @param info     The information about the golf course.
     * @param x        The current position.
     * @param friction The frictions of the golf course.
     * @param hole     The position of the hole.
     * @return The vector with the highest score.
     */
    double[] comparingVectors(double[][][] map, double[][] info, double[] x, double[] friction, double[] hole);

    /**
     * Checks if the current position is within a certain distance from the hole.
     *
     * @param x    The current position.
     * @param hole The position of the hole.
     * @return True if the ball is withing 5(m) of the hole, false otherwise.
     */
    boolean checkHole(double[] x, double[] hole);
}
