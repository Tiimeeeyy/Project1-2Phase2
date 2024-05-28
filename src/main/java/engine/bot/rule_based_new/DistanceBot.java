package engine.bot.rule_based_new;

/**
 * Interface for the distance bot. This bot calculates the best distance based on the distance to the hole after the shot
 */
public interface DistanceBot {

    public void playGame(double[] hole, double[] position, boolean reachedHole);
}
