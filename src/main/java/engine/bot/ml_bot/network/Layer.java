package engine.bot.ml_bot.network;

import engine.bot.ml_bot.math.activation_functions.ActivationFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import engine.bot.ml_bot.perceptron.Perceptron;
import engine.bot.ml_bot.perceptron.PerceptronParams;
import engine.bot.ml_bot.perceptron.Predictor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Layer implements Serializable {
    private transient List<Perceptron> perceptrons;

    /**
     * Creates a Layer for the Neural Network.
     *
     * @param numPerceptrons     The number of perceptrons in the layer.
     * @param activationFunction The activation function used in the layer.
     */
    public Layer(int numPerceptrons, ActivationFunction activationFunction) {
        perceptrons = new ArrayList<>();
        for (int i = 0; i < numPerceptrons; i++) {

            PerceptronParams params = new PerceptronParams(activationFunction, numPerceptrons);
            perceptrons.add(new Predictor(params));

        }
    }

    /**
     * The predict method for the layer. It takes each perceptron in that layer and predicts its
     * outcome.
     *
     * @param input The input.
     * @return A list of Vectors containing the predicted outputs.
     */
    public List<Double> predict(RealVector input) {
       List<Double> layerPredictions = new ArrayList<>();
       for (Perceptron perceptron : perceptrons) {
           layerPredictions.add(perceptron.predict(input));
       }
       return layerPredictions;
    }


    /**
     * Gets the List of perceptrons
     * @return List of perceptrons.
     */
    public List<Perceptron> getPerceptrons() {
        return perceptrons;
    }
}
