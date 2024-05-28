package engine.bot.rule_based_old;

import engine.solvers.GolfGameEngine;

public class PredictVelocity {
    private final GolfGameEngine golfGame;

    public PredictVelocity(GolfGameEngine golfGame) {
        this.golfGame = golfGame;
    }

    private final double[] hole = getHOLE();

    public double assumeVelocity(double[] x) {
        double distance = golfGame.getDistance(x, hole);
        double velocty = 0;
        if (distance >= 400) {
            return velocty = 5;
        } else if (distance >= 300) {
            return velocty = 4;
        } else if (distance >= 200) {
            return velocty = 3;
        } else if (distance >= 100) {
            return velocty = 2;
        } else if (distance >= 50) {
            return velocty = 1;
        }else
            return 0.5;


    }

    public double[] getHOLE() {
        return hole;
    }
}
