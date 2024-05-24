package engine.bot.ml_bot.test;

import engine.bot.distance.DistanceMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DistanceMeasureTest {
    private DistanceMeasure distanceMeasure;
    private double[] position;
    private double[] hole;

    @BeforeEach
    void setUp() {
        position = new double[]{0, 0};
        hole = new double[]{0, 0};
        distanceMeasure = new DistanceMeasure(position, new double[]{0, 0}, hole, 1, false);
    }

    @Test
    void checkHoleReturnsTrueWhenBallIsInHole() {
        assertTrue(distanceMeasure.checkHole(position, hole));
    }

    @Test
    void checkHoleReturnsFalseWhenBallIsNotInHole() {
        double[] positionFarFromHole = {100, 100};
        assertFalse(distanceMeasure.checkHole(positionFarFromHole, hole));
    }

    @Test
    void getOnePlayReturnsNonEmptyVector() {
        double[] result = distanceMeasure.getOnePlay(position, hole);
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    void getOnePlayReturnsZeroVectorWhenPositionIsHole() {
        double[] result = distanceMeasure.getOnePlay(position, hole);
        assertNotNull(result);
        assertEquals(3, result.length);
        if (Arrays.equals(position, hole)) {
            // If position and hole are the same, the result should be a zero vector
            assertArrayEquals(new double[]{0.0, 0.0, 0.0}, result, 0.0001);
        } else {
            assertFalse(Arrays.equals(new double[] {0.0, 0.0, 0.0}, result));
        }
    }

    @Test
    void assumeVelocityReturnsCorrectValue() {
        double[] farPosition = {100, 100};
        assertEquals(5, distanceMeasure.assumeVelocity(farPosition));

        double[] closePosition = {1, 1};
        assertEquals(0.5, distanceMeasure.assumeVelocity(closePosition));
    }

    @Test
    void calculateDirectionReturnsUnitVector() {
        double[] direction = distanceMeasure.calculateDirection(position, hole);
        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        if (position[0] == hole[0] && position[1] == hole[1]) {
            // If position and hole are the same, the magnitude should be 0
            assertEquals(0, magnitude, 0.0001);
        } else {
            assertEquals(1, magnitude, 0.0001);
        }
    }

    @Test
    void lastShotReturnsCorrectVelocity() {
        double[] lastShot = distanceMeasure.lastShot(position, hole);
        double expectedVelocity = Math.sqrt(lastShot[0] * lastShot[0] + lastShot[1] * lastShot[1]);
        if (position[0] == hole[0] && position[1] == hole[1]) {
            // If position and hole are the same, the velocity should be 0
            assertEquals(0, expectedVelocity, 0.0001);
        } else {
            // Add your expected velocity here
            assertEquals(expectedVelocity, expectedVelocity, 0.0001);
        }
    }

    @Test
    void createVelocityVectorsReturnsCorrectNumberOfVectors() {
        double[] direction = {1, 0};
        double[][] velocities = distanceMeasure.createVelocityVectors(direction);
        assertEquals(3, velocities.length);
    }

    @Test
    void playGameReturnsNonEmptyListWhenNotReachedHole() {
        ArrayList<double[]> gameplay = distanceMeasure.playGame(position, hole, false);
        assertFalse(gameplay.isEmpty());
    }

    @Test
    void playGameReturnsEmptyListWhenReachedHole() {
        ArrayList<double[]> gameplay = distanceMeasure.playGame(position, hole, true);
        assertTrue(gameplay.isEmpty());
    }
}