package engine.bot;

/**
 * The interface Bot interface for the bot classes
   These methods are the ones used by all bot classes, the "Dumb" bot and the "Smart" bot.
 */
public interface BotInterface {

    /**
     * Gets total required velocity for the ball to hit a hole in one
     *
     * @param a         the initial position of the ball
     * @param b         the target position of the ball
     * @param frictions the frictions of the different floors
     * @return the total required velocity for the ball to hit a hole in one
     */
    public double getTotalRequiredVelocity(double[] a, double[] b, double[] frictions);

    public double handleVelocity(double[] a, double[] b, double[] frictions, double maxVelocity);

}
