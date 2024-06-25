package engine.bot.AibotGA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import engine.solvers.MapHandler;
import engine.solvers.TerrainType;
import engine.solvers.Utility;

/**
 * The MapSearcher class is responsible for finding the shortest path from the starting position to the hole 
 * on a golf course map, considering obstacles and different terrain types.
 */
public class MapSearcher {
    private TerrainType[][] terrain;
    private int[] startBallPosition;
    private int[] holePosition;
    private int[] lastPoint;
    private double r;
    private int sandResolution = 5;
    private int grassResolution = 10;

    /**
     * Constructs a MapSearcher instance.
     *
     * @param mapPath          the path to the map file
     * @param startBallPosition the starting position of the ball
     * @param holePosition     the position of the hole
     * @param r                the radius of the ball
     */
    public MapSearcher(String mapPath, double[] startBallPosition, double[] holePosition, double r) {
        MapHandler map = new MapHandler();
        map.readmap(mapPath);
        this.terrain = map.getTerrain();
        this.startBallPosition = Utility.coordinateToPixel(startBallPosition);
        this.holePosition = Utility.coordinateToPixel(holePosition);
        this.r = r;
    }

    /**
     * Finds the shortest path from the starting position to the hole.
     *
     * @return an array list of coordinates representing the shortest path
     */
    public ArrayList<double[]> findShortestPath() {
        Set<int[]> visited = new HashSet<>();
        visited.add(startBallPosition.clone());

        Map<int[], int[]> previous = new HashMap<>();

        Queue<int[]> queue = new LinkedList<>();
        queue.add(startBallPosition.clone());

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int i = current[0];
            int j = current[1];
            if (Utility.getDistance(Utility.pixelToCoordinate(current), Utility.pixelToCoordinate(holePosition)) <= grassResolution / Utility.ratio) {
                lastPoint = current.clone();
                break;
            }

            int res = terrain[i][j].equals(TerrainType.Sand) ? sandResolution : grassResolution;

            if (i + res <= 499) {
                checkPoint(new int[]{i + res, j}, current, visited, queue, previous);
            }
            if (j + res <= 499) {
                checkPoint(new int[]{i, j + res}, current, visited, queue, previous);
            }
            if (i - res >= 0) {
                checkPoint(new int[]{i - res, j}, current, visited, queue, previous);
            }
            if (j - res >= 0) {
                checkPoint(new int[]{i, j - res}, current, visited, queue, previous);
            }
        }
        return reConstruct(previous);
    }

    /**
     * Checks a point to see if it is a valid position to move to.
     *
     * @param point    the point to check
     * @param current  the current position
     * @param visited  the set of visited positions
     * @param queue    the queue of positions to visit
     * @param previous the map of previous positions
     */
    private void checkPoint(int[] point, int[] current, Set<int[]> visited, Queue<int[]> queue, Map<int[], int[]> previous) {
        if ((terrain[point[0]][point[1]].equals(TerrainType.Grass) || terrain[point[0]][point[1]].equals(TerrainType.Sand))
                && (!contains(visited, point))) {
            visited.add(point.clone());
            queue.add(point.clone());
            previous.put(point.clone(), current.clone());
        }
    }

    /**
     * Reconstructs the shortest path from the previous positions map.
     *
     * @param previous the map of previous positions
     * @return an array list of coordinates representing the shortest path
     */
    private ArrayList<double[]> reConstruct(Map<int[], int[]> previous) {
        ArrayList<double[]> shortestPath = new ArrayList<>();
        int[] cur = lastPoint.clone();

        shortestPath.add(Utility.pixelToCoordinate(cur));
        while (!equals(getValueOf(previous, cur), startBallPosition)) {
            cur = getValueOf(previous, cur);
            shortestPath.addFirst(Utility.pixelToCoordinate(cur));
        }

        return shortestPath;
    }

    /**
     * Checks if two arrays are equal.
     *
     * @param a the first array
     * @param b the second array
     * @return true if the arrays are equal, false otherwise
     */
    private boolean equals(int[] a, int[] b) {
        boolean re = true;
        for (int i = 0; i < b.length; i++) {
            re = re && (a[i] == b[i]);
        }
        return re;
    }

    /**
     * Checks if a set contains a target array.
     *
     * @param set    the set of arrays
     * @param target the target array
     * @return true if the set contains the target array, false otherwise
     */
    private boolean contains(Set<int[]> set, int[] target) {
        for (int[] entry : set) {
            if (equals(entry, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the value associated with a key in a map.
     *
     * @param map       the map
     * @param targetKey the target key
     * @return the value associated with the target key
     */
    private int[] getValueOf(Map<int[], int[]> map, int[] targetKey) {
        for (Map.Entry<int[], int[]> entry : map.entrySet()) {
            if (equals(entry.getKey(), targetKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the turning points in a path.
     *
     * @param path the path
     * @return an array list of turning points
     */
    public ArrayList<double[]> getTurningPoints(ArrayList<double[]> path) {
        ArrayList<double[]> turningPoints = new ArrayList<>();
        if (path.size() < 3) {
            return turningPoints;
        }

        double[] previousDirection = getDirection(path.get(0), path.get(1));
        for (int i = 1; i < path.size() - 1; i++) {
            double[] currentDirection = getDirection(path.get(i), path.get(i + 1));
            if (!Arrays.equals(previousDirection, currentDirection)) {
                turningPoints.add(path.get(i));
            }
            previousDirection = currentDirection;
        }
        return turningPoints;
    }

    /**
     * Gets the direction from one point to another.
     *
     * @param point1 the first point
     * @param point2 the second point
     * @return the direction as an array of doubles
     */
    private double[] getDirection(double[] point1, double[] point2) {
        return new double[]{Math.signum(point2[0] - point1[0]), Math.signum(point2[1] - point1[1])};
    }

    /**
     * Checks if there are obstacles between two points.
     *
     * @param pointA the first point
     * @param pointB the second point
     * @return true if there are obstacles, false otherwise
     */
    public boolean isObstacled(double[] pointA, double[] pointB) {
        double[] a = pointA.clone();
        double[] b = pointB.clone();

        if (Math.abs(a[0] - b[0]) >= Math.abs(a[1] - b[1])) {
            double slope = (b[1] - a[1]) / (b[0] - a[0]);
            double intercept = a[1] - slope * a[0];
            if (a[0] > b[0]) {
                double[] c = b;
                b = a;
                a = c;
            }
            for (double i = a[0]; i < b[0]; i = i + 1.0 / Utility.ratio) {
                int[] p = Utility.coordinateToPixel(new double[]{i, i * slope + intercept});
                if ((!terrain[p[0]][p[1]].equals(TerrainType.Grass)) && (!terrain[p[0]][p[1]].equals(TerrainType.Sand))) {
                    return true;
                }
            }
        } else {
            double slope = (b[0] - a[0]) / (b[1] - a[1]);
            double intercept = a[0] - slope * a[1];
            if (a[1] > b[1]) {
                double[] c = b;
                b = a;
                a = c;
            }
            for (double i = a[1]; i < b[1]; i = i + 1.0 / Utility.ratio) {
                int[] p = Utility.coordinateToPixel(new double[]{i * slope + intercept, i});
                if ((!terrain[p[0]][p[1]].equals(TerrainType.Grass)) && (!terrain[p[0]][p[1]].equals(TerrainType.Sand))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculates how far the ball can see along the shortest path.
     *
     * @param shortestPath the shortest path
     * @param x            the current position
     * @return the distance the ball can see
     */
    public double howFarItSee(ArrayList<double[]> shortestPath, double[] x) {
        double fit = 1;
        for (int i = 0; i < shortestPath.size(); i++) {
            if (!isObstacled(x, shortestPath.get(i))) {
                fit = i;
            }
        }
        return fit;
    }

    /**
     * Creates an image of the map with the path plotted on it.
     *
     * @param path the path to plot
     */
    public void createImage(ArrayList<double[]> path) {
        MapHandler mapHandler = new MapHandler();
        mapHandler.plotTrajectory("src/main/resources/userInputMap.png", "src/main/resources/outtest.png", path);
    }

    /**
     * Main method for testing the MapSearcher class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        MapSearcher mapSearcher = new MapSearcher("src/main/resources/userInputMap.png", new double[]{-3, 0}, new double[]{4, 1}, 0.1);
        ArrayList<double[]> test = mapSearcher.findShortestPath();

        MapHandler mapHandler = new MapHandler();
        mapHandler.plotTrajectory("src/main/resources/userInputMap.png", "src/main/resources/outtest.png", test);

        ArrayList<double[]> turningPoints = mapSearcher.getTurningPoints(test);
        for (double[] point : turningPoints) {
            System.out.println("Turning point: " + Arrays.toString(point));
        }
    }
}
