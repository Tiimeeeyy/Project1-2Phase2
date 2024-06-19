package engine.bot.ml_bot.math.activation_functions;

public class PureLinearActivationFunction implements ActivationFunction {

	public double activation(double parameter) {
		return parameter;
	}

	public double deriv(double param) {
		return 1;
	}
}