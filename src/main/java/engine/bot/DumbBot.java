package engine.bot;

public class DumbBot implements BotInterface{
    // Here we slightly modify these methods for a "trial and error" approach to the problem

    @Override
    public double getTotalRequiredVelocity(double[] a, double[] b, double[] frictions) {
        return 0;
    }

    @Override
    public double handleVelocity(double[] a, double[] b, double[] frictions, double maxVelocity) {
        return 0;
    }
}
