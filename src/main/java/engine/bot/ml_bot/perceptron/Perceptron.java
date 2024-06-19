package engine.bot.ml_bot.perceptron;

import org.apache.commons.math3.linear.RealVector;


public interface Perceptron {

    RealVector predict(RealVector input);

    void train(RealVector input, RealVector target, double learningRate);

}
