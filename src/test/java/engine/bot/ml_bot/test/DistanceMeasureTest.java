package engine.bot.ml_bot.test;


import engine.bot.distance.DistanceMeasure;
import engine.solvers.GolfGameEngine;
import engine.solvers.odeSolvers.RK4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DistanceMeasureTest {
    private DistanceMeasure distanceMeasure;
    private GolfGameEngine golfGame;

    @BeforeEach
    void setUp() {
        distanceMeasure = new DistanceMeasure(new double[]{0, 0,0,0}, new double[]{0.1, 0.1}, new double[]{234,345, 577}, 0.5, false);
        golfGame = new GolfGameEngine(new RK4(), new double[]{0, 0}, 0.1, new double[]{234, 345, 577}, 0.5, "src/main/resources/userInputMap.png");
    }

    @Test
    void shouldReturnNonNullBestDistance() {
        ArrayList<double[]> bestDistance = distanceMeasure.bestDistance(new double[]{0, 0}, new double[]{10, 10});
        assertNotNull(bestDistance);
    }

    @Test
    void shouldReturnNonNullRecursiveDistances() {
        ArrayList<ArrayList<double[]>> recursiveDistances = distanceMeasure.recursiveDistances(new double[]{0, 0}, new double[]{10, 10});
        assertNotNull(recursiveDistances);
    }

    @Test
    void shouldReturnCorrectAssumedVelocity() {
        double velocity = distanceMeasure.assumeVelocity(new double[]{0, 0});
        assertEquals(0.5, velocity);
    }

    @Test
    void shouldReturnCorrectDirection() {
        double[] direction = distanceMeasure.calculateDirection(new double[]{0, 0}, new double[]{10, 10});
        assertArrayEquals(new double[]{1, 1}, direction);
    }

    @Test
    void shouldReturnCorrectLastShot() {
        double[] lastShot = distanceMeasure.lastShot(new double[]{0, 0}, new double[]{10, 10});
        assertArrayEquals(new double[]{10, 10}, lastShot);
    }

    @Test
    void shouldReturnCorrectVelocityVectors() {
        double[][] velocityVectors = distanceMeasure.createVelocityVectors(new double[]{1, 1});
        assertArrayEquals(new double[]{0.5, 0.5}, velocityVectors[0]);
    }

    @Test
    void shouldReturnFalseWhenCheckHoleWithDistanceMoreThan5() {
        boolean isNearHole = distanceMeasure.checkHole(new double[]{0, 0}, new double[]{10, 10});
        assertFalse(isNearHole);
    }

    @Test
    void shouldReturnTrueWhenCheckHoleWithDistanceLessThan5() {
        boolean isNearHole = distanceMeasure.checkHole(new double[]{0, 0}, new double[]{3, 4});
        assertTrue(isNearHole);
    }

    @Test
    void shouldNotContainNullInBestDistance() {
        ArrayList<double[]> bestDistance = distanceMeasure.bestDistance(new double[]{0, 0}, new double[]{10, 10});
        for (double[] element : bestDistance) {
            assertNotNull(element);
        }
    }

    @Test
    void shouldNotContainNullInRecursiveDistances() {
        ArrayList<ArrayList<double[]>> recursiveDistances = distanceMeasure.recursiveDistances(new double[]{0, 0}, new double[]{10, 10});
        for (ArrayList<double[]> trajectory : recursiveDistances) {
            for (double[] element : trajectory) {
                assertNotNull(element);
            }
        }
    }
}
