package solvers;
/**
 * EulerSolver
 */
public class EulerSolver implements MySolver {
    public boolean nextstep(MyFunction f, double[] x,double[] a, double[] dh, double dt){
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