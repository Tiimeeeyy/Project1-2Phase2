package engine.solvers.odeSolvers;

import engine.solvers.odeFunctions.FunctionInterface;

/**
 * Interface for numerical solvers for ODEs.
 */
public interface SolverInterface {
    /**
     * Solve differential equation give x value at the next time step
     *
     * @param f indictate which dynamic function going to be solved, in this game, it is the golf pyhsics
     * @param x the states of the golf ball for current moment. x={coordinate_x,coordinate_y,velocity_x,velocity_y}
     * @param a The friction coefficients. a={knetic friction, static friction}
     * @param dh the height gradient at current coordinate dh={slope_x,slope_y}
     * @param dt The time step
     * @return True if the system has reached equilibrium, false otherwise.
     */
    public boolean nextstep(FunctionInterface f, double[] x,double[] a, double[] dh, double dt);
    
} 