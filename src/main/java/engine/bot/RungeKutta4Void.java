package engine.bot;

import engine.solvers.MyFunction;
import engine.solvers.MySolver;

import java.util.logging.Logger;

/**
 * The type Rk 4.
 */
// I changed this method to avoid having to assign a Variable when using it in code.
public class RungeKutta4Void{
    private static final Logger LOGGER = Logger.getLogger(RungeKutta4Void.class.getName());

    public void nextstep(MyFunction f, double[] x, double[] a, double[] dh, double dt) {
        final int length = x.length;
        double[] gx1 = new double[length];
        double[] gx2 = new double[length];
        double[] gx3 = new double[length];
        double[] gx4 = new double[length];
        double[] xTilda = new double[x.length];

        gx1 = f.ode(x, a, dh);
        for (int j = 0; j < x.length; j++) {
            xTilda[j] = x[j] + gx1[j] * dt / 2;
        }
        gx2 = f.ode(xTilda, a, dh);
        for (int j = 0; j < x.length; j++) {
            xTilda[j] = x[j] + gx2[j] * dt / 2;
        }
        gx3 = f.ode(xTilda, a, dh);
        for (int j = 0; j < x.length; j++) {
            xTilda[j] = x[j] + gx3[j] * dt;
        }
        gx4 = f.ode(xTilda, a, dh);

        for (int j = 0; j < x.length; j++) {
            x[j] = x[j] + dt * (gx1[j] + gx2[j] * 2 + gx3[j] * 2 + gx4[j]) / 6;
        }
        boolean equilibrium = true;
        for (double d : gx1) {
            equilibrium = equilibrium && (d == 0);
        }

        if (equilibrium) {
            LOGGER.info("System has reached equilibrium");
        }


    }
}
