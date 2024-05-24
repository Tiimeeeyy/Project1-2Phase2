package engine.bot.ml_bot;

import java.util.Random;
import java.util.logging.Logger;

public class NeuralNetwork {
    private final Random random = new Random();
    private final double[][] errorSignal;
    private final double[][] outputDerivative;
    private final Logger logger = Logger.getLogger(NeuralNetwork.class.getName());
    private double[][] output;
    private double[][][] weights;
    private double[][] bias;

    /**
     * Class constructor
     *
     * @param nodes amount of nodes in each layer of the network
     */
    public NeuralNetwork(int... nodes) {
        output = new double[nodes.length][];
        weights = new double[nodes.length][][];
        bias = new double[nodes.length][];
        errorSignal = new double[nodes.length][];
        outputDerivative = new double[nodes.length][];

        for (int i = 0; i < nodes.length; i++) {
            output[i] = new double[nodes[i]];
            errorSignal[i] = new double[nodes[i]];
            outputDerivative[i] = new double[nodes[i]];
            bias[i] = new double[nodes[i]];

            if (i > 0) {
                weights[i] = new double[nodes[i]][nodes[i - 1]];
                for (int j = 0; j < nodes[i]; j++) {
                    weights[i][j] = createRandomArray(nodes[i - 1]);

                }
            }

        }
    }

    /**
     * Method used to initialise the weights and biases for the network with random values
     *
     * @param size
     * @return
     */
    public double[] createRandomArray(int size) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextDouble();
        }
        return array;
    }

    /**
     * Propagates the input through the network. Calculates sum of the inputs for each node
     * and applies the sigmoid activation function.
     *
     * @param input The input values.
     * @return The output values after propagation.
     */
    public double[] feedForward(double[] input) {
        System.out.println("Feed forward called");
        output[0] = input;
        for (int i = 1; i < output.length; i++) {
            for (int j = 0; j < output[i].length; j++) {
                double sum = 0;
                for (int k = 0; k < output[i - 1].length; k++) {
                    sum += output[i - 1][k] * weights[i][j][k];
                }
                sum += bias[i][j];
                output[i][j] = sigmoid(sum);
                outputDerivative[i][j] = output[i][j] * (1 - output[i][j]);
            }
        }
        return output[output.length - 1];
    }

    /**
     * Adjusts weights and biases based on the error of each node based on the output
     *
     * @param target the target values.
     */
    public void backPropagation(double[] target) {
        if (weights == null) {
            initialiseWeights();
        }
        System.out.println("Backpropagation called");
        for (int i = 0; i < errorSignal[errorSignal.length - 1].length; i++) {
            errorSignal[errorSignal.length - 1][i] = (output[output.length - 1][i] - target[i]) * outputDerivative[outputDerivative.length - 1][i];
            System.out.println("Error signal for node " + i + ": " + errorSignal[errorSignal.length - 1][i]);
        }

        for (int i = weights.length - 2; i >= 0; i--) {
            for (int j = 0; j < weights[i].length; j++) {
                for (int k = 0; k < weights[i][j].length; k++) {
                    weights[i][j][k] -= errorSignal[i][j] * output[i - 1][k];
                }
                bias[i][j] -= errorSignal[i][j];
            }

        }
    }

    /**
     * Makes a prediction based on the input and feeds the input through the network.
     *
     * @param input The input values.
     * @return The predicted values.
     */
    public double[] predict(double[] input) {
        System.out.println("Predict called");
        double[] output = feedForward(input);

        double angle = output[0] * 2 * Math.PI;
        double directionX = Math.cos(angle);
        double directionY = Math.sin(angle);

        double velocity = output[1];

        double velocityX = velocity * directionX;
        double velocityY = velocity * directionY;

        return new double[]{directionX, directionY, velocityX, velocityY};
    }

    /**
     * Helper function to apply the Sigmoid activation function.
     *
     * @param x The input value.
     * @return The output value after applying the sigmoid function.
     */
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public int getOutputLayerSize() {
        return output[output.length - 1].length;
    }

    private void initialiseWeights() {
        if (weights == null) {
            weights = new double[output.length][][];
        }
        for (int i = 0; i < output.length; i++) {
            if (weights[i] == null && output[i - 1] != null) {
                    weights[i] = new double[output[i].length][output[i - 1].length];
                    for (int j = 0; j < output[i].length; j++) {
                        weights[i][j] = createRandomArray(output[i - 1].length);
                    }
                }else {
                System.out.println("Output is null!");
            }


        }
    }
}
