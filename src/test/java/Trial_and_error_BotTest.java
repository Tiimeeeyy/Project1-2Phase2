import engine.bot.CollisionChecker;
import engine.bot.Trial_and_error_Bot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class Trial_and_error_BotTest {

    private Trial_and_error_Bot bot;
    private CollisionChecker collisionChecker;

    @BeforeEach
    void setUp() {
        collisionChecker = Mockito.mock(CollisionChecker.class);
        bot = new Trial_and_error_Bot(collisionChecker);
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

        Mockito.when(collisionChecker.heightChecker(map, x, friction, hole)).thenReturn(heightVec);
        Mockito.when(collisionChecker.collisionVectors(info, x, friction, hole)).thenReturn(collisionVec);

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

        Mockito.when(collisionChecker.heightChecker(map, x, friction, hole)).thenReturn(heightVec);
        Mockito.when(collisionChecker.collisionVectors(info, x, friction, hole)).thenReturn(collisionVec);

        assertNull(bot.comparingVectors(map, info, x, friction, hole));
    }
}
