package engine.solvers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class test {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a function: ");
        String func = scanner.nextLine();
        System.out.println("Your function: "+func);
        
        System.out.print("  For x: ");
        double xPosition = scanner.nextDouble();
        System.out.print("  For y: ");
        double yPosition = scanner.nextDouble();
        System.out.println("\nVelocity: ");
        System.out.print("  Velocity for x: ");
        double xVelocity = scanner.nextDouble();
        System.out.print("  Velocity for y: ");
        double yVelocity = scanner.nextDouble();
        System.out.println("\nFriction: ");
        System.out.print("  Kinetic friction for the grass: ");
        double kineticGrass = scanner.nextDouble();
        System.out.print("  Static friction for the grass: ");
        double staticGrass = scanner.nextDouble();
        System.out.print("  Kinetic friction for the sand: ");
        double kineticSand = scanner.nextDouble();
        System.out.print("  Static friction for the sand: ");
        double staticSand = scanner.nextDouble();
        System.out.print("\nTimestep: ");
        double dt = scanner.nextDouble();
        System.out.println("\nPosition for the hole: ");
        System.out.print("  For x: ");
        double xPositionHole = scanner.nextDouble();
        System.out.print("  For y: ");
        double yPositionHole = scanner.nextDouble();
        System.out.print("\nRadiuss of the hole: ");
        double r = scanner.nextDouble();
        scanner.close();

        // manually inputed
        double[] x={xPosition,yPosition,xVelocity,yVelocity};
        double[] frictGrass={kineticGrass,staticGrass};             // a[0] is kenitic friction. a[1] is Static friction
        double[] frictSand={kineticSand,staticSand};             // a[0] is kenitic friction. a[1] is Static friction
        double[] hole={xPositionHole,yPositionHole};
        System.out.println();

        // pre-defined
        // double[] x={17.0,46.0,4.5,-3.5};
        // double[] a={0.06,0.10};             // a[0] is kenitic friction. a[1] is Static friction
        // double dt=0.05;
        // double[] hole={33.5,9.97};
        // double r=0.15;

        // Print the array contents
        System.out.println("Initial position and velocity: " + Arrays.toString(x));
        System.out.println("Friction coefficients for grass: " + Arrays.toString(frictGrass));
        System.out.println("Friction coefficients for sand: " + Arrays.toString(frictSand));
        System.out.println("Position of the hole: " + Arrays.toString(hole));

        golfgame g=new golfgame();
        g.createMap("target/classes/createdmap.png");
        ArrayList<double[]> xpath=g.shoot(new RK4(), x, frictGrass, dt, hole,r,"target/classes/createdmap.png");
        System.out.println(Arrays.toString(xpath.get(0)));
        g.plotTrajectory("target/classes/createdmap.png", "output/outplot.png", xpath);
        System.out.println(xpath.getLast()[0]+" "+xpath.getLast()[1]);
        System.out.println(g.getMinDistance());
        
       
    }



    
}

