package engine.bot.ml_bot.math.activation_functions;

/**
 * The interface for the activation functions.
 */
public interface ActivationFunction {
    /**
     * The activation function at position x.
     * @param parameter The parameter inputted (in this case position x)
     * @return The output of the activation function.
     */
    double activation(double parameter);

    /**
     * The derivative of the activation function at position x.
     * @param paramter The parameter inputted (in this case position x)
     * @return The derivative of the activation function at point x.
     */
    double deriv(double paramter);
    
}
