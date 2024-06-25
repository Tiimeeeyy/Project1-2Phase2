package engine.bot.ml_bot.network;


import engine.bot.ml_bot.agent.QLearningAgent;
import engine.bot.ml_bot.agent.State;
import engine.bot.ml_bot.math.activation_functions.ActivationFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import engine.bot.ml_bot.perceptron.Perceptron;
import engine.bot.ml_bot.perceptron.Predictor;
import engine.bot.ml_bot.perceptron.UpdateParams;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NeuralNetwork implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(NeuralNetwork.class.getName());
    private final int[] layerSizes;
    private transient List<Layer> layers;
    private transient ActivationFunction[] activationFunctions;
    private static final double LEARNING_RATE_DECAY = 0.2;

    public NeuralNetwork(int layerSize, ActivationFunction activationFunction) {

        this.layerSizes = new int[]{layerSize, layerSize, 1};
        this.activationFunctions = new ActivationFunction[]{activationFunction, activationFunction, activationFunction};

        layers = new ArrayList<>();
        for (int i = 0; i < layerSizes.length; i++) {
            layers.add(new Layer(layerSizes[i], activationFunctions[i]));
        }

    }

    /**
     * This method predicts an outcome based on the input.
     *
     * @param input The input based on which we predict.
     * @return A vector containing the prediction.
     */
    public double predict(RealVector input) {
        List<List<Double>> networkPredictions = new ArrayList<>();
        for (Layer layer : layers) {
            networkPredictions.add(layer.predict(input));
        }
        return networkPredictions.getLast().getLast();
    }

    /**
     * Calculated the Error of the Node based on the Following parameters.
     *
     * @param layerOutputs        The outputs of all layers.
     * @param nodeIndex           The index of the Node we're accessing.
     * @param nextLayerErrors     The Errors of the next node.
     * @param prevLayerPerceptron The Perceptron of the previous layer.
     * @param activationFunction  The activation function of the System.
     * @return The Node error (double).
     */
    public double calculateNodeError(List<Double> layerOutputs, int nodeIndex, double nextLayerErrors, List<Perceptron> prevLayerPerceptron, ActivationFunction activationFunction) {
        if (nodeIndex >= layerOutputs.size()) {
            throw new IllegalArgumentException("Node index is bigger than layerOutputs: " + nodeIndex + " " + layerOutputs.size());
        }
        double error = 0;

        for (int i = 0; i < prevLayerPerceptron.size(); i++) {
            RealVector perceptronWeights = new ArrayRealVector(((Predictor) prevLayerPerceptron.get(i)).getParams().getWeights());
            double weight = perceptronWeights.getEntry(i);
            error += weight;
        }
        double temp = layerOutputs.get(nodeIndex);
        double activated = activationFunction.deriv(temp);
        return error * activated;
    }

    public void train(RealVector state, RealVector action, RealVector nextState,  double reward, double discountFactor,double learningRate, NeuralNetwork target) {
        LOGGER.log(Level.INFO, "train called!");
        double targetQ = reward;
        if (nextState != null) {
            double maxNextQ = target.predictMaxQValue(nextState, action, target);
            targetQ += discountFactor * maxNextQ;
        }
        double output;
        double error;
        int maxIter = 10000;
        int iter = 0;

        do {
            output = predictQValue(state, action);
            error = targetQ - output;
            backpropagate(state, error, learningRate);
            iter++;
            learningRate *= LEARNING_RATE_DECAY;

            if (iter % 1000 == 0) {
                softUpdateTarget(0.2, target);
            }

        }while(Math.abs(error) > 0.1 && iter < maxIter);
    }

    public double predictMaxQValue(RealVector nextState, RealVector action, NeuralNetwork target) {
        List<RealVector> possibleActions = generatePossibleActions(new State(nextState));

        List<Double> qVals= possibleActions.stream()
                .map(a -> target.predict(nextState))
                .toList();

        return Collections.max(qVals);
    }
    private List<RealVector> generatePossibleActions(State state) {

        double[] coords = state.getCoordinates();
        List<RealVector> actions = new ArrayList<>();
        for (int angle = 0; angle < 100; angle++) {
            for (int j = 0; j < 5; j++) {
                double rad = Math.toRadians(angle);
                double xDir = Math.cos(rad);
                double yDir = Math.sin(rad);
                Random random = new Random();
                double pow = random.nextDouble() * 5;
                RealVector action = new ArrayRealVector(new double[]{coords[0], coords[1], xDir * pow, yDir * pow});
                actions.add(action);
            }
        }
        return actions;
    }

    public void backpropagate(RealVector input, double outputError, double learningRate) {
        List<Double> layerErrors = new ArrayList<>();
        layerErrors.add(outputError);

        for (int i = layers.size() -2; i >= 0; i--) {
            Layer currentLayer = layers.get(i+1);
            if (currentLayer.getPerceptrons().size() == 1) {
                continue;
            }
            double currentLayerError = 0;
            for (int j = 0; j < currentLayer.getPerceptrons().size(); j++) {
                double error = calculateNodeError(layers.get(i).predict(input), j, layerErrors.getFirst(), currentLayer.getPerceptrons(), activationFunctions[1]);
                currentLayerError += error;
            }
            layerErrors.addFirst(currentLayerError);
        }
        for (int i = 0; i < layers.size(); i++) {
            Layer currentLayer = layers.get(i);
            for (int j = 0; j < currentLayer.getPerceptrons().size() - 1; j++) {
                Perceptron perceptron = currentLayer.getPerceptrons().get(j);
                double perceptronError = layerErrors.get(i);

                double regularization = 0.01 * ((Predictor) perceptron).getParams().getWeights()[i];
                UpdateParams.update(((Predictor)perceptron).getParams(), input, perceptronError + regularization, learningRate);
            }
        }
    }

    public double predictQValue(RealVector state, RealVector action) {
        RealVector stateAction = new ArrayRealVector(state.getDimension() + action.getDimension());
        stateAction.setSubVector(0, state);
        stateAction.setSubVector(state.getDimension(), action);
        return predict(stateAction);
    }

    private void softUpdateTarget(double tau, NeuralNetwork target) {
        for (int i = 0; i < layers.size(); i++) {
            Layer qNetLayer = layers.get(i);
            Layer targetNetLayer = target.getLayers().get(i);
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
     * Getters and setters.
     */
    public ActivationFunction[] getActivationFunctions() {
        return activationFunctions;
    }

    public int[] getLayerSizes() {
        return layerSizes;
    }

    public List<Layer> getLayers() {
        return layers;
    }
}


