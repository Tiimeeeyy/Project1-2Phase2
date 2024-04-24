package engine.bot;
/**
 * The Trial and Error bot
 * <p>
 * Notes:
 * If no Obstacle -> velocity straight to the hole
 * Else -> Check 30-40° from the initial point for collisions
 * Then -> Go 30-40° from the point with velocity 5 m/s
 * If collision is not avoidable -> run it and calculate the collision
 * Maybe: If collision can be used as an advantage
 * <p>
 * Notes:
 * If no Obstacle -> velocity straight to the hole
 * Else -> Check 30-40° from the initial point for collisions
 * Then -> Go 30-40° from the point with velocity 5 m/s
 * If collision is not avoidable -> run it and calculate the collision
 * Maybe: If collision can be used as an advantage
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
    private final CollisionChecker collisionChecker;
    public Trial_and_error_Bot(CollisionChecker collisionChecker) {
        this.collisionChecker = collisionChecker;
    }
    /**
     * The Golf game.
     */
    golfgame golfgame;
    // Game-plan: Compare the resulting Checkers and find the vector that fulfills both properties

    public double calculateScore(double[] vector, double[][] heightVec, double[][] collisionVec) {
        double score = 0;

        // Check if the vector is in the collisionVec
        for (double[] collisionVector : collisionVec) {
            if (vector[2] == collisionVector[2] && vector[3] == collisionVector[3]) {
                score -= 1000; // High negative score for collisions
                break;
            }
        }

        // Check if the vector is in the heightVec
        for (double[] heightVector : heightVec) {
            if (vector[2] == heightVector[2] && vector[3] == heightVector[3]) {
                score -= heightVector[4]; // Negative score for height gains
                break;
            }
        }

        return score;
    }

    public double[] comparingVectors(double[][][] map, double[][] info, double[] x, double[] friction, double[] hole) {
        double[][] heightVec = collisionChecker.heightChecker(map, x, friction, hole);
        double[][] collisionVec = collisionChecker.collisionVectors(info, x, friction, hole);

        double[] bestVector = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Iterate over each vector in the heightVec and collisionVec arrays
        for (double[] vector : heightVec) {
            double score = calculateScore(vector, heightVec, collisionVec);
            if (score > bestScore) {
                bestScore = score;
                bestVector = vector;
            }
        }

        return bestVector;
    }

    public boolean checkHole(double[] x, double[] hole) {
        double distance = Math.sqrt(Math.pow(x[0] - hole[0], 2)) + Math.pow(x[1] - hole[1], 2);
        return distance <= 5;
    }
}
