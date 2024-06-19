package engine.bot.ml_bot.agent;

import org.apache.commons.math3.linear.RealVector;
import engine.solvers.GolfGameEngine;

public class Reward {
    // TODO: Shot counter?

    private static final double WATER_PENALTY = -1000;
    private static final double GOAL_REWARD = 1000;
    public static final double CLOSENESS_REWARD = 0.1;
    private static final double DISTANCE_REWARD = 0.001;
    private final GolfGameEngine golfGameEngine;
    //private final MapHandler mapHandler;
    //private static final double SAND_PENALTY = -0.01;

    public Reward(GolfGameEngine golfGameEngine) {
        this.golfGameEngine = golfGameEngine;
    }

    public double calculateReward(State resultingState, State initialState, RealVector holePosition) {
        RealVector resultState = resultingState.getCurrentPosition();
        RealVector initState = initialState.getCurrentPosition();
        double reward = 0;

        switch (golfGameEngine.getStatus()) {
            case HitWater, OutOfBoundary -> reward -= WATER_PENALTY;
            case Goal -> reward += GOAL_REWARD;
            default -> reward = 0;
        }
        double distanceInitialState = resultState.getDistance(initState);
        reward += distanceInitialState * DISTANCE_REWARD;

        double distanceHole = resultState.getDistance(holePosition);
        reward += distanceHole * CLOSENESS_REWARD;

        return reward;
    }

}
