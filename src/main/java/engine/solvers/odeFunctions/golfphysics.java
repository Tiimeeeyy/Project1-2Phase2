package engine.solvers.odeFunctions;

/**
 * The golfphysics class implements the function interface.
 * It is used to calculate the ODEs for a Golf physics simulation.
 */
public class golfphysics implements FunctionInterface {
    /**
     * This method calculates the ODEs for the simulation.
     * @param x The current state of the system.
     * @param a The friction of the surfaces.
     * @param dh The chang in height.
     * @return The rate of change of the System.
     */
    public double[] ode(double[] x, double[] a, double[] dh){
        double[] dx=new double[x.length];
        double g=9.81;
        
        dx[0]=x[2];
        dx[1]=x[3];

        //precise the speed to 0.01 m/s
        if (Math.abs(x[2])<0.01 && Math.abs(x[3])<0.01) {
            x[2]=0;
            x[3]=0;
        }

        if (x[2]==0 && x[3]==0) {
            if (a[1]>Math.sqrt(Math.pow(dh[0], 2)+Math.pow(dh[1], 2))) {
                dx[2]=0;
                dx[3]=0;
            }else if (dh[0]==0 && dh[1]==0){
                dx[2]=0;
                dx[3]=0;
            }else{
                dx[2]=-g*dh[0]-a[0]*g*dh[0]/(Math.sqrt(Math.pow(dh[0],2)+Math.pow(dh[1], 2)));
                dx[3]=-g*dh[1]-a[0]*g*dh[1]/(Math.sqrt(Math.pow(dh[0],2)+Math.pow(dh[1], 2)));
            }
        }else{
            dx[2]=-g*dh[0]-a[0]*g*x[2]/(Math.sqrt(Math.pow(x[2],2)+Math.pow(x[3], 2)));
            dx[3]=-g*dh[1]-a[0]*g*x[3]/(Math.sqrt(Math.pow(x[2],2)+Math.pow(x[3], 2)));

        }

        

        return dx;
    }
    

    
}