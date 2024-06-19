package engine.bot.ml_bot.perceptron;

import engine.bot.ml_bot.math.activation_functions.ActivationFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * Predictor class implements Perceptron.
 * This class is used to predict using the perceptron.
 */
public class Predictor implements Perceptron, Serializable {

    private PerceptronParams params;

    /**
     * Class constructor.
     *
     * @param params The parameters for the perceptron.
     */
    public Predictor(PerceptronParams params) {
        this.params = params;
    }

    /**
     * Predicts an output (in our case, a hit to the golf ball) based on the Input given.
     *
     * @param input The current state of the Game (Ball position)
     * @return An Array that contains the prediction.
     */
    public RealVector predict(RealVector input) {
        double[] weights = params.getWeights();
        ActivationFunction function = params.getFunction();
        double bias = params.getBias();
        if (weights.length == 2 || input.getDimension() == 2) { // Handling the input layer.
            double veloWeight = weights[params.getSize() - 1];
            RealVector directionWeights = new ArrayRealVector(weights, 0, params.getSize());
            double dot = directionWeights.dotProduct(input);
            double velo = function.activation(input.getEntry(0) * veloWeight + bias);


            return new ArrayRealVector(new double[]{function.activation(dot + bias), function.activation(dot + bias), velo});
        } else { // Handling consecutive Layers.
            RealVector directionWeights = new ArrayRealVector(weights);
            double veloWeight = weights[1]; // Random number in the Weights array
            double dot = directionWeights.dotProduct(input);
            return new ArrayRealVector(new double[]{function.activation(dot + bias * 0.001), function.activation(dot + bias * 0.0012), function.activation(veloWeight + bias * 0.324)});
        }
    }

    @Override
    public void train(RealVector input, RealVector target, double learningRate) { /* Empty method to allow polymorphism */ }

    public PerceptronParams getParams() {
        return params;
    }
}
