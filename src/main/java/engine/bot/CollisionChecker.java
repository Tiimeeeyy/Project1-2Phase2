package engine.bot;

import engine.solvers.RK4;
import engine.solvers.golfphysics;

import java.util.ArrayList;
import java.util.Comparator;

public class CollisionChecker implements BotInterface {
    RK4 rk4;

    public double[][] collisionChecker(double[][] info, double[] x, double[] friction, double[] hole) {
        double[] direction = {hole[0] - x[0], hole[1] - x[1]};
        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        direction[0] /= magnitude;
        direction[1] /= magnitude;

        // Create Velocity Vectors
        double[][] velocities = createVelocityVectors(direction);
        ArrayList<double[]> noCollisionVelocities = new ArrayList<>();

        for (double[] velocity : velocities) {
            double[] position = {x[0], x[1]};
            double speed = 5;
            boolean collision = false;

            while (speed > 0.001) {
                double[] a = {friction[1], friction[0]};
                double[] dh = {0, 0};
                boolean equilibrium = rk4.nextstep(new golfphysics(), new double[]{
                                position[0], position[1], velocity[0], velocity[1]},
                        a, dh, 0.1);

                position[0] += velocity[0] * 0.1;
                position[1] += velocity[1] * 0.1;

                if (infoCollision(info, position)) {
                    collision = true;
                    break;
                }

                speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

            }
            if (!collision) {
                noCollisionVelocities.add(new double[]{position[0], position[1], velocity[0], velocity[1]});
            }
        }
        return new double[noCollisionVelocities.size()][4];

    }

    private boolean infoCollision(double[][] info, double[] position) {
        int x = (int) Math.floor(position[0]);
        int y = (int) Math.floor(position[1]);

        if (x >= 0 && x < info.length && y >= 0 && y < info[0].length) {
            if (info[x][y] != 0) {
                return true;
            }
        }
        return false;
    }

    public double[][] createVelocityVectors(double[] direction) {

        double degreesPos = Math.toRadians(30);
        double degreesNeg = Math.toRadians(-30);

        double[][] velocities = new double[3][2];
        velocities[0] = new double[]{5 * direction[0], 5 * direction[1]};
        velocities[1] = new double[]{
                5 * Math.cos(degreesPos) * direction[0] - Math.sin(degreesPos) * direction[1],
                5 * Math.sin(degreesPos) * direction[0] + Math.cos(degreesPos) * direction[1]
        };
        velocities[2] = new double[]{
                5 * Math.cos(degreesNeg) * direction[0] - Math.sin(degreesNeg) * direction[1],
                5 * Math.sin(degreesNeg) * direction[0] + Math.cos(degreesNeg) * direction[1]
        };
        return velocities;
    }

    public double[][] heightChecker(double[][][] map, double[] x, double[] friction, double[] hole) {
        double[] direction = {hole[0] - x[0], hole[1] - x[1]};
        double magnitude = Math.sqrt(direction[0] * direction[0] + direction[1] * direction[1]);
        direction[0] /= magnitude;
        direction[1] /= magnitude;

        double[][] velocities = createVelocityVectors(direction);
        ArrayList<double[]> heightVelocities = new ArrayList<>();

        for (double[] velocity : velocities) {
            double[] position = {x[0], x[1]};
            double speed = 5;

            while (speed > 0.001) {
                double[] a = {friction[1], friction[0]};
                double[] dh = {0, 0};
                boolean equilibrium = rk4.nextstep(new golfphysics(), new double[]{
                                position[0], position[1], velocity[0], velocity[1]},
                        a, dh, 0.1);

                position[0] += velocity[0] * 0.1;
                position[1] += velocity[1] * 0.1;

                speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

            }
            int finalX = (int) Math.floor(position[0]);
            int finalY = (int) Math.floor(position[1]);
            if (heightDifference(map, position)) {
                double height = map[finalX][finalY][2];
                heightVelocities.add(new double[]{
                        position[0], position[1], velocity[0], velocity[1], height
                });
            }
        }
        heightVelocities.sort(Comparator.comparingDouble(a -> a[4]));

        double[][] heightVelocitiesArray = new double[heightVelocities.size()][5];
        return heightVelocities.toArray(heightVelocitiesArray);
    }


    private boolean heightDifference(double[][][] map, double[] position) {
        int finalX = (int) Math.floor(position[0]);
        int finalY = (int) Math.floor(position[1]);

        if (finalX >= 0 && finalX < map.length && finalY >= 0 && finalY < map[0].length) {
            return true;
        }
        return false;
    }
}
