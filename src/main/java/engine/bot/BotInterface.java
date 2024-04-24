package engine.bot;

/**
 * The interface Bot interface for the bot classes
 * These methods are the ones used by all bot classes, the "Dumb" bot and the "Smart" bot.
 */
public interface BotInterface {

    public double calculateScore(double[] vector, double[][] heightVec, double[][] collisionVec);

    public double[] comparingVectors(double[][][] map, double[][] info, double[] x, double[] friction, double[] hole);

    public boolean checkHole(double[] x, double[] hole);
}
