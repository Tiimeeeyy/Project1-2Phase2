package engine.bot;
// TODO: Handle cases for different lengths of certain terrain
// TODO: Get the putts to under 5 m/s per putt
// TODO: Take gravitational force into account


import engine.bot.BotInterface;

public class PuttCalculator implements BotInterface {

    public static final double GRAVITY = 9.81;

    public double handleVelocity(double[] a, double[] b, double[] frictions, double maxVelocity) {
        // Calculate the distance between the two points (a and b)
        final var totalRequVelocity = getTotalRequiredVelocity(a, b, frictions);
        if (totalRequVelocity <= maxVelocity) {
            System.out.println("Required velocity: " + totalRequVelocity);
            return totalRequVelocity;

        } else {
            System.out.println("Not pussible in one putt! Velocity is over " + maxVelocity);
            // Calculate the two steps
            double veloTwoSteps = totalRequVelocity / 2;
            System.out.println("The velocity equates to " + veloTwoSteps + " for each step");
            return veloTwoSteps;
        }

    }

    public double getTotalRequiredVelocity(double[] a, double[] b, double[] frictions) {
        double distance = Math.sqrt(Math.pow(b[0] - a[0], 2)) + Math.pow(b[1] - a[1], 2) + Math.pow(b[2] - a[2], 2);
        // Calculate the height difference from the two points (a and b)
        double heightDifference = b[2] - a[2];
        // Calculate the required velocity:
        double totalRequiredVelocity = 0;
        for (double friction : frictions) {
            // Calculate velocity to cross each surface
            double requiredVelocity = Math.sqrt(2 * distance * friction + 2 * GRAVITY * heightDifference);
            totalRequiredVelocity += requiredVelocity;
        }
        return totalRequiredVelocity;
    }
}
