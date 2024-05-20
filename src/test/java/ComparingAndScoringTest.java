import engine.bot.rule_based.CheckCollisionAndHeight;
import engine.bot.rule_based.ComparingAndScoring;
import engine.bot.rule_based.PredictVelocity;
import engine.bot.rule_based.RungeKutta4Void;
import engine.solvers.GolfGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComparingAndScoringTest {
    private ComparingAndScoring comparingAndScoring;
    private CheckCollisionAndHeight checkCollisionAndHeight;
    private PredictVelocity predictVelocity;
    private GolfGame golfGame;

    @BeforeEach
    public void setup() {
        golfGame = new GolfGame();
        predictVelocity = new PredictVelocity(golfGame);
        RungeKutta4Void rk4Void = new RungeKutta4Void();
        checkCollisionAndHeight = new CheckCollisionAndHeight(rk4Void, predictVelocity);
        comparingAndScoring = new ComparingAndScoring(checkCollisionAndHeight);
    }

    @Test
    public void calculateScoreReturnsHighNegativeForCollision() {
        double[] vector = {0, 0, 1, 1};
        double[][] heightVec = {};
        double[][] collisionVec = {{0, 0, 1, 1}};
        double score = comparingAndScoring.calculateScore(vector, heightVec, collisionVec);
        assertTrue(score < -999);
    }

    @Test
    public void calculateScoreReturnsZeroForNoCollisionOrHeightGain() {
        double[] vector = {0, 0, 1, 1};
        double[][] heightVec = {};
        double[][] collisionVec = {};
        double score = comparingAndScoring.calculateScore(vector, heightVec, collisionVec);
        assertEquals(0, score);
    }

    @Test
    public void comparingVectorsReturnsNonNullVector() {
        double[][][] map = new double[500][500][3];
        double[][] info = new double[500][500];
        double[] x = {0, 0};
        double[] friction = {0.5, 3};
        double[] hole = {10, 10};
        double[] bestVector = comparingAndScoring.comparingVectors(map, info, x, friction, hole);
        assertNotNull(bestVector);
    }

    @Test
    public void checkHoleReturnsTrueWhenExactlyAtHole() {
        double[] x = {5, 5};
        double[] hole = {5, 5};
        assertTrue(comparingAndScoring.checkHole(x, hole));
    }

    @Test
    public void checkHoleReturnsTrueWhenJustOutsideHole() {
        double[] x = {5.1, 5.1};
        double[] hole = {5, 5};
        assertTrue(comparingAndScoring.checkHole(x, hole));
    }
}