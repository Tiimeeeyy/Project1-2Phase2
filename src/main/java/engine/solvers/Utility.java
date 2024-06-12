package engine.solvers;;

public class Utility {
    public static double ratio=10.0;

    public static int heightToColor(double x){
        return (int) (x*40.0+125); 
    }
    public static double colorToHeight(int x){
        return (double) ((x-125)/40.0); 
    }
    public static int[] coordinateToPixel(double[] x){
        int[] y=new int[2];
        y[0]=(int) Math.floor(x[0]*ratio+250);
        y[1]=(int) Math.floor(-x[1]*ratio+250);
        return y;
    }public static double[] pixelToCoordinate(int[] x){
        double[] y=new double[2];
        y[0]=(double) (x[0]-250)/ratio;
        y[1]=(double) (-x[1]+250)/ratio;
        return y;
    }
    public static int coordinateToPixel_X(double x){
        return (int) Math.floor(x*ratio+250);
    }
    public static int coordinateToPixel_Y(double x){
        return (int) Math.floor(-x*ratio+250);
    }
    public static double pixelToCoordinate_X(int x){
        return (double) (x-250)/ratio;
    }
    public static double pixelToCoordinate_Y(int x){
        return (double) (-x+250)/ratio;
    }
    public static double getPowerFromVelocity(double[] x){
        if(x.length==2){
            return Math.sqrt(Math.pow(x[0], 2)+Math.pow(x[1], 2));
        }
        if (x.length==4) {
            return Math.sqrt(Math.pow(x[2], 2)+Math.pow(x[3], 2));
        }
        return -1;
    }
    public static double[] getDirectionFromVelocity(double[] x){
        double[] normalized={x[0]/getPowerFromVelocity(x),x[1]/getPowerFromVelocity(x)};
        return normalized;
    }

    
}
