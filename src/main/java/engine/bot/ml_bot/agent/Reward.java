package engine.bot.ml_bot.agent;

import org.apache.commons.math3.linear.RealVector;

public class Reward {
    // TODO: Shot counter?

    private static final double WATER_PENALTY = -1000;
    private static final double GOAL_REWARD = 1000;
    private static final double TREE_REWARD = 0;
    private static final double SAND_PENALTY = -0.01;
    public static final double CLOSENESS_REWARD = 0.1;
    private static final double DISTANCE_REWARD = 0.001;

    public double calculateReward(Action action) {
        RealVector actionTaken = action.getAction();
        double reward = 0;
        /*
        if (in water()) {
        water penalty
        etc..

        get distances


         */

        return reward;
    }

}
