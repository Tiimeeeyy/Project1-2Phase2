package engine.bot;

import java.util.Arrays;

public class TestingClass {
    public static void main(String[] args) {
        // Create a flat terrain with no obstacles
        double[][][] map = new double[10][10][3];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                map[i][j][0] = i;
                map[i][j][1] = j;
                map[i][j][2] = 0; // Height is 0
            }
        }

        // Create a bot
        Trial_and_error_Bot bot = new Trial_and_error_Bot();

        // Set the initial position and target
        double[] x = {0, 0, 0, 0}; // Starting at the origin with no initial velocity
        double[] hole = {9, 9}; // Target is at the other end of the map
        double[] friction = {0.3, 0.3}; // Friction is constant everywhere
        double gravity = 9.8; // Gravity is constant

        // Call the collisionChecker method
        // double[][] result = CollisionChecker.heightChecker(map, x, friction, hole);

        // Print the result
        // System.out.println(Arrays.toString(result));
    }
}
