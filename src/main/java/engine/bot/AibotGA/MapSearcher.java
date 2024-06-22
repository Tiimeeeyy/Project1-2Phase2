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

public class MapSearcher {
    private TerrainType[][] terrain;
    private int[] startBallPostion;
    private int[] holePostion;
    private int[] lastPoint;
    private double r;
    private int sandResolution=5;
    private int grassResolution=10;

    public MapSearcher(String mappath, double[] startBallPostion, double[] holePostion, double r) {
        MapHandler map = new MapHandler();
        map.readmap(mappath);
        this.terrain=map.getTerrain();
        this.startBallPostion = Utility.coordinateToPixel(startBallPostion);
        this.holePostion = Utility.coordinateToPixel(holePostion);
        this.r = r;
    }

    public ArrayList<double[]> findShortestPath() {
        
        Set<int[]> visited = new HashSet<>();
        visited.add(startBallPostion.clone());

        Map<int[], int[]> previous = new HashMap<>();

        Queue<int[]> queue = new LinkedList<>();
        queue.add(startBallPostion.clone());
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int i = current[0];
            int j = current[1];
            if (Utility.getDistance(Utility.pixelToCoordinate(current), Utility.pixelToCoordinate(holePostion)) <= grassResolution / Utility.ratio) {
                lastPoint = current.clone();
                // System.out.println("holereached");
                break;
            }
            
            int res= terrain[i][j].equals(TerrainType.Sand) ? sandResolution : grassResolution;
            

            if (i+res<=499) {
                checkPoint(new int[]{i + res, j}, current, visited, queue, previous);  
            }
            if (j+res<=499) {
                checkPoint(new int[]{i, j + res}, current, visited, queue, previous);
            }
            if (i-res>=0) {
                checkPoint(new int[]{i - res, j}, current, visited, queue, previous);
            }
            if (j-res>=0) {
                checkPoint(new int[]{i, j - res}, current, visited, queue, previous);
            }
            
            
        }
        return reConstruct(previous);

    }

    private void checkPoint(int[] point,int[] current, Set<int[]> visited, Queue<int[]> queue, Map<int[],int[]>previous){
        if ((terrain[point[0]][point[1]].equals(TerrainType.Grass) || terrain[point[0]][point[1]].equals(TerrainType.Sand)) 
                && (!contains(visited, point))) {
            visited.add(point.clone());
            queue.add(point.clone());
            previous.put(point.clone(), current.clone());
        }
    }

    private ArrayList<double[]> reConstruct(Map<int[], int[]> previous) {
        ArrayList<double[]> shortestPath = new ArrayList<>();
        int[] cur = lastPoint.clone();

        shortestPath.add(Utility.pixelToCoordinate(cur));
        while (!equals(getValueOf(previous, cur), startBallPostion)) {
            cur = getValueOf(previous, cur);
            shortestPath.addFirst(Utility.pixelToCoordinate(cur));
        }

        return shortestPath;

    }

    private boolean equals(int[] a, int[] b) {
        boolean re = true;
        for (int i = 0; i < b.length; i++) {
            re = re && (a[i] == b[i]);
        }
        return re;
    }

    private boolean contains(Set<int[]> set, int[] target) {
        for (int[] entry : set) {
            if (equals(entry, target)) {
                return true;
            }
        }
        return false;
    }

    private int[] getValueOf(Map<int[], int[]> map, int[] targetKey) {
        for (Map.Entry<int[], int[]> entry : map.entrySet()) {
            if (equals(entry.getKey(), targetKey)) {
                return entry.getValue();
            }
        }
        return null;
    }
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

    private double[] getDirection(double[] point1, double[] point2) {
        return new double[]{Math.signum(point2[0] - point1[0]), Math.signum(point2[1] - point1[1])};
    }

    public boolean isObstacled(double[] pointa, double[] pointb){
        double[]a=pointa.clone();
        double[]b=pointb.clone();

        if (Math.abs(a[0]-b[0])>=Math.abs(a[1]-b[1])) {
            double slope=(b[1]-a[1])/(b[0]-a[0]);
            double intercept=a[1]-slope*a[0];
            if (a[0]>b[0]) {
                double[] c=b;
                b=a;
                a=c;
            }
            for (double i =a[0] ; i < b[0]; i=i+1.0/Utility.ratio) {
                int[] p=Utility.coordinateToPixel(new double[]{i,i*slope+intercept});
                if ((!terrain[p[0]][p[1]].equals(TerrainType.Grass)) && (!terrain[p[0]][p[1]].equals(TerrainType.Sand))){
                    return true;
                }
            }
        }else{
            double slope=(b[0]-a[0])/(b[1]-a[1]);
            double intercept=a[0]-slope*a[1];
            if (a[1]>b[1]) {
                double[] c=b;
                b=a;
                a=c;
            }
            for (double i =a[1] ; i < b[1]; i=i+1.0/Utility.ratio) {
                int[] p=Utility.coordinateToPixel(new double[]{i*slope+intercept,i});
                if ((!terrain[p[0]][p[1]].equals(TerrainType.Grass)) && (!terrain[p[0]][p[1]].equals(TerrainType.Sand))) {
                    return true;
                }
            }
        }
        return false;
    }

    public double howFarItSee(ArrayList<double[]> shortestPath, double[] x){
        double fit=1;
        for (int i =0; i<shortestPath.size();i++) {
            if (!isObstacled(x, shortestPath.get(i))) {
                fit = i;
            }
        }
        return fit;
    }

    public void createImage( ArrayList<double[]> path) {
        // MapSearcher mapSearcher = new MapSearcher("src/main/resources/userInputMap.png", startBallPostion, holePosition, radisus);
        // ArrayList<double[]> test = mapSearcher.findShortestPath();
        MapHandler aa=new MapHandler();
        aa.plotTrajectory("src/main/resources/userInputMap.png", "src/main/resources/outtest.png", path);
    }

    public static void main(String[] args) {
        MapSearcher mapSearcher = new MapSearcher("src/main/resources/userInputMap.png", new double[]{-24, 0}, new double[]{4, 1}, 0.1);
        ArrayList<double[]> test = mapSearcher.findShortestPath();
        

        MapHandler aa=new MapHandler();
        aa.plotTrajectory("src/main/resources/userInputMap.png", "src/main/resources/outtest.png", test);
        
        ArrayList<double[]> turningPoints = mapSearcher.getTurningPoints(test);
        for (double[] point : turningPoints) {
            System.out.println("Turning point: " + Arrays.toString(point));
        }
    }
}
