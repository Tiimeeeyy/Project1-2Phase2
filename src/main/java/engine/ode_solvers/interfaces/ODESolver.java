package engine.ode_solvers.interfaces;


import java.util.HashMap;
import java.util.ArrayList;

/**
 * Interface for numerical solvers for ODEs.
 */
public interface ODESolver {
    
    /**
     * Solves ODE using the Euler method.
     *
     * @param dim The dimension of the system of ODEs.
     * @param t The total time for the solution.
     * @param h The step size for the Euler method.
     * @param initHM The initial conditions hashmap, with variable names as keys and initial values as values.
     * @param functions The arraylist of functions representing the right-hand side of the ODEs.
     * @param variables The arraylist of variables corresponding to each function in the ODE system.
     * @return A 2D Double array containing the solutions for each variable over time.
     */
    
    Double[][] solverODE(int dim, Double t, Double h, HashMap<String, Double> initHM, ArrayList<String> functions, ArrayList<String> variables);
}
