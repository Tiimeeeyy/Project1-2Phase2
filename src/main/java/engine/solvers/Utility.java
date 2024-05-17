package engine.solvers;;

public class Utility {
    
    public static void cornerToCenter(double[] x){
        x[0]=x[0]+25;
        x[1]=-x[1]+25;
    }

    public static void centerToCorner(double[] x){
        x[0]=x[0]-25;
        x[1]=-(x[1]-25);
    }
    
    public static int heightToColor(double x){
        return (int) (x*50+125); 
    }
    public static int[] coordinateToPixel(double[] x){
        int[] y=new int[2];
        y[0]=(int) Math.floor(x[0]*10+250);
        y[1]=(int) Math.floor(-x[1]*10+250);
        return y;
    }
}
