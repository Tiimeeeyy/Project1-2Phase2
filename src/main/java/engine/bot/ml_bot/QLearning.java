package engine.bot.ml_bot;

import engine.solvers.GolfGame;
import engine.solvers.odeSolvers.MySolver;

import java.util.ArrayList;
import java.util.Random;

public class QLearning {
    private final NeuralNetwork network;
    private final Random random = new Random();
    private final GolfGame golfGame;
    private final MySolver mySolver;
    private final double[] hole;
    private final double[] friction;
    private final double radius;
    private final double[] position;
    private final boolean isGoal;

    public QLearning(MySolver mySolver, double[] hole, double[] friction, double[] gradient, double radius, double[] position, boolean isGoal, int... nodes) {
        this.mySolver = mySolver;
        this.hole = hole;
        this.friction = friction;
        this.radius = radius;
        this.position = position;
        this.isGoal = isGoal;
        network = new NeuralNetwork(nodes);
        golfGame = new GolfGame(mySolver, friction, 0.1, hole, radius, "src/main/resources/userInputMap.png");
    }

    public void train(int epochs) {
        for (int i = 0; i < epochs; i++) {
            
            ArrayList<ArrayList<double[]>> history = playGame(golfGame);
            
            double performance = evaluatePerformance(history);
        }
    }

    private ArrayList<ArrayList<double[]>> playGame(GolfGame game) {
        ArrayList<ArrayList<double[]>> history = new ArrayList<>();
        ArrayList<double[]> currentAction = new ArrayList<>();
        while (!isGoal) {
            double[] action = network.predict(position);

            double directionX = action[0];
            double directionY = action[1];
            double velocity = action[3];

            double[] playArray = new double[]{position[0], position[1], directionX*velocity, directionY*velocity};
            currentAction = game.shoot(playArray, true);

            history.add(currentAction);
        }
        return history;
    }


    private double evaluatePerformance(ArrayList<ArrayList<double[]>> history) {
        double performance = 0; // Initial performance
        double[] hole = golfGame.getHole();
        for (ArrayList<double[]> occurence : history) {
            double[] result = occurence.getLast();
            
            double distance = golfGame.getDistance(result, hole);
            
            performance -= distance;
        }
        return performance;
    }

    private void adjustWeightsAndBiases(double performance) {
    }
}
