package engine.bot.ml_bot.test;

import engine.bot.ml_bot.QLearning;
import engine.solvers.GolfGame;
import engine.solvers.odeSolvers.RK4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class QLearningTest {
    private QLearning qLearning;
    private GolfGame golfGame;

    @BeforeEach
    void setUp() {
        // Initialize the QLearning instance with appropriate parameters
        qLearning = new QLearning(new RK4(), new double[]{0, 0}, new double[]{0.1, 0.1}, 0.1, new double[]{0, 0}, false, 10, 10);
        golfGame = new GolfGame(new RK4(), new double[]{0, 0}, 0.1, new double[]{234, 345, 577}, 0.5, "src/main/resources/userInputMap.png");
    }

    @Test
    void shouldNotThrowExceptionWhenTraining() {
        assertDoesNotThrow(() -> qLearning.train(100));
    }

    @Test
    void shouldReturnNonNullHistoryWhenPlayingGame() {
        ArrayList<ArrayList<double[]>> history = qLearning.playGame(golfGame);
        assertNotNull(history);
    }

    @Test
    void shouldReturnNegativePerformance() {
        ArrayList<ArrayList<double[]>> history = qLearning.playGame(golfGame);
        double performance = qLearning.evaluatePerformance(history);
        assertTrue(performance <= 0); // Assuming performance is a negative value representing distance to the hole
    }

    @Test
    void shouldNotThrowExceptionWhenAdjustingWeightsAndBiases() {
        ArrayList<ArrayList<double[]>> history = qLearning.playGame(golfGame);
        double performance = qLearning.evaluatePerformance(history);
        assertDoesNotThrow(() -> qLearning.adjustWeightsAndBiases(history, performance));
    }
}