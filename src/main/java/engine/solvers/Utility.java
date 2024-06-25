package engine.solvers;

public class Utility {
    public static double ratio = 10.0;

    /**
     * Converts a height value to a color value.
     *
     * @param x the height value
     * @return the corresponding color value
     */
    public static int heightToColor(double x) {
        return (int) (x * 40.0 + 125);
    }

    /**
     * Converts a color value to a height value.
     *
     * @param x the color value
     * @return the corresponding height value
     */
    public static double colorToHeight(int x) {
        return (double) ((x - 125) / 40.0);
    }

    /**
     * Converts a coordinate to a pixel value.
     *
     * @param x the coordinate
     * @return the corresponding pixel value
     */
    public static int[] coordinateToPixel(double[] x) {
        int[] y = new int[2];
        y[0] = (int) Math.floor(x[0] * ratio + 250);
        y[1] = (int) Math.floor(-x[1] * ratio + 250);
        return y;
    }

    /**
     * Converts a pixel value to a coordinate.
     *
     * @param x the pixel value
     * @return the corresponding coordinate
     */
    public static double[] pixelToCoordinate(int[] x) {
        double[] y = new double[2];
        y[0] = (double) (x[0] - 250) / ratio;
        y[1] = (double) (-x[1] + 250) / ratio;
        return y;
    }

    /**
     * Converts a coordinate to a pixel x value.
     *
     * @param x the coordinate
     * @return the corresponding pixel x value
     */
    public static int coordinateToPixel_X(double x) {
        return (int) Math.floor(x * ratio + 250);
    }

    /**
     * Converts a coordinate to a pixel y value.
     *
     * @param x the coordinate
     * @return the corresponding pixel y value
     */
    public static int coordinateToPixel_Y(double x) {
        return (int) Math.floor(-x * ratio + 250);
    }

    /**
     * Converts a pixel x value to a coordinate.
     *
     * @param x the pixel x value
     * @return the corresponding coordinate
     */
    public static double pixelToCoordinate_X(int x) {
        return (double) (x - 250) / ratio;
    }

    /**
     * Converts a pixel y value to a coordinate.
     *
     * @param x the pixel y value
     * @return the corresponding coordinate
     */
    public static double pixelToCoordinate_Y(int x) {
        return (double) (-x + 250) / ratio;
    }

    /**
     * Calculates the power from a velocity vector.
     *
     * @param x the velocity vector
     * @return the power
     */
    public static double getPowerFromVelocity(double[] x) {
        if (x.length == 2) {
            return Math.sqrt(Math.pow(x[0], 2) + Math.pow(x[1], 2));
        }
        if (x.length == 4) {
            return Math.sqrt(Math.pow(x[2], 2) + Math.pow(x[3], 2));
        }
        return -1;
    }

    /**
     * Normalizes a velocity vector to get the direction.
     *
     * @param x the velocity vector
     * @return the normalized direction vector
     */
    public static double[] getDirectionFromVelocity(double[] x) {
        double power = getPowerFromVelocity(x);
        return new double[]{x[0] / power, x[1] / power};
    }

    /**
     * Calculates the distance between two points.
     *
     * @param src the source point
     * @param des the destination point
     * @return the distance between the points
     */
    public static double getDistance(double[] src, double[] des) {
        return Math.sqrt(Math.pow(des[0] - src[0], 2) + Math.pow(des[1] - src[1], 2));
    }
}
