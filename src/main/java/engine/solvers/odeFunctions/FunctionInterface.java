package engine.solvers.odeFunctions;

/**
 * The FunctionInterface is an interface that represents a function used in solving ordinary differential equations (ODEs).
 * It defines a single method, ode, which takes three parameters: an array of x values, an array of a values, and an array of dh values.
 * The method returns an array of double values which represent the result of the function.
 */
public interface FunctionInterface {
    /**
     * This method represents an ordinary differential equation (ODE).
     *
     * @param x  An array of x values.
     * @param a  An array of a values.
     * @param dh An array of dh values.
     * @return An array of double values which represent the result of the function.
     */
    double[] ode(double[] x, double[] a, double[] dh);
}

