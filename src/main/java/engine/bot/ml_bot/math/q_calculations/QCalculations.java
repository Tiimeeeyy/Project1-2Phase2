package engine.bot.ml_bot.math.q_calculations;

import engine.bot.ml_bot.agent.Action;
import engine.bot.ml_bot.agent.State;
import engine.bot.ml_bot.network.NeuralNetwork;
import org.apache.commons.math3.linear.RealVector;

public class QCalculations {
    private NeuralNetwork qNetwork;
    private double discountFactor;

    public QCalculations(NeuralNetwork qNetwork, double discountFactor) {
        this.qNetwork = qNetwork;
        this.discountFactor = discountFactor;
    }

    /**
     * Calculates the Q value based on the parameters:
     * @param state The current state.
     * @param action The action taken.
     * @param reward The reward for taking that action.
     * @param nextState The state after taking that action.
     * @return The Q Value for that Action.
     */
    public double calculateQValue(State state, Action action, double reward, State nextState, NeuralNetwork targetNet) {
        double nextQValue = qNetwork.predictMaxQValue(nextState.getCurrentPosition());
        return reward + discountFactor * nextQValue;
    }
}
