package solvers;
public class RK4 implements MySolver{
    public boolean nextstep(MyFunction f, double[] x,double[] a, double[] dh,double dt){
        boolean equillium = true;
        
        double[] gx1=new double[x.length];
        double[] gx2=new double[x.length];
        double[] gx3=new double[x.length];
        double[] gx4=new double[x.length];
        double[] xTilda=new double[x.length];

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

        for (int j = 0; j < x.length; j++) {
            x[j]=x[j]+dt*(gx1[j]+gx2[j]*2+gx3[j]*2+gx4[j])/6;
        }
    
        for (double d : gx1) {
            equillium=equillium && (d==0);
        }
        return equillium;

        
        
    }
}
