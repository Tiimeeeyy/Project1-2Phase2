package engine.bot.ml_bot.perceptron;

import org.apache.commons.math3.linear.RealVector;

/**
 * This interface represents a perceptron
 */
public interface Perceptron {

    double predict(RealVector input);
}
