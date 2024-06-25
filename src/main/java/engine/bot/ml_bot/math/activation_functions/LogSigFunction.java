package engine.bot.ml_bot.math.activation_functions;

/**
 * Implementation of the Log Sig activation function.
 */
public class LogSigFunction implements ActivationFunction {
    public double activation(double parameter) {

        return 1.0 / (1.0 + Math.pow(Math.E, (-1.0 * parameter)));

    }

    public double deriv(double parameter) {
        double e = 1.0 / (1.0 + Math.pow(Math.E, (-1.0 * parameter)));
        return e * (1.0 - e);
    }
}