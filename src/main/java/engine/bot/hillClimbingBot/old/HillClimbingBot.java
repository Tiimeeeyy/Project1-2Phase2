// package engine.bot.hillClimbingBot.upd;

// import java.util.ArrayList;
// import java.util.Random;
// import engine.solvers.GolfGameEngine;
// import engine.bot.AibotGA.MapSearcher;

// public class HillClimbingBotNEW {
//     private boolean goal = false;
//     private GolfGameEngine game;
//     private double[] startBallPosition;
//     private double[] holePosition;
//     private double[] velocity;
//     // private String message = "";
//     private boolean useAnotherAlgorithm = false;
//     private static int quadrantIndex = 0;


//     private static final int MAX_ITERATIONS = 10;
//     private static final int MAX_ITERATIONS_FINAL = 15;

//     private static final double INITIAL_STEP_SIZE = 1.0;
//     private static final double TOLERANCE = 0.02;
//     private static final int RANDOM_RESTARTS = 4;

//     private MapSearcher mapSearcher;
//     private ArrayList<double[]> visitedPositions = new ArrayList<>();
//     private static final int MAX_VISITED_COUNT = 3;
//     private ArrayList<double[]> shortestPath;

//     public HillClimbingBotNEW(GolfGameEngine game, double[] startBallPosition, double[] holePosition, String mapPath, double radius) {
//         this.game = game;
//         this.startBallPosition = startBallPosition.clone();
//         this.holePosition = holePosition;
//         this.velocity = initializeVelocity();
//         this.mapSearcher = new MapSearcher(mapPath, startBallPosition, holePosition, radius);
//         this.shortestPath = mapSearcher.findShortestPath();
//     }

//     public ArrayList<double[]> hillClimbingAlgorithm() {
//         ArrayList<double[]> shots = new ArrayList<>();
//         this.goal = false;
//         int numOfShots = 0;
//         long startTime = System.currentTimeMillis(); 
//         int totalIterations = 0; 
//         double bestFitness = Double.NEGATIVE_INFINITY;

//         while (!this.goal) {
//             double[] shot;
//             double currentFitness;

//             if (!mapSearcher.isObstacled(startBallPosition, holePosition)) {
//                 shot = hillClimbingFinalShot();
//                 this.useAnotherAlgorithm = true;
//                 // currentFitness = evaluateFinalShotFitness(startBallPosition, shot);
//             } else if (!this.useAnotherAlgorithm) {
//                 shot = hillClimbing();
//                 // currentFitness = evaluateFitness(startBallPosition, shot);
//             } else {
//                 shot = hillClimbingFinalShot();
//                 // currentFitness = evaluateFinalShotFitness(startBallPosition, shot);
//             }
//             // && currentFitness > bestFitness
//             // System.out.println(checkNoWater(startBallPosition, shot));
//             // double[] inputEngine = {startBallPosition[0], startBallPosition[1], velocity[0], velocity[1]};
//             // game.shoot(inputEngine, false);
//             // System.out.println(!game.getMessage().contains("Water!"));
//             // System.out.println(game.getMessage());
//             // System.out.println("pos");
//             if (checkNoWater(startBallPosition, shot)) {
//                 double[] currentShot = {startBallPosition[0], startBallPosition[1], shot[0], shot[1]};
//                 numOfShots++;
//                 System.out.println("SHOT: " + numOfShots + " " + shot[0] + ", " + shot[1]);
//                 startBallPosition = getTrajectory(startBallPosition, shot).clone();
//                 if (calculateDistance(startBallPosition, holePosition) <= TOLERANCE) {
//                     this.goal = true;
//                 }

//                 if (isStuck(startBallPosition)) {
//                     System.out.println("Bro is stuck, reinitializing velocity.");
//                     this.velocity = initializeVelocity();
//                 } else {
//                     visitedPositions.add(startBallPosition.clone());
//                 }
//                 shots.add(currentShot.clone());
//                 // bestFitness = currentFitness;
//             } else{
//                 System.out.println("NO SHOT. WATER!");
//             }

//             totalIterations++; 
//         }

//         long endTime = System.currentTimeMillis(); 
//         long duration = endTime - startTime;

//         System.out.println("Algorithm completed in " + (duration / 1000.0) + " seconds");
//         System.out.println("Total iterations: " + totalIterations);

//         return shots;
//     }


//     private boolean isStuck(double[] position) {
//         int count = 0;
//         for (double[] pos : visitedPositions) {
//             if (calculateDistance(pos, position) < TOLERANCE) {
//                 count++;
//             }
//         }
//         return count >= MAX_VISITED_COUNT;
//     }

//     private double[] hillClimbing() {
//         double bestFitness = Double.NEGATIVE_INFINITY;
//         double[] bestVelocity = new double[2];

//         for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
//             this.velocity = initializeVelocity();
//             double currentFitness;
//             currentFitness = evaluateFitness(startBallPosition, velocity);

//             double stepSize = INITIAL_STEP_SIZE;
//             int iterationsWithoutImprovement = 0;

//             for (int i = 0; i < MAX_ITERATIONS; i++) {
//                 double[][] neighbors = generateNeighbors(velocity, stepSize);
//                 boolean foundBetter = false;
//                 for (double[] neighbor : neighbors) {
//                     double fitness;
//                     fitness = evaluateFitness(startBallPosition, neighbor);
 
//                     // double fitness = evaluateFitness(startBallPosition, neighbor);
//                     if (fitness > currentFitness) { 
//                         currentFitness = fitness;
//                         velocity = neighbor;
//                         foundBetter = true;
//                         iterationsWithoutImprovement = 0;
//                     }
//                 }
//                 if (!foundBetter) {
//                     stepSize /= 2;
//                     iterationsWithoutImprovement++;
//                 } else {
//                     stepSize = INITIAL_STEP_SIZE;
//                 }

//                 if (iterationsWithoutImprovement >= 4) {
//                     break;
//                 }

//                 System.out.println("Restart " + restart + ", Iteration " + i + ": Velocity = [" + velocity[0] + ", " + velocity[1] + "], Fitness = " + currentFitness + " Best Fitness: " + bestFitness );
//             }
//             if (currentFitness > bestFitness) {
//                 bestFitness = currentFitness;
//                 bestVelocity = velocity.clone();
//             }
//         }

//         return bestVelocity;
//     }

//     private double[] hillClimbingFinalShot() {
//         double bestFitness = Double.NEGATIVE_INFINITY;
//         double[] bestVelocity = new double[2];

//         for (int restart = 0; restart < RANDOM_RESTARTS; restart++) {
//             this.velocity = initializeVelocity();
//             double currentFitness = evaluateFinalShotFitness(startBallPosition, velocity);
//             double stepSize = INITIAL_STEP_SIZE;

//             for (int i = 0; i < MAX_ITERATIONS_FINAL; i++) {
//                 double[][] neighbors = generateNeighbors(velocity, stepSize);
//                 boolean foundBetter = false;
//                 for (double[] neighbor : neighbors) {
//                     double fitness = evaluateFinalShotFitness(startBallPosition, neighbor);
//                     if (fitness > currentFitness ) { 
//                         currentFitness = fitness;
//                         velocity = neighbor;
//                         foundBetter = true;
//                     }

//                 }
//                 if (!foundBetter) {
//                     stepSize /= 2;
//                 } else {
//                     stepSize = INITIAL_STEP_SIZE;
//                 }
//                 if ((Math.abs(currentFitness) <= TOLERANCE || bestFitness > -0.2)) {
//                     this.goal = true;
//                     break;
//                 }

//                 System.out.println("Restart " + restart + ", Iteration " + i + ": Velocity = [" + velocity[0] + ", " + velocity[1] + "], Fitness = " + currentFitness + " Best Fitness: " + bestFitness);
//             }
//             if (currentFitness > bestFitness ) {
//                 bestFitness = currentFitness;
//                 bestVelocity = velocity.clone();
//             }
//         }
//         return bestVelocity;
//     }

//     private double evaluateFinalShotFitness(double[] ballPosition, double[] velocity) {  
//         // if (!checkNoWater(ballPosition, velocity)){
//         //     return -100;
//         // } else{
//         double[] finalPosition = getTrajectory(ballPosition, velocity);
//         double distanceToHole = calculateDistance(finalPosition, holePosition);
//         return -distanceToHole;
//         // }

//     }

//     private double[][] generateNeighbors(double[] currentVelocity, double stepSize) {
//         ArrayList<double[]> neighborsList = new ArrayList<>();
        
//         for (double dx : new double[]{-stepSize, 0, stepSize}) {
//             for (double dy : new double[]{-stepSize, 0, stepSize}) {
//                 if (dx != 0 || dy != 0) {
//                     double[] neighbor = { clamp(currentVelocity[0] + dx, -5, 5), clamp(currentVelocity[1] + dy, -5, 5) };
//                     neighborsList.add(neighbor);
//                 }
//             }
//         }
        
//         return neighborsList.toArray(new double[neighborsList.size()][]);
//     }

//     private double evaluateFitness(double[] ballPosition, double[] velocity) {
//         // if (!checkNoWater(ballPosition, velocity)){
//         //     return -1;
//         // } else{
//         double[] finalPosition = getTrajectory(ballPosition, velocity);
//         return mapSearcher.howFarItSee(shortestPath, finalPosition);
//         // }
       
//     }

//     private boolean checkNoWater(double[] ballPosition, double[] velocity){
//         double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
//         game.shoot(inputEngine, false);
//         if(game.getMessage().contains("Water!")){
//             return false;
//         }
//         return true;
//     }
    


//     private double[] getTrajectory(double[] ballPosition, double[] velocity) {
//         double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
//         game.shoot(inputEngine, false);
//         // message = game.getMessage();
        
//         double[] finalPosition = game.getStoppoint();
//         return finalPosition;
//     }
    

//     private static double calculateDistance(double[] finalPosition, double[] targetPosition) {
//         return Math.sqrt(Math.pow(finalPosition[0] - targetPosition[0], 2) + Math.pow(finalPosition[1] - targetPosition[1], 2));
//     }

//     // private static double[] initializeVelocity() {
//     //     Random rand = new Random();
//     //     double[] velocity = new double[2];
//     //     velocity[0] = rand.nextDouble() * 10 - 4;
//     //     velocity[1] = rand.nextDouble() * 10 - 4;
//     //     return velocity;
//     // }

//     private static double[] initializeVelocity() {
//         Random rand = new Random();
//         double[] velocity = new double[2];
//         int quadrantIndex = rand.nextInt(4);

        
//         switch (quadrantIndex) {
//             case 0: // [-;-]
//                 velocity[0] = -(rand.nextDouble() * 5);
//                 velocity[1] = -(rand.nextDouble() * 5);
//                 break;
//             case 1: //  [+;-]
//                 velocity[0] = rand.nextDouble() * 5;
//                 velocity[1] = -(rand.nextDouble() * 5);
//                 break;
//             case 2: //  [+;+]
//                 velocity[0] = rand.nextDouble() * 5;
//                 velocity[1] = rand.nextDouble() * 5;
//                 break;
//             case 3: //  [-;+]
//                 velocity[0] = -(rand.nextDouble() * 5);
//                 velocity[1] = rand.nextDouble() * 5;
//                 break;
//         }

//         // quadrantIndex = (quadrantIndex + 1) % 4;

//         return velocity;
//     }

    

//     private static double clamp(double value, double min, double max) {
//         return Math.max(min, Math.min(max, value));
//     }
// }
