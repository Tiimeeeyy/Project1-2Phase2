package engine.bot.ml_bot.math.activation_functions;

/**
 * Implementation of the pure linear activation function.
 */
public class PureLinearActivationFunction implements ActivationFunction {

	public double activation(double parameter) {
		return parameter;
	}

	public double deriv(double param) {
		return 1;
	}
}