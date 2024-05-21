package engine.bot.rule_based;

import engine.solvers.GolfGame;
import javafx.css.Rule;

import java.util.ArrayList;

public class DistanceMeasure {
    private RungeKutta4Void rk4;
    private PredictVelocity predictVelocity;
    private CheckCollisionAndHeight checkCollisionAndHeight;
    private GolfGame golfGame;
    private ComparingAndScoring comparingAndScoring;
    private RuleBasedPlayer ruleBasedPlayer;
    public DistanceMeasure() {
        this.golfGame = new GolfGame();
        this.rk4 = new RungeKutta4Void();
        this.predictVelocity = new PredictVelocity(golfGame);

        this.checkCollisionAndHeight = new CheckCollisionAndHeight(rk4, predictVelocity);

    }

    public ArrayList<double[]> bestDistance(double[] x, double[] hole) {
        double direction[] = checkCollisionAndHeight.calculateDirection(x, hole);
        double[][] velocities = checkCollisionAndHeight.createVelocityVectors(direction);

        ArrayList<double[]> trajectories = new ArrayList<>();
        double maxDistance = 0;

        ArrayList<double[]> farthestTrajectory = null;

        for (double[] velocityVector : velocities) {
            double[] point = x.clone();
            point[2] = velocityVector[0];
            point[3] = velocityVector[1];

            ArrayList<double[]> trajectory = golfGame.shoot(x,true);

            double distance = golfGame.getDistance(x, trajectory.getLast());

            if (distance > maxDistance) {
                maxDistance = distance;
                farthestTrajectory = trajectory;
            }
        }
        return farthestTrajectory;
    }
    public ArrayList<ArrayList<double[]>> recursiveDistances(double[] x, double [] hole) {
        ArrayList<ArrayList<double[]>> allTrajectories = new ArrayList<>();
        ArrayList<double[]> farthestTrajectory = bestDistance(x, hole);

        allTrajectories.add(farthestTrajectory);

        double[] finalPosition = farthestTrajectory.getLast();

        if (!comparingAndScoring.checkHole(finalPosition, hole)) {
            allTrajectories.addAll(recursiveDistances(finalPosition, hole));
        } else {
            double[] lastVelocity = ruleBasedPlayer.lastShot(x, hole);
            ArrayList<double[]> finalTrajectory = golfGame.shoot(new double[] {finalPosition[0], finalPosition[1], lastVelocity[0], lastVelocity[1]}, true);
            allTrajectories.add(finalTrajectory);
        }
        return allTrajectories;
    }
}
