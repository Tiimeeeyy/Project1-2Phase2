package engine.bot;

public class TestingClass {
    public static void main(String[] args) {
        PuttCalculator puttCalculator = new PuttCalculator();
        double[] a = {0, 0, 0};
        double[] b = {23, 89, 1024};
        double[] frictions = {0.3, 0.4, 0.5};
        double maxVelo = 5;
        double sol = puttCalculator.handleVelocity(a, b, frictions, maxVelo);
        System.out.println("Calculated velo is: " + sol);
    }
}
