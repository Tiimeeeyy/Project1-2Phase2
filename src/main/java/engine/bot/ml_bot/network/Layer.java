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
    private List<Perceptron> perceptrons;

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
    public RealVector predict(RealVector input) {
        RealVector outputs = new ArrayRealVector(3);
        for (Perceptron perceptron : perceptrons) {
            RealVector prediction = perceptron.predict(input);
            if (prediction.getDimension() != outputs.getDimension()) {
                throw new IllegalArgumentException("Dimension Mismatch: Prediction size: " + prediction.getDimension() + " output size: " + outputs.getDimension());
            }
            outputs.add(perceptron.predict(input));
        }
        return outputs;
    }


    /**
     * Gets the List of perceptrons
     * @return List of perceptrons.
     */
    public List<Perceptron> getPerceptrons() {
        return perceptrons;
    }
}
