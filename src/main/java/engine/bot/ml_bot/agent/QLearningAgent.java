package engine.bot.ml_bot.agent;

import engine.bot.ml_bot.math.q_calculations.QCalculations;
import engine.bot.ml_bot.network.NeuralNetwork;
import engine.bot.ml_bot.network.ReplayBuffer;
import engine.solvers.GolfGameEngine;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QLearningAgent implements Serializable {
    // TODO: Add tests.
    // TODO: Hyperparameter (Report).
    private static final int NUM_ANGLES = 100; // The amount of angles we let the agent explore.
    private static final int NUM_POWERS = 5; // The amount of shot powers the agent can use (In our case 0-5 m/s).
    private static final double EPSILON_DECAY = 0.995;
    private static final double MIN_EPSILON = 0.01;
    private final transient Logger logger = Logger.getLogger(QLearningAgent.class.getName());
    private NeuralNetwork qNetwork;
    private ReplayBuffer replayBuffer;
    private double epsilon;
    private Random random;
    private transient QCalculations qCalculations;
    private transient GolfGameEngine golfGameEngine;
    private State initialState;
    private Reward reward;
    private RealVector holePosition;

    public QLearningAgent(NeuralNetwork qNetwork, ReplayBuffer replayBuffer, double epsilon, QCalculations qCalculations, GolfGameEngine golfGameEngine, State initialState, Reward reward, RealVector holePosition) {

        this.qNetwork = qNetwork;
        this.replayBuffer = replayBuffer;
        this.epsilon = epsilon;
        this.qCalculations = qCalculations;
        this.golfGameEngine = golfGameEngine;
        this.initialState = initialState;
        this.reward = reward;
        this.holePosition = holePosition;
    }

    /**
     * Chooses an Action based on the Epsilon Greedy method.
     *
     * @param state The State the game is in.
     * @return A Vector containing the Action.
     */
    public RealVector chooseAction(State state) {
        int i = 1;
        if (random.nextDouble() < epsilon) {
            if (random.nextDouble() < 0.5) {
                i = -1;
            }
            double randomPower = 1 + random.nextDouble() * 4;
            return new ArrayRealVector(new double[]{random.nextDouble() * i, random.nextDouble() * i, random.nextDouble(), randomPower});
        } else {
            return getBestAction(state);
        }
    }

    /**
     * Gets the best action out of a finite set by calculating the Q value of each action.
     *
     * @param state The current state.
     * @return The best action (as a Vector).
     */
    private RealVector getBestAction(State state) {
        double maxQValue = Double.NEGATIVE_INFINITY;
        RealVector bestAction = null;

        for (int angle = 0; angle < NUM_ANGLES; angle++) {
            for (double power = 0; power <= NUM_POWERS; power += 0.2) {
                double rad = Math.toRadians(angle);
                double xDir = Math.cos(rad);
                double yDir = Math.sin(rad);

                RealVector action = new ArrayRealVector(new double[]{xDir, yDir, power});
                double qValue = qCalculations.calculateQValue(state, new Action(action), 0, new State(calculateNextState(action)));

                if (qValue > maxQValue) {
                    maxQValue = qValue;
                    bestAction = action;
                }
            }

        }
        return bestAction;
    }

    public RealVector calculateNextState(RealVector currentAction) {

        double[] arrayAction = currentAction.toArray();

        ArrayList<double[]> result = golfGameEngine.shoot(arrayAction, false);

        return new ArrayRealVector(result.getLast());
    }

    public void train(int numEpisodes) {
        for (int episode = 0; episode < numEpisodes; episode++) {

            logger.log(Level.INFO, "Episode: {}", episode);

            State state = initialState;

            RealVector action = chooseAction(state);

            State nextState = new State(calculateNextState(action));

            Action action1 = new Action(action);
            double calcReward = reward.calculateReward(nextState, state, holePosition);

            addExperienceToBuffer(state, action1, calcReward, nextState);

            updateQValues();

            state = nextState;

            logger.log(Level.INFO, "Episode {} completed!", episode);
        }
        decayEpsilon();
    }

    /**
     * Updates the Q values for each experience of a random batch, and retrains the model.
     */
    public void updateQValues() {

        List<ReplayBuffer.Experience> batch = ReplayBuffer.sampleBatch(4);
        for (ReplayBuffer.Experience experience : batch) {
            double targetQ = qCalculations.calculateQValue(
                    experience.getState(),
                    experience.getAction(),
                    experience.getReward(),
                    experience.getNextState()
            );
            qNetwork.train(
                    experience.getState().getCurrentPosition().append(experience.getAction().getAction()), new ArrayRealVector(new double[]{targetQ}), 0.01
            );
        }
    }

    public void addExperienceToBuffer(State state, Action action, double reward, State nextState) {
        replayBuffer.addExperience(new ReplayBuffer.Experience(state, action, reward, nextState));
    }

    public void decayEpsilon() {
        epsilon = Math.max(MIN_EPSILON, epsilon * EPSILON_DECAY);
    }

    public void saveModel(NeuralNetwork model, String fileName, String directory) {
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } catch (SecurityException securityException) {
            logger.log(Level.SEVERE, "Permission denied! Unable to create directory.", securityException);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(model);
        } catch (IOException i) {
            logger.log(Level.SEVERE, "An error occurred while saving the file: ", i);
        }

    }

    public NeuralNetwork loadModel(String fileName) {
        NeuralNetwork model = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            model = (NeuralNetwork) in.readObject();
        } catch (IOException i) {
            logger.log(Level.SEVERE, "An error occurred while loading: ", i);
        } catch (ClassNotFoundException c) {
            logger.log(Level.SEVERE, "Neural Network class not found!", c);
        }
        return model;
    }
}