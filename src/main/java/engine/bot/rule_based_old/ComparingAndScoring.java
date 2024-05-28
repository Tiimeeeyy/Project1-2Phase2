package engine.bot.rule_based_old;


//  Notes:
//  If no Obstacle -> velocity straight to the hole
//  Else -> Check 30-40° from the initial point for collisions
//  Then -> Go 30-40° from the point with velocity 5 m/s
//  If collision is not avoidable -> run it and calculate the collision
//  Maybe: If collision can be used as an advantage

import java.util.Arrays;

/**
 * This class is responsible for comparing and scoring different vectors for the bot to "play" the game
 * It uses the CheckCollisionAndHeight class to check for 1. collisions and 2. height differences.
 */

public class ComparingAndScoring implements BotInterface {
    private final CheckCollisionAndHeight collisionChecker;

    /**
     * Constructor for the ComparingAndScoring class, lets us use the CheckCollisionAndHeight class.
     *
     * @param checkCollisionAndHeight Constructor argument for calling and creating the object.
     */
    public ComparingAndScoring(CheckCollisionAndHeight checkCollisionAndHeight) {
        this.collisionChecker = checkCollisionAndHeight;
    }

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
                score -= heightVector[4]; // Low negative score for height gains
                break;
            }
        }

        return score;
    }

    public double[] comparingVectors(double[][][] map, double[][] info, double[] x, double[] friction, double[] hole) {
        double[][] heightVec = collisionChecker.heightChecker(map, x, friction, hole);
        double[][] collisionVec = collisionChecker.calculateCollisionVectors(info, x, friction, hole);

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
        System.out.println("Best Vector " + Arrays.toString(bestVector));
        return bestVector;

    }

    public boolean checkHole(double[] x, double[] hole) {
        double distance = Math.sqrt(Math.pow(x[0] - hole[0], 2)) + Math.pow(x[1] - hole[1], 2);
        return distance <= 5;
    }
}
