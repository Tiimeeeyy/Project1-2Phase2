import engine.bot.CheckCollisionAndHeight;
import engine.bot.ComparingAndScoring;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ScoringTest {

    @Test
    void calculateScoreReturnsNegativeForCollisionVectors() {
        CheckCollisionAndHeight checkCollisionAndHeight = Mockito.mock(CheckCollisionAndHeight.class);
        ComparingAndScoring bot = new ComparingAndScoring(checkCollisionAndHeight);

        double[] vector = {0, 0, 1, 1, 1};
        double[][] heightVec = new double[][]{{0, 0, 2, 2, 2}};
        double[][] collisionVec = new double[][]{{0, 0, 1, 1, 1}};

        double score = bot.calculateScore(vector, heightVec, collisionVec);

        assertTrue(score < 0);
    }

    @Test
    void calculateScoreReturnsNegativeForHeightVectors() {
        CheckCollisionAndHeight checkCollisionAndHeight = Mockito.mock(CheckCollisionAndHeight.class);
        ComparingAndScoring bot = new ComparingAndScoring(checkCollisionAndHeight);

        double[] vector = {0, 0, 2, 2, 2};
        double[][] heightVec = new double[][]{{0, 0, 2, 2, 2}};
        double[][] collisionVec = new double[][]{{0, 0, 1, 1, 1}};

        double score = bot.calculateScore(vector, heightVec, collisionVec);

        assertTrue(score < 0);
    }

    @Test
    void checkHoleReturnsTrueWhenWithinDistance() {
        CheckCollisionAndHeight checkCollisionAndHeight = Mockito.mock(CheckCollisionAndHeight.class);
        ComparingAndScoring bot = new ComparingAndScoring(checkCollisionAndHeight);

        double[] x = {0, 0};
        double[] hole = {3, 4};

        assertTrue(bot.checkHole(x, hole));
    }

    @Test
    void checkHoleReturnsFalseWhenOutsideDistance() {
        CheckCollisionAndHeight checkCollisionAndHeight = Mockito.mock(CheckCollisionAndHeight.class);
        ComparingAndScoring bot = new ComparingAndScoring(checkCollisionAndHeight);

        double[] x = {0, 0};
        double[] hole = {6, 8};

        assertFalse(bot.checkHole(x, hole));
    }
}