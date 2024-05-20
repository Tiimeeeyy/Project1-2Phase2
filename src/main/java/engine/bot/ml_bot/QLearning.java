package engine.bot.ml_bot;

import engine.solvers.GolfGame;
import java.util.Random;

public class QLearning {
    private final NeuralNetwork network;
    private final Random random = new Random();

    public QLearning(int... nodes) {
        network = new NeuralNetwork(nodes);
    }

    public void train(GolfGame game, int numGames) {
        for (int i = 0; i < numGames; i++) {
            playGame(game);
            double performance = evaluatePerformance(game);
            adjustWeightsAndBiases(performance);
        }
    }
    private void playGame(GolfGame game){}

    private double evaluatePerformance (GolfGame game) { return 0.1;}

    private void adjustWeightsAndBiases(double performance){}
}
