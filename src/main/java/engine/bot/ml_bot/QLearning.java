package engine.bot.ml_bot;

import engine.solvers.GolfGame;
import engine.solvers.odeSolvers.MySolver;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;


public class QLearning {
    private final NeuralNetwork network;
    private final Random random = new Random();
    private final GolfGame golfGame;
    private final double[] position;
    private final boolean isGoal;
    private final Logger logger = Logger.getLogger(QLearning.class.getName());

    public QLearning(MySolver mySolver, double[] hole, double[] friction, double radius, double[] position, boolean isGoal, int... nodes) {
        this.position = position;
        this.isGoal = isGoal;
        network = new NeuralNetwork(nodes);
        golfGame = new GolfGame(mySolver, friction, 0.1, hole, radius, "src/main/resources/userInputMap.png");
    }

    public void train(int iterations) {
        for (int i = 0; i < iterations; i++) {

            ArrayList<ArrayList<double[]>> history = playGame(golfGame);

            double performance = evaluatePerformance(history);
            logger.info(MessageFormat.format("Performance at iteration:{0} :{1}", i, performance));


            adjustWeightsAndBiases(history, performance);
        }
    }

    /**
     * Plays the game and stores the trajectories of the "play"
     *
     * @param game The game
     * @return A list containing the trajectories.
     */
    public ArrayList<ArrayList<double[]>> playGame(GolfGame game) {
        System.out.println("playGame called");
        ArrayList<ArrayList<double[]>> history = new ArrayList<>();
        ArrayList<double[]> currentAction = new ArrayList<>();
        int plays = 10;
        while (plays >= 0) {
            double[] action = network.predict(position);

            double directionX = action[0];
            double directionY = action[1];
            double velocity = action[3];

            double[] playArray = new double[]{position[0], position[1], directionX * velocity, directionY * velocity};
            currentAction = game.shoot(playArray, true);

            history.add(currentAction);

            double[] finalPos = currentAction.getLast();
            plays--;
        }
        return history;
    }

    /**
     * This method evaluated the Performance of the individual shots taken.
     *
     * @param history The history of trajectories.
     * @return A metric based on the performance of the network.
     */
    public double evaluatePerformance(ArrayList<ArrayList<double[]>> history) {
        System.out.println("evaluatePerformance is called");
        double performance = 0; // Initial performance
        double[] holePos = golfGame.getHole();
        for (ArrayList<double[]> occurrence : history) {
            double[] result = occurrence.getLast();

            double distance = golfGame.getDistance(result, holePos); // calculates distance from position to hole

            performance -= distance;
        }
        return performance;
    }

    public void adjustWeightsAndBiases(ArrayList<ArrayList<double[]>> history, double performance) {
        System.out.println("Adjust weights and biases called");
        for (ArrayList<double[]> trajectory : history) {
            for (double[] action : trajectory) {

                int outputLayerSize = network.getOutputLayerSize();
                double[] target = new double[outputLayerSize];
                Arrays.fill(target, performance);
                network.backPropagation(target);
            }
        }
    }
}
