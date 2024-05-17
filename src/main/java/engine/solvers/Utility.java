package engine.solvers;;

public class Utility {
    public static void coordinateConvertor(double[] x){
        x[0]=x[0]+25;
        x[1]=-x[1]+25;
    }

    public static void reverseCovertor(double[] x){
        x[0]=x[0]-25;
        x[1]=-(x[1]-25);
    }
    
    // public static int heightToColor(double x){
    //     return 
    // }
}
