package engine.bot.rule_based;

import engine.solvers.GolfGame;

import java.util.ArrayList;

public class RuleBasedPlayer {
    private final GolfGame golfGame;
    private final ComparingAndScoring comparingAndScoring;
    private double[][][] map;
    private double[][] info;
    private double[] friction;
    private double[] hole;

    public RuleBasedPlayer(ComparingAndScoring comparingAndScoring, GolfGame golfGame) {
        this.comparingAndScoring = comparingAndScoring;
        this.golfGame = golfGame;
        this.map = map;
        this.info = info;
        this.friction = friction;
        this.hole = hole;
    }

    public ArrayList<double[]> scoreGoal(double[] currentPos, Boolean recording) {
        ArrayList<double[]> velocities = new ArrayList<>();
        if (comparingAndScoring.checkHole(currentPos, hole)) {

        }
        double[] bestVector = comparingAndScoring.comparingVectors(map, info, currentPos, friction, hole);

        velocities.add(bestVector);

        ArrayList<double[]> trajectory = golfGame.shoot(currentPos, recording);

        currentPos = trajectory.get(trajectory.size() - 1);

        velocities.addAll(scoreGoal(currentPos, recording));

        return velocities;


    }

    public double[] lastShot(double[] x, double[] hole) {
        double distance = Math.sqrt(Math.pow(hole[0] - x[0], 2) + Math.pow(hole[1] - x[1], 2));

        double[] direction = {hole[0] - x[0], hole[1] - x[1]};

        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);

        direction[0] /= magnitude;
        direction[1] /= magnitude;

        return new double[]{distance * direction[0], distance * direction[1]};
    }
}
