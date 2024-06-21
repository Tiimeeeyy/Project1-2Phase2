package engine.bot.ml_bot.perceptron;

import org.apache.commons.math3.linear.RealVector;


public interface Perceptron {

    double predict(RealVector input);
}
