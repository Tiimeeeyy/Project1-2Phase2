package engine.bot.ml_bot.agent;

import engine.bot.AibotGA.MapSearcher;
import engine.bot.ml_bot.math.activation_functions.LogSigFunction;
import engine.bot.ml_bot.math.activation_functions.ReLUFunction;
import engine.bot.ml_bot.math.q_calculations.QCalculations;
import engine.bot.ml_bot.network.Layer;
import engine.bot.ml_bot.network.NeuralNetwork;
import engine.bot.ml_bot.network.ReplayBuffer;
import engine.bot.ml_bot.perceptron.Perceptron;
import engine.bot.ml_bot.perceptron.Predictor;
import engine.solvers.GolfGameEngine;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.Relationship;

import java.awt.datatransfer.FlavorEvent;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the Q learning agent. From the time the report was written, a few things changed here to fix the issues we were
 * encountering. The changes will be highlighted with a CHANGED annotation in the Javadocs.
 * The class uses 2-different Neural networks to perform so-called Deep Q Learning, where the separate networks are used for different parts
 * of the learning process. One network is used to solve the Bellman equation, and the other one is used to predict an action based on
 * the current Q Values.
 * !The implementation is still very buggy! Please try it on simple problems (relatively close to the hole)
 */
public class QLearningAgent implements Serializable {
    private static final int NUM_ANGLES = 100; // The amount of angles we let the agent explore.
    private static final int NUM_POWERS = 5; // The amount of shot powers the agent can use (In our case 0-5 m/s).
    private static final double EPSILON_DECAY = 0.995;
    private static final double MIN_EPSILON = 0.10;
    private final transient Logger logger = Logger.getLogger(QLearningAgent.class.getName());
    public boolean isTrained = false;
    private NeuralNetwork qNetwork;
    private NeuralNetwork targetNetwork;
    private ReplayBuffer replayBuffer;
    private double epsilon;
    private Random random;
    private transient QCalculations qCalculations;
    private transient GolfGameEngine golfGameEngine;
    private State initialState;
    private Reward reward;
    private transient RealVector holePosition;
    private transient MapSearcher mapSearcher;

    /**
     * Class Constructor.
     * @param epsilon Decaying value used to select either a random action, or a SoftMax selected action.
     * @param golfGameEngine The engine used to simulate the game.
     * @param initialState The initial position / state of the ball.
     * @param holePosition The position of the hole.
     * @param mapSearcher The class performing Breadth-First search.
     */
    public QLearningAgent(double epsilon, GolfGameEngine golfGameEngine, State initialState, RealVector holePosition, MapSearcher mapSearcher) {
        this.mapSearcher = mapSearcher;
        this.qNetwork = new NeuralNetwork(2, new int[]{64, 32, 16, 4, 2}, new ReLUFunction());
        this.targetNetwork = new NeuralNetwork(2,new int[]{64, 32, 16, 4, 2}, new ReLUFunction());
        this.replayBuffer = new ReplayBuffer();
        this.epsilon = epsilon;
        this.qCalculations = new QCalculations(qNetwork, 0.9);
        this.golfGameEngine = golfGameEngine;
        this.initialState = initialState;
        this.reward = new Reward(golfGameEngine);
        this.holePosition = holePosition;
        this.random = new Random();
    }

    /**
     * Chooses an Action based on the Epsilon Greedy method.
     *
     * @param state The State the game is in.
     * @return A Vector containing the Action.
     */
    public RealVector chooseAction(State state) {
        int i = 1;
        double[] coords = state.getCoordinates();
        if (random.nextDouble() < epsilon) {
            logger.log(Level.INFO, "Random shot!");
            if (random.nextDouble() < 0.5) {
                i = -1;
            }
            return new ArrayRealVector(new double[]{coords[0], coords[1], random.nextDouble() * i, random.nextDouble() * i});
        } else {
            logger.log(Level.INFO, "Best shot!");
            return getSoftMaxAction(state);
        }
    }

    /**
     * Gets the best action out of a finite set by calculating the Q value of each action based on the current state.
     *
     * @param state The current state.
     * @return The best action (as a Vector).
     */
    private RealVector getBestAction(State state) {
        double maxQValue = Double.NEGATIVE_INFINITY;
        RealVector bestAction = null;
        double[] coords = state.getCoordinates();
        List<RealVector> actions = new ArrayList<>();
        for (int angle = 0; angle < NUM_ANGLES; angle++) {
            for (int power = 0; power < NUM_POWERS; power++) {


                double rad = Math.toRadians(angle);
                double xDir = Math.cos(rad);
                double yDir = Math.sin(rad);
                double pow = random.nextDouble() * 6;
                RealVector action = new ArrayRealVector(new double[]{coords[0], coords[1], xDir * pow, yDir * pow});
                actions.add(action);

            }

        }
        return actions.parallelStream()
                .max(Comparator.comparingDouble(action -> qCalculations.calculateQValue(
                        state,
                        new Action(action),
                        reward.calculateReward(new State(calculateNextState(action)), state, holePosition),
                        new State(calculateNextState(action)),
                        targetNetwork
                )))
                .orElseThrow(() -> new RuntimeException("No actions available"));
    }

    /**
     * Calculates the next state the system will be in by simulating the game.
     *
     * @param currentAction The action to be simulated.
     * @return The resulting state, as a Vector.
     */
    public RealVector calculateNextState(RealVector currentAction) {

        double[] arrayAction = currentAction.toArray();

        ArrayList<double[]> result = golfGameEngine.shoot(arrayAction, true);
        double[] resultVec = result.getLast();


        return new ArrayRealVector(new double[]{resultVec[0], resultVec[1]});

    }

    /**
     * Trains the model.
     *
     * @param numEpisodes The number of training loops to be done.
     */
    public void train(int numEpisodes) {
        learnFromInstructor(initialState);
        for (int episode = 0; episode <= numEpisodes; episode++) {
//            int episode = 0;
//            while (epsilon > MIN_EPSILON) {
            logger.log(Level.INFO, "Episode: {0}", episode);
            double[] stateArr = initialState.getCoordinates().clone();
            State state = new State(new ArrayRealVector(stateArr));

            RealVector action = chooseAction(state);

            State nextState = new State(calculateNextState(action));

            Action action1 = new Action(action);
            double calcReward = reward.calculateReward(nextState, state, holePosition);

            addExperienceToBuffer(state, action1, calcReward, nextState);

            updateQValues(8);

            decayEpsilon();

            logger.log(Level.INFO, "Episode {0} completed!", episode);
            logger.log(Level.INFO, "Epsilon Value: {0}", epsilon);

            softUpdateTarget(0.9);
        }
        logger.log(Level.INFO, "Training is finished!");
        isTrained = true; // Flag check for the play method.
        saveModel(qNetwork, "Map1Q", "src/main/java/engine/bot/ml_bot/models");

        saveModel(targetNetwork, "Map1Target", "src/main/java/engine/bot/ml_bot/models");
    }

    /**
     * Updates the Q values for each experience of a random batch, and retrains the model.
     *
     * @param batchSize The number of experiences to be pulled.
     */
    public void updateQValues(int batchSize) {

        List<ReplayBuffer.Experience> batch = ReplayBuffer.sampleBatch(batchSize);
        List<Double> qVals = new ArrayList<>();
        logger.log(Level.INFO, "Batch size: {0}", batch.size());
        for (ReplayBuffer.Experience experience : batch) {
            double Qval = qCalculations.calculateQValue(
                    experience.getState(),
                    experience.getAction(),
                    experience.getReward(),
                    experience.getNextState(),
                    targetNetwork
            );
            qVals.add(Qval);
            for (Double qVal : qVals) {
                qNetwork.train(
                        experience.getState().getCurrentPosition(),
                        experience.getAction().getAction(),
                        experience.getNextState().getCurrentPosition(),
                        qVal,
                        0.9,
                        0.1,
                        targetNetwork
                );
            }
        }
    }

    /**
     * Method, that gets one play from the agent.
     *
     * @param currentState The state the system is in currently.
     * @return The vector containing the play the agent believes to be the best.
     */
    public RealVector getOnePlay(State currentState) {
        // Check if the model is trained
        if (!isTrained) {
            throw new IllegalStateException("The Agent is not trained! Please train the agent before using this method!");
        }
        return getBestAction(currentState);
    }

    /**
     * Adds an experience to the Memory buffer.
     *
     * @param state     The state before the action was taken.
     * @param action    The action that was taken.
     * @param reward    The reward for that action.
     * @param nextState The state the system is in after taking the action.
     */
    public void addExperienceToBuffer(State state, Action action, double reward, State nextState) {
        replayBuffer.addExperience(new ReplayBuffer.Experience(state, action, reward, nextState));
    }

    /**
     * Performs epsilon decay on the Epsilon value.
     */
    public void decayEpsilon() {
        epsilon = Math.max(MIN_EPSILON, epsilon * EPSILON_DECAY);
    }

    private List<RealVector> generatePossibleActions(State state) {

        double[] coords = state.getCoordinates();
        List<RealVector> actions = new ArrayList<>();
        for (int angle = 0; angle < NUM_ANGLES; angle++) {
            for (int j = 0; j < NUM_POWERS; j++) {
                double rad = Math.toRadians(angle);
                double xDir = Math.cos(rad);
                double yDir = Math.sin(rad);
                double pow = random.nextDouble() * 5;
                RealVector action = new ArrayRealVector(new double[]{coords[0], coords[1], xDir * pow, yDir * pow});
                actions.add(action);
            }
        }
        return actions;
    }

    /**
     * Uses the BFS algorithm from the MapSearcher.java class to "teach" the perfect path to the Agent.
     * @param state The state the Agent is in currently.
     */
    private void learnFromInstructor(State state) {
        List<RealVector> optimalActions = getOptimalPath(state);
        logger.log(Level.INFO, "Actions size: {0}", optimalActions.size());
        for (RealVector action : optimalActions) {
            State nextState = new State(calculateNextState(action));

            double rewardVal = reward.calculateReward(nextState, state, holePosition);
            addExperienceToBuffer(state, new Action(action), rewardVal, nextState);

            state = nextState;
        }
        List<ReplayBuffer.Experience> experiences = ReplayBuffer.sampleBatch(8);
        List<Double> qVals = new ArrayList<>();
        logger.log(Level.INFO, "Size of experiences {0}", experiences.size());
        for (ReplayBuffer.Experience experience : experiences) {
            double Qval = qCalculations.calculateQValue(
                    experience.getState(),
                    experience.getAction(),
                    experience.getReward(),
                    experience.getNextState(),
                    targetNetwork
            );
            qVals.add(Qval);
            for (Double qVal : qVals) {
                qNetwork.train(
                        experience.getState().getCurrentPosition(),
                        experience.getAction().getAction(),
                        experience.getNextState().getCurrentPosition(),
                        qVal,
                        0.9,
                        0.1,
                        targetNetwork
                );
            }
            softUpdateTarget(0.8);
            logger.log(Level.INFO, "Learned from the master (BFS)");


        }
    }

    /**
     * Gets an action based on the SoftMax Selection process. It uses the softmax function to select
     * an action.
     * @param state The current state the system is in.
     * @return The Action selected by the process.
     */
    private RealVector getSoftMaxAction(State state) {
        List<RealVector> actions = generatePossibleActions(state);
        List<Double> qVals = actions.stream()
                .map(action -> qCalculations.calculateQValue(
                        state,
                        new Action(action),
                        reward.calculateReward(new State(calculateNextState(action)),state, holePosition),
                        new State(calculateNextState(action)),
                        targetNetwork))
                .toList();


        double maxQValue = Collections.max(qVals);
        List<Double> expQVals = qVals.stream()
                .map(qVal -> Math.exp(qVal - maxQValue))
                .toList();

        double sumExpQValues = expQVals.stream().mapToDouble(Double::doubleValue).sum();
        List<Double> probabilities = expQVals.stream()
                .map(expQval -> expQval / sumExpQValues)
                .toList();

        double p = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < actions.size(); i++) {
            cumulativeProbability += probabilities.get(i);
            if (p <= cumulativeProbability) {
                return actions.get(i);
            }
        }

        return actions.getLast();
    }

    /**
     * Gets the optimal path using the MapSearcher.java BFS algorithm. It returns a list containing all
     * the points in the optimal path in an interval of 10.
     * @param state The current state the system is in.
     * @return A list containing the points of the perfect path.
     */
    private List<RealVector> getOptimalPath(State state) {
        ArrayList<double[]> path = mapSearcher.findShortestPath();
        logger.log(Level.INFO, "Path size: {0}", path.size());
        List<double[]> turningPoints = mapSearcher.getTurningPoints(path);
        logger.log(Level.INFO, "turning points: {0}", turningPoints.size());
        List<RealVector> actions = new ArrayList<>();
//        if (!turningPoints.isEmpty()) {
//            for (int i = 0; i < turningPoints.size() - 1; i++) {
//                double[] currentPos = turningPoints.get(i);
//                double[] nextPos = turningPoints.get(i + 1);
//
//                double[] action = new double[]{currentPos[0], currentPos[1], nextPos[0] - currentPos[0], nextPos[1] - currentPos[1]};
//
//                actions.add(new ArrayRealVector(action));
//            }
//        }else {
            for (int i = 0; i < path.size() - 10; i++) {
                double[] currentPos = path.get(i);
                double[] nextPos = path.get(i+10);

                double[] action = new double[]{currentPos[0], currentPos[1], nextPos[0] - currentPos[0], nextPos[1] - currentPos[1]};

                actions.add(new ArrayRealVector(action));
            }

        logger.log(Level.INFO, "Actions size: {0}", actions.size());
        return actions;
    }

    /**
     * "Soft" updates the network with a mix of the two networks' parameters.
     * @param tau The mixing parameter.
     */
    private void softUpdateTarget(double tau) {
        List<Layer> layers = targetNetwork.getLayers();
            for (int i = 0; i < layers.size(); i++) {
                Layer qNetLayer = layers.get(i);
                Layer targetNetLayer = targetNetwork.getLayers().get(i);
                for (int j = 0; j < qNetLayer.getPerceptrons().size(); j++) {
                    Perceptron qnetPerceptron = qNetLayer.getPerceptrons().get(j);
                    Perceptron targetPerceptron = targetNetLayer.getPerceptrons().get(j);
                    double[] qNetWeights = ((Predictor) qnetPerceptron).getParams().getWeights();
                    double[] targetNetWeights = ((Predictor) targetPerceptron).getParams().getWeights();
                    for (int k = 0; k < qNetWeights.length; k++) {
                        targetNetWeights[k] = tau * qNetWeights[k] + (1-tau) * targetNetWeights[k];
                    }
                }
            }
        }
    /**
     * Saves the model.
     *
     * @param model     The model to be saved.
     * @param fileName  The name of the model.
     * @param directory The file directory.
     */
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

    /**
     * Loads the saved model.
     *
     * @param fileName The name of the saved model.
     * @return The saved model.
     */
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
