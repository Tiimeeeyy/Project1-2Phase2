package engine.bot.ml_bot.agent;

import engine.solvers.GolfGameEngine;
import org.apache.commons.math3.linear.RealVector;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class Calculates the reward for an action.
 */
public class Reward {
    // TODO: Shot counter?

    public static final double CLOSENESS_REWARD = 0.1;
    private static final double WATER_PENALTY = -1;
    private static final double GOAL_REWARD = 1;
    private static final double DISTANCE_REWARD = 0.01;
    private static final Logger logger = Logger.getLogger(Reward.class.getName());
    private final GolfGameEngine golfGameEngine;
    //private final MapHandler mapHandler;
    //private static final double SAND_PENALTY = -0.01;

    public Reward(GolfGameEngine golfGameEngine) {
        this.golfGameEngine = golfGameEngine;
    }

    /**
     * Calculates reward for the Agent based on the following inputs:
     *
     * @param resultingState The state that results from the action.
     * @param initialState   The initial state.
     * @param holePosition   The position of the hole.
     * @return The calculated reward for that action.
     */
    public double calculateReward(State resultingState, State initialState, RealVector holePosition) {
        RealVector resultState = resultingState.getCurrentPosition();
        RealVector initState = initialState.getCurrentPosition();
        double reward = 0;

        switch (golfGameEngine.getStatus()) {
            case HitWater, OutOfBoundary -> reward -= WATER_PENALTY;
            case Goal -> reward += GOAL_REWARD;
            default -> reward = 0;
        }
        // Reward distance between the initial point and the resulting point.
        double distanceInitialState = resultState.getDistance(initState);
        reward += distanceInitialState * DISTANCE_REWARD;

        double distanceHole = resultState.getDistance(holePosition);
        // TODO: Look how this behaves
        reward += distanceHole * CLOSENESS_REWARD;

        return reward;
    }

}
