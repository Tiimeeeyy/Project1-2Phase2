package engine.bot.ml_bot;

import java.util.Random;

public class NeuralNetwork {
    private final Random random = new Random();
    private final double[][] output;
    private final double[][][] weights;
    private final double[][] bias;

    private final double[][] errorSignal;
    private final double[][] outputDerivative;

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

    public double[] createRandomArray(int size) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextDouble();
        }
        return array;
    }

    public double[] feedForward(double[] input) {
        output[0] = input;
        for (int i = 0; i < output.length; i++) {
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

    public void backPropagation(double[] target) {
        for (int i = 0; i < errorSignal[errorSignal.length - 1].length; i++) {
            errorSignal[errorSignal.length - 1][i] = (output[output.length - 1][i] - target[i]) * outputDerivative[outputDerivative.length - 1][i];
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

    public double[] predict(double[] input) {
        double[] output = feedForward(input);

        double angle = output[0] * 2 *Math.PI;
        double directionX = Math.cos(angle);
        double directionY = Math.sin(angle);

        double velocity = output[1];

        return new double[]{directionX, directionY, velocity};
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
