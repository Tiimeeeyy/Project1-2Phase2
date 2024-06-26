package engine.solvers.odeSolvers;

import engine.solvers.odeFunctions.FunctionInterface;

/**
 * EulerSolver
 */
public class EulerSolver implements SolverInterface {
    /**
     * Function that calculates the next step of the ODE using the Euler method.
     * @param f indictate which dynamic function going to be solved, in this game, it is the golf pyhsics
     * @param x the states of the golf ball for current moment. x={coordinate_x,coordinate_y,velocity_x,velocity_y}
     * @param a The friction coefficients. a={knetic friction, static friction}
     * @param dh the height gradient at current coordinate dh={slope_x,slope_y}
     * @param dt The time step
     * @return True if the system has reached equilibrium, false otherwise.
     */
    public boolean nextstep(FunctionInterface f, double[] x,double[] a, double[] dh, double dt){
        boolean equillium = true;
        //Call ODE
        double[] gx=f.ode(x,a,dh);
        for (int j = 0; j < x.length; j++) {
            x[j]=x[j]+dt*gx[j];
        }
        for (double d : gx) {
            equillium=equillium && (d==0);
        }
        return equillium;
    }
}