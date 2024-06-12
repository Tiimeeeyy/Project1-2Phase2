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
import engine.solvers.Utility;

public class MapSearcher {
    private boolean[][] grass;
    private int[] startBallPostion;
    private int[] holePostion;
    private double r;

    MapSearcher(String mappath, double[] startBallPostion,double[] holePostion, double r){
        MapHandler map=new MapHandler();
        // map.readmap(mappath);
        map.readmap(mappath);
        this.grass=map.getGrass();
        this.startBallPostion=Utility.coordinateToPixel(startBallPostion);
        this.holePostion=Utility.coordinateToPixel(holePostion);
        this.r=Utility.ratio*r;
    }

    private ArrayList<double[]> findShortestPath(){
        
        Set<int[]> visited=new HashSet<>();
        visited.add(startBallPostion.clone());
       
        Map<int[],int[]> previous=new HashMap<>();

        Queue<int[]> queue=new LinkedList<>();
        queue.add(startBallPostion);
        while (!queue.isEmpty()) {
            int[] current=queue.poll();
            int i=current[0];
            int j=current[1];
            if (equals(current,holePostion)) {
                System.out.println("holereached");
                break;
            }
            
            if (i==1 || j==1 || i==499 || j==499) {
                continue;
            }
            
            
            if (grass[i+1][j] && (!contains(visited, new int[]{i+1,j}))) {
                int[] temp=new int[]{i+1,j};
                visited.add(temp.clone());
                queue.add(temp.clone());
                previous.put(temp.clone(),current.clone());
            }
            if (grass[i][j+1] && (!contains(visited, new int[]{i,j+1}))) {
                int[] temp=new int[]{i,j+1};
                visited.add(temp.clone());
                queue.add(temp.clone());
                previous.put(temp.clone(),current.clone());
            }
            if (grass[i-1][j] && (!contains(visited, new int[]{i-1,j}))) {
                int[] temp=new int[]{i-1,j};
                visited.add(temp.clone());
                queue.add(temp.clone());
                previous.put(temp.clone(),current.clone());
            }
            if (grass[i][j-1] && (!contains(visited, new int[]{i,j-1}))) {
                int[] temp=new int[]{i,j-1};
                visited.add(temp.clone());
                queue.add(temp.clone());
                previous.put(temp.clone(),current.clone());
            }
           
        }

        System.out.println(Arrays.toString(holePostion));
        return reConstruct(previous);

    }
    private  ArrayList<double[]> reConstruct(Map<int[],int[]> previous){
        ArrayList<double[]> shortestPath=new ArrayList<>();
        int[] cur=holePostion.clone();
        
        shortestPath.add(Utility.pixelToCoordinate(cur));
        while (!equals(getValueOf(previous, cur),startBallPostion)) {
            cur=getValueOf(previous, cur);
            shortestPath.addFirst(Utility.pixelToCoordinate(cur));
        }
        
        return shortestPath;

    }

    private  boolean equals(int[] a, int[] b){
        boolean re=true;
        for (int i = 0; i < b.length; i++) {
            re=re&&(a[i]==b[i]);
        }
        return re;
    }
    private  boolean contains(Set<int[]> set,int[] target){
        for (int[] entry : set) {
            if (equals(entry, target)) {
                return true;                
            }
        }
        return false;
    }
    private  int[] getValueOf(Map<int[],int[]> map,int[] targetKey){
        for (Map.Entry<int[],int[]> entry: map.entrySet()) {
            if (equals(entry.getKey(), targetKey)) {
                return entry.getValue();                
            }
        }
        return null;
    }
    

    public static void main(String[] args) {
       MapSearcher mapSearcher=new MapSearcher("src/main/resources/userInputMap.png", new double[]{-3,0}, new double[]{5,5}, 0.1);
       ArrayList<double[]> test=mapSearcher.findShortestPath();
       for (double[] entry:test) {
            System.out.println(Arrays.toString(entry));
       }
       MapHandler aa=new MapHandler();
       aa.plotTrajectory("src/main/resources/userInputMap.png", "src/main/resources/outtest.png", test);

    }
    
}
