import engine.bot.CheckCollisionAndHeight;
import engine.bot.ComparingAndScoring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ComparingAndScoringTest {

    private ComparingAndScoring bot;
    private CheckCollisionAndHeight checkCollisionAndHeight;

    @BeforeEach
    void setUp() {
        checkCollisionAndHeight = Mockito.mock(CheckCollisionAndHeight.class);
        bot = new ComparingAndScoring(checkCollisionAndHeight);
    }

    @Test
    void comparatorReturnsVectorWithHighestScore() {
        double[][][] map = new double[10][10][3];
        double[][] info = new double[10][10];
        double[] x = {0, 0, 0, 0};
        double[] friction = {0.3, 0.3};
        double[] hole = {9, 9};

        double[][] heightVec = new double[][]{{0, 0, 1, 1, 1}, {0, 0, 2, 2, 2}};
        double[][] collisionVec = new double[][]{{0, 0, 1, 1, 1}};

        Mockito.when(checkCollisionAndHeight.heightChecker(map, x, friction, hole)).thenReturn(heightVec);
        Mockito.when(checkCollisionAndHeight.collisionVectors(info, x, friction, hole)).thenReturn(collisionVec);

        double[] expected = {0, 0, 2, 2, 2};
        assertArrayEquals(expected, bot.comparingVectors(map, info, x, friction, hole));
    }

    @Test
    void comparatorReturnsNullWhenNoVectors() {
        double[][][] map = new double[10][10][3];
        double[][] info = new double[10][10];
        double[] x = {0, 0, 0, 0};
        double[] friction = {0.3, 0.3};
        double[] hole = {9, 9};

        double[][] heightVec = new double[0][0];
        double[][] collisionVec = new double[0][0];

        Mockito.when(checkCollisionAndHeight.heightChecker(map, x, friction, hole)).thenReturn(heightVec);
        Mockito.when(checkCollisionAndHeight.collisionVectors(info, x, friction, hole)).thenReturn(collisionVec);

        assertNull(bot.comparingVectors(map, info, x, friction, hole));
    }
}
