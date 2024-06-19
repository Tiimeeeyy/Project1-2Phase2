package engine.bot.ml_bot.perceptron;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

public class Trainer implements Perceptron, Serializable {
    private PerceptronParams params;
    private Predictor predictor;

    public Trainer(PerceptronParams params, Predictor predictor) {
        this.params = params;
        this.predictor = predictor;
    }

    @Override
    public RealVector predict(RealVector input) {
        return null; /* Empty class to allow Polymorphism */
    }

    /**
     * Training function to train the Perceptron.
     *
     * @param input        The current state.
     * @param target       The target state.
     * @param learningRate The learning rate of the system.
     */
    @Override
    public void train(RealVector input, RealVector target, double learningRate) {
        RealVector output = predictor.predict(input);
        RealVector error = target.subtract(output);

        //UpdateParams.update(params, input, error, learningRate);
    }
}
