package engine.bot.ml_bot.test;

import engine.bot.AibotGA.AiBotGAOneShot;
import engine.solvers.GolfGameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AiBotGATest {
    private AiBotGAOneShot aiBotGA;
    private GolfGameEngine mockGameEngine;

    @BeforeEach
    public void setup() {
        mockGameEngine = mock(GolfGameEngine.class);
        aiBotGA = new AiBotGAOneShot(mockGameEngine);
    }

    @Test
    public void golfBot_initializesPopulation() {
        double[] initialState = {0, 0, 0, 0};
        aiBotGA.golfBot(initialState);
        double[] best = aiBotGA.getBest();
        assertNotNull(best);
    }

    @Test
    public void golfBot_reachesGoal() {
        double[] initialState = {0, 0, 0, 0};
        when(mockGameEngine.isGoal()).thenReturn(true);
        aiBotGA.golfBot(initialState);
        double[] best = aiBotGA.getBest();
        assertNotNull(best);
    }

    @Test
    public void golfBot_doesNotReachGoal() {
        double[] initialState = {0, 0, 0, 0};
        when(mockGameEngine.isGoal()).thenReturn(false);
        aiBotGA.golfBot(initialState);
        double[] best = aiBotGA.getBest();
        assertNotNull(best);
    }
}
