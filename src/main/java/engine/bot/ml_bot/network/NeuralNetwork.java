package engine.bot.ml_bot.network;


import engine.bot.ml_bot.math.activation_functions.ActivationFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import engine.bot.ml_bot.perceptron.Perceptron;
import engine.bot.ml_bot.perceptron.Predictor;
import engine.bot.ml_bot.perceptron.UpdateParams;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeuralNetwork implements Serializable {

    private static final int INPUT_LAYER_SIZE = 2;
    private static final int OUTPUT_LAYER_SIZE = 3;
    private static final Logger LOGGER = Logger.getLogger(NeuralNetwork.class.getName());
    private final int[] layerSizes;
    private transient List<Layer> layers;
    private transient ActivationFunction[] activationFunctions;

    public NeuralNetwork(int hiddenLayerSize, ActivationFunction activationFunction) {

        this.layerSizes = new int[]{INPUT_LAYER_SIZE, hiddenLayerSize, OUTPUT_LAYER_SIZE};
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
    public RealVector predict(RealVector input) {
        RealVector output = input;
        for (Layer layer : layers) {
            output = layer.predict(output);
        }
        return output;
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
    public double calculateNodeError(List<RealVector> layerOutputs, int nodeIndex, RealVector nextLayerErrors, List<Perceptron> prevLayerPerceptron, ActivationFunction activationFunction) {
        if (nodeIndex >= layerOutputs.size()) {
            throw new IllegalArgumentException("Node index is bigger than layerOutputs: " + nodeIndex + " " + layerOutputs.size());
        }
        if (nextLayerErrors.getDimension() > prevLayerPerceptron.size()) {
            throw new IllegalArgumentException("Next layers error vector dimension is greater than previous layer!" + nextLayerErrors.getDimension() + " " + prevLayerPerceptron.size());
        }

        double error = 0;

        for (int i = 0; i < nextLayerErrors.getDimension(); i++) {

            RealVector perceptronWeights = new ArrayRealVector(((Predictor) prevLayerPerceptron.get(i)).getParams().getWeights());
            double weight = perceptronWeights.getEntry(nodeIndex);
            error += weight * nextLayerErrors.getEntry(i);
        }

        double temp = layerOutputs.getFirst().getEntry(nodeIndex);
        double activated = activationFunction.deriv(temp);
        return error * activated;

    }

    public void train(RealVector input, RealVector target, double learningRate) {
        List<RealVector> layerOutputs = new ArrayList<>();
        RealVector output = input;

        for (Layer layer : layers) {
            output = layer.predict(output);
            layerOutputs.add(output);
        }

        RealVector outputError = target.subtract(output);
        backpropagate(layerOutputs, outputError, learningRate);
    }

    public void backpropagate(List<RealVector> layerOutputs, RealVector outputError, double learningRate) {
        List<RealVector> layerErrors = new ArrayList<>();
        layerErrors.add(outputError);

        for (int i = layers.size() - 2; i >= 0; i--) {
            Layer currentLayer = layers.get(i + 1);
            RealVector currentLayerError = new ArrayRealVector(currentLayer.getPerceptrons().size());
            for (int j = 0; j < currentLayer.getPerceptrons().size(); j++) {
                double error = calculateNodeError(layerOutputs, j, layerErrors.getFirst(), currentLayer.getPerceptrons(), activationFunctions[i]);
                currentLayerError.setEntry(j, error);
            }
            layerErrors.addFirst(currentLayerError);
        }
        for (int i = 0; i < layers.size(); i++) {
            Layer currentLayer = layers.get(i);
            for (int j = 0; j < currentLayer.getPerceptrons().size(); j++) {
                Perceptron perceptron = currentLayer.getPerceptrons().get(j);
                RealVector perceptronError = new ArrayRealVector(new double[]{layerErrors.get(i).getEntry(j)});
                UpdateParams.update(((Predictor) perceptron).getParams(), layerOutputs.get(i), perceptronError, learningRate);
            }
        }
        LOGGER.log(Level.INFO, "Backpropagation finished.");
    }

    public double predictQValue(RealVector state, RealVector action) {
        RealVector stateAction = new ArrayRealVector(state.getDimension());
        stateAction.setSubVector(0, state);
        stateAction.setSubVector(state.getDimension(), action);

        RealVector output = predict(stateAction);

        return output.getEntry(0);
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


