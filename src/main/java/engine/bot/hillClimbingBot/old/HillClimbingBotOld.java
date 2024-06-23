// package engine.bot.hillClimbingBot.upd;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Random;
// import engine.solvers.GolfGameEngine;
// import engine.bot.AibotGA.MapSearcher;

// public class HillClimbingBotNEW {
//     private boolean goal = false;
//     private GolfGameEngine game;
//     private double[] startBallPosition;
//     private double[] holePosition;
//     private double[] velocity;
//     private boolean useAnotherAlgorithm = false;

//     private static final int MAX_ITERATIONS = 5;
//     private static final int MAX_ITERATIONS_FINAL = 8;

//     private static final double INITIAL_STEP_SIZE = 1.0;
//     private static final double TOLERANCE = 0.02;
//     private static final int RANDOM_RESTARTS = 4;

//     private MapSearcher mapSearcher;
//     private ArrayList<double[]> visitedPositions = new ArrayList<>();
//     private static final int MAX_VISITED_COUNT = 3;
//     private ArrayList<double[]> shortestPath;
//     private ArrayList<double[]> turningPoints;
//     private double duration;

//     public HillClimbingBotNEW(GolfGameEngine game, double[] startBallPosition, double[] holePosition, String mapPath, double radius) {
//         this.game = game;
//         this.startBallPosition = startBallPosition.clone();
//         this.holePosition = holePosition;
//         this.mapSearcher = new MapSearcher(mapPath, startBallPosition, holePosition, radius);
//         this.shortestPath = mapSearcher.findShortestPath();
//         this.turningPoints = mapSearcher.getTurningPoints(shortestPath);
//         this.velocity = initializeVelocity();
//         mapSearcher.createImage(shortestPath);
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
//             } else if (!this.useAnotherAlgorithm) {
//                 shot = hillClimbing();
//             } else {
//                 shot = hillClimbingFinalShot();
//             }

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
//                     updateTurningPoints();
//                 }
//                 shots.add(currentShot.clone());
//             } else {
//                 System.out.println("NO SHOT. WATER!");
//             }

//             totalIterations++; 
//         }

//         long endTime = System.currentTimeMillis(); 
//         duration =( endTime - startTime)/1000.0;

//         System.out.println("Algorithm completed in " + duration  + " seconds");
//         System.out.println("Total iterations: " + totalIterations);

//         return shots;
//     }

//     public double getDuration(){
//         return this.duration;
//     }

//     public boolean isGoal() {
//         return this.goal;
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

//                 System.out.println("Restart " + restart + ", Iteration " + i + ": Velocity = [" + velocity[0] + ", " + velocity[1] + "], Fitness = " + currentFitness + " Best Fitness: " + bestFitness);
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
//                     if (fitness > currentFitness) {
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

//                 System.out.println("Restart " + restart + ", Iteration " + i + ": Velocity = [" + velocity[0] + "," +velocity[1] + "], Fitness = " + currentFitness + " Best Fitness: " + bestFitness);
//             }
//             if (currentFitness > bestFitness) {
//                 bestFitness = currentFitness;
//                 bestVelocity = velocity.clone();
//             }
//         }
//         return bestVelocity;
//     }

//     private double evaluateFinalShotFitness(double[] ballPosition, double[] velocity) {
//         double[] finalPosition = getTrajectory(ballPosition, velocity);
//         double distanceToHole = calculateDistance(finalPosition, holePosition);

//         if (distanceToHole<=4){
//             double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
//             game.shoot(inputEngine, false);
//             if (game.getMessage().contains("Goal!!!")) {
//                 return 0.001;
//             }
//         }
//         return -distanceToHole;
//     }

//     private double[][] generateNeighbors(double[] currentVelocity, double stepSize) {
//         ArrayList<double[]> neighborsList = new ArrayList<>();

//         for (double dx : new double[]{-stepSize, 0, stepSize}) {
//             for (double dy : new double[]{-stepSize, 0, stepSize}) {
//                 if (dx != 0 || dy != 0) {
//                     double[] neighbor = {clamp(currentVelocity[0] + dx, -5, 5), clamp(currentVelocity[1] + dy, -5, 5)};
//                     neighborsList.add(neighbor);
//                 }
//             }
//         }

//         return neighborsList.toArray(new double[neighborsList.size()][]);
//     }

//     private double evaluateFitness(double[] ballPosition, double[] velocity) {
//         double[] finalPosition = getTrajectory(ballPosition, velocity);
//         int fit = (int)mapSearcher.howFarItSee(shortestPath, finalPosition);

//         // turningPoints.remove(nearestTurningPoint);
//         return fit-Math.log10((game.getDistance(game.getStoppoint(),  shortestPath.get(fit))+0.01)/(calculateDistance(startBallPosition,shortestPath.get(fit) )+0.01)) ;
//     }

//     private boolean checkNoWater(double[] ballPosition, double[] velocity) {
//         double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
//         game.shoot(inputEngine, false);
//         if (game.getMessage().contains("Water!")) {
//             return false;
//         }
//         return true;
//     }

//     private double[] getTrajectory(double[] ballPosition, double[] velocity) {
//         double[] inputEngine = {ballPosition[0], ballPosition[1], velocity[0], velocity[1]};
//         game.shoot(inputEngine, false);
        

//         double[] finalPosition = game.getStoppoint();
//         return finalPosition;
//     }

//     private static double calculateDistance(double[] finalPosition, double[] targetPosition) {
//         return Math.sqrt(Math.pow(finalPosition[0] - targetPosition[0], 2) + Math.pow(finalPosition[1] - targetPosition[1], 2));
//     }

//     private double[] initializeVelocity() {
//         Random rand = new Random();
//         double[] velocity = new double[2];

//         if (!turningPoints.isEmpty()) {
            
//             double[] nearestTurningPoint = turningPoints.get(0);
//             for (double[] point : turningPoints) {
//                 if (Math.abs(point[1] - startBallPosition[1]) < Math.abs(nearestTurningPoint[1] - startBallPosition[1])) {
//                     nearestTurningPoint = point;
//                 }
//             }

//             if (Math.abs(nearestTurningPoint[1] - startBallPosition[1]) < 2) {
//                 // less than 5 in y, choose based on x
//                 if (nearestTurningPoint[0] > startBallPosition[0]) {
//                     // positive x
//                     if (rand.nextBoolean()) {
//                         velocity[0] = rand.nextDouble() * 5;   // 1q
//                         velocity[1] = rand.nextDouble() * 5;
//                     } else {
//                         velocity[0] = rand.nextDouble() * 5;   // 4q
//                         velocity[1] = -(rand.nextDouble() * 5);
//                     }
//                 } else {
//                     // negative x
//                     if (rand.nextBoolean()) {
//                         velocity[0] = -(rand.nextDouble() * 5); // 2q
//                         velocity[1] = rand.nextDouble() * 5;
//                     } else {
//                         velocity[0] = -(rand.nextDouble() * 5); // 3q
//                         velocity[1] = -(rand.nextDouble() * 5);
//                     }
//                 }
//             } else {
//                 // more than 5 in y, choose based on y
//                 if (nearestTurningPoint[1] > startBallPosition[1]) {
//                     // y positive
//                     if (rand.nextBoolean()) {
//                         velocity[0] = rand.nextDouble() * 5;   // 1q
//                         velocity[1] = rand.nextDouble() * 5;
//                     } else {
//                         velocity[0] = -(rand.nextDouble() * 5); // 2q
//                         velocity[1] = rand.nextDouble() * 5;
//                     }
//                 } else {
//                     // negative y
//                     if (rand.nextBoolean()) {
//                         velocity[0] = rand.nextDouble() * 5;    // 3q
//                         velocity[1] = -(rand.nextDouble() * 5);
//                     } else {
//                         velocity[0] = -(rand.nextDouble() * 5);  // 4q
//                         velocity[1] = -(rand.nextDouble() * 5);
//                     }
//                 }
//             }
//         } else {
//             int quadrantIndex = rand.nextInt(4);
//             switch (quadrantIndex) {
//                 case 0: // [-;-]
//                     velocity[0] = -(rand.nextDouble() * 5);
//                     velocity[1] = -(rand.nextDouble() * 5);
//                     break;
//                 case 1: // [+;-]
//                     velocity[0] = rand.nextDouble() * 5;
//                     velocity[1] = -(rand.nextDouble() * 5);
//                     break;
//                 case 2: // [+;+]
//                     velocity[0] = rand.nextDouble() * 5;
//                     velocity[1] = rand.nextDouble() * 5;
//                     break;
//                 case 3: // [-;+]
//                     velocity[0] = -(rand.nextDouble() * 5);
//                     velocity[1] = rand.nextDouble() * 5;
//                     break;
//             }
//         }

//         return velocity;
//     }

//     private void updateTurningPoints() {
//         turningPoints.removeIf(point -> calculateDistance(point, startBallPosition) < TOLERANCE);
//     }

//     private static double clamp(double value, double min, double max) {
//         return Math.max(min, Math.min(max, value));
//     }
// }


