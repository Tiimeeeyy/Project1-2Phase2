package engine.bot;
import engine.solvers.golfgame;

public class RewardBasedBot {

    private final CheckCollisionAndHeight checkCollisionAndHeight;

    private final ComparingAndScoring comparingAndScoring;

    private final golfgame golfgame;

    public RewardBasedBot(CheckCollisionAndHeight checkCollisionAndHeight, ComparingAndScoring comparingAndScoring, engine.solvers.golfgame golfgame) {
        this.checkCollisionAndHeight = checkCollisionAndHeight;
        this.comparingAndScoring = comparingAndScoring;
        this.golfgame = golfgame;
    }

    public void makeDescisions(double[][][] map, double[][] info, double[] x, double[] friction, double[] hole) {
        // Arbitrary amount of plays to avoid infinite shot taking
        int maxPlays = 10;
        if (comparingAndScoring.checkHole(x, hole) || maxPlays == 0) {
        }
    }
}
