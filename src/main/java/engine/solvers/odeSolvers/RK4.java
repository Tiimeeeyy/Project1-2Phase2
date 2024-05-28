package engine.solvers.odeSolvers;

import engine.solvers.odeFunctions.FunctionInterface;

/**
 * The RK4 class implements the SolverInterface.
 * It is used to solve ordinary differential equations (ODEs) Using the 4th order Runge Kutta method (RK4)
 */
public class RK4 implements SolverInterface{

    /**
     * This method calculates the next step of the ODE solution using the RK4 method.
     * @param f indictate which dynamic function going to be solved, in this game, it is the golf pyhsics
     * @param x the states of the golf ball for current moment. x={coordinate_x,coordinate_y,velocity_x,velocity_y}
     * @param a The friction coefficients. a={knetic friction, static friction}
     * @param dh the height gradient at current coordinate dh={slope_x,slope_y}
     * @param dt The time step
     * @return A boolean indicating whether the system has reached equilibrium.
     */
    public boolean nextstep(FunctionInterface f, double[] x,double[] a, double[] dh,double dt){
        boolean equillium = true;
        
        double[] gx1=new double[x.length];
        double[] gx2=new double[x.length];
        double[] gx3=new double[x.length];
        double[] gx4=new double[x.length];
        double[] xTilda=new double[x.length];

        // Calculate the slopes used in the Rk4 method.
        gx1=f.ode(x, a,dh);
        for (int j = 0; j < x.length; j++) {
            xTilda[j]=x[j]+gx1[j]*dt/2;
        }
        gx2=f.ode(xTilda, a,dh);
        for (int j = 0; j < x.length; j++) {
            xTilda[j]=x[j]+gx2[j]*dt/2;
        }
        gx3=f.ode(xTilda, a,dh);
        for (int j = 0; j < x.length; j++) {
            xTilda[j]=x[j]+gx3[j]*dt;
        }
        gx4=f.ode(xTilda, a,dh);

        // Update the state of the system
        for (int j = 0; j < x.length; j++) {
            x[j]=x[j]+dt*(gx1[j]+gx2[j]*2+gx3[j]*2+gx4[j])/6;
        }

        // Check if the system has reached equilibrium.
        for (double d : gx1) {
            equillium=equillium && (d==0);
        }
        return equillium;

        
        
    }
}
