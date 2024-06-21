package ui.controller;

import engine.bot.AiBotGAV.AiBotGAV;
import engine.bot.AibotGA.AiBotGA;
// import engine.bot.hillClimbingBot.old.HillClimbingBot;
import engine.bot.hillClimbingBot.upd.HillClimbingBotNEW;
import engine.bot.newtonRaphsonBot.NewtonRaphsonBot;
import engine.bot.rule_based_new.DistanceMeasure;
import engine.solvers.GolfGameEngine;
import engine.solvers.Utility;
import engine.solvers.odeSolvers.RK4;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import ui.Main;
import ui.helpers.CircularSlider;
import ui.screenFactory.ScreenInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.media.*;
import java.net.URL;


/**
 * Controller class for the third screen in the UI.
 */
public class ThirdScreenController implements ScreenInterface {

    @FXML
    private ImageView mapImageView;

    @FXML
    private Button goBackButton;

    @FXML
    private Button hitButton;

    @FXML
    private Slider powerSlider;

    @FXML
    private Pane circularSliderPane;

    @FXML
    private Canvas ballCanvas;

    @FXML
    private Label ballPositionLabel;

    @FXML
    private Label shotCountLabel;

    @FXML
    private Label directionLabel;

    @FXML
    private Label powerLabel;

    @FXML
    private TextArea logTextArea;

    @FXML
    private Button ruleBot;

    @FXML
    private Button chBot;

    @FXML
    private Button mlBot;

    @FXML
    private Button gaBot;

    @FXML
    private ComboBox<String> botSelect;

    @FXML
    private Button playBotButton;

    @FXML   
    private Button replayButton;
    private MediaPlayer mediaPlayer;


    private DistanceMeasure distanceMeasure;
    private CircularSlider circularSlider; // Custom circular slider for direction
    private double[] BallPosition; // Starting position of the ball
    private double[] HolePostion; // Position of the hole
    private GolfGameEngine golfGame; // Golf game engine
    private double grassFrictionKINETIC; // Kinetic friction on grass
    private double grassFrictionSTATIC; // Static friction on grass
    private int shotCount = 0; // Count of shots taken
    private ArrayList<double[]> fullTrajectory = new ArrayList<>(); // Full trajectory of the ball
    private boolean REACHED_THE_HOLE; // Flag to check if the ball reached the hole
    private Timeline timeline; // Animation timeline
    private boolean ballMoving = false;
    private boolean ruleBasedBot = false;
    private ArrayList<double[]> shots;
    private boolean botActivated = false;
    private double radiusHole; // Radius of the hole
    private ArrayList<double[]> plays;
    private double[] initPosit;
    private double[] a;

    

    private Parent root; // Root node

    /**
     * Constructor for ThirdScreenController.
     */
    public ThirdScreenController() {
    }

    /**
     * Gets the root node.
     *
     * @return the root node
     */
    @Override
    public Parent getRoot() {
        return root;
    }

    /**
     * Sets the root node.
     *
     * @param root the root node
     */
    public void setRoot(Parent root) {
        this.root = root;
    }

    /**
     * Initializes parameters for the golf game.
     *
     * @param startBallPostion     the starting position of the ball
     * @param HolePostion          the position of the hole
     * @param radiusHole           the radius of the hole
     * @param grassFrictionKINETIC the kinetic friction on grass
     * @param grassFrictionSTATIC  the static friction on grass
     */
    public void initializeParameters(double[] startBallPostion, double[] HolePostion, double radiusHole, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        this.BallPosition = startBallPostion;
        this.HolePostion = HolePostion;
        this.radiusHole = radiusHole;
        this.grassFrictionKINETIC = grassFrictionKINETIC;
        this.grassFrictionSTATIC = grassFrictionSTATIC;
        this.REACHED_THE_HOLE = false;
        this.a = new double[]{grassFrictionKINETIC, grassFrictionSTATIC};
        this.distanceMeasure = new DistanceMeasure(startBallPostion, a, HolePostion, radiusHole, REACHED_THE_HOLE);
        this.golfGame = new GolfGameEngine(new RK4(), a, 0.01, HolePostion, radiusHole, "src/main/resources/userInputMap.png");

        initialize();
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        if (BallPosition == null || HolePostion == null) {
            return;
        }
        loadNewImage();
        initializeComboBox();
    
        circularSlider = new CircularSlider();
        circularSliderPane.getChildren().add(circularSlider);
    
        circularSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.stop();
                moveBallToEndOfTrajectory();
            }
            updateDirection(newVal);
            drawBallAndArrow();
        });
        powerSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.stop();
                moveBallToEndOfTrajectory();
            }
            updatePower(newVal);
            drawBallAndArrow();
        });
    
        drawBallAndArrow();
        updateBallPositionLabel();
        updateShotCountLabel();
    }
    
    private void initializeComboBox() {
        botSelect.setItems(FXCollections.observableArrayList("Rule Bot", "GA Bot", "HC Bot", "ML Bot"));
    }
    


    /**
     * Updates the direction label.
     *
     * @param newVal the new direction value
     */
    private void updateDirection(Number newVal) {
        double[] directionVector = circularSlider.getDirectionVector();
        directionLabel.setText(String.format("Direction: [%.2f, %.2f]", directionVector[0], directionVector[1]));
        // System.out.println("Direction Vector: [" + directionVector[0] + ", " + directionVector[1] + "]");
    }

    /**
     * Updates the power label.
     *
     * @param newVal the new power value
     */
    private void updatePower(Number newVal) {
        powerLabel.setText(String.format("Power: %.2f", newVal.doubleValue()));
    }

    /**
     * Loads a new image for the map.
     */
    private void loadNewImage() {
        try {
            String userDir = System.getProperty("user.dir");
            File resourcesDir = new File(userDir, "src/main/resources");
            if (!resourcesDir.exists() || !resourcesDir.isDirectory()) {
                showAlert(Alert.AlertType.ERROR, "Load Failed", "The resources directory does not exist.");
                return;
            }

            File[] files = resourcesDir.listFiles();
            if (files == null) {
                showAlert(Alert.AlertType.ERROR, "Load Failed", "Failed to list files in the resources directory.");
                return;
            }

            for (File file : files) {
                if (file.isFile() && file.getName().equals("userInputMap.png")) {
                    String fileUrl = file.toURI().toURL().toExternalForm();
                    // System.out.println("Loading image from: " + fileUrl);
                    Image image = new Image(new FileInputStream(file));

                    if (image.isError()) {
                        System.err.println("Error loading image: " + image.getException());
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "Failed to load image: " + image.getException().getMessage()));
                        return;
                    }

                    mapImageView.setImage(image);
                    return;
                }
            }

            // System.err.println("Image file userInputMap.png does not exist in the resources directory.");
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "The image file userInputMap.png does not exist in the resources directory."));

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "An error occurred while loading the image: " + e.getMessage()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "File not found: " + e.getMessage()));
        }
    }

    /**
     * Draws the trajectory of the ball.
     *
     * @param gc           the graphics context
     * @param currentIndex the current index in the trajectory
     */
    private void drawTrajectory(GraphicsContext gc, int currentIndex) {
        gc.setFill(Color.RED);

        for (int i = 0; i <= currentIndex; i++) {
            double[] point = fullTrajectory.get(i);
            double x = Utility.coordinateToPixel_X(point[0]);
            double y = Utility.coordinateToPixel_Y(point[1]);
            gc.fillOval(x, y, 1, 1);
        }
    }

    /**
     * Draws the ball and the arrow indicating direction and power.
     */
    private void drawBallAndArrow() {
        if (ballCanvas != null) {
            GraphicsContext gc = ballCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());

            double ballDiameter = 0.1 * Utility.ratio;
            double ballRadius = ballDiameter / 2.0;

            double ballX = Utility.coordinateToPixel_X(BallPosition[0]) - ballRadius;
            double ballY = Utility.coordinateToPixel_Y(BallPosition[1]) - ballRadius;

            gc.setFill(Color.WHITE);
            gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);

            double[] directionVector = circularSlider.getDirectionVector();
            double arrowLength = powerSlider.getValue() * 5;
            double arrowX = ballX + ballRadius + directionVector[0] * arrowLength;
            double arrowY = ballY + ballRadius - directionVector[1] * arrowLength;

            gc.setStroke(Color.RED);
            gc.setLineWidth(1);

            // Draw the arrow shaft
            gc.strokeLine(ballX + ballRadius, ballY + ballRadius, arrowX, arrowY);

            // Draw the arrowhead
            drawArrowhead(gc, arrowX, arrowY, directionVector);

            updateBallPositionLabel();
        } else {
            System.err.println("ballCanvas is null");
        }
    }


    /**
     * Draws the arrowhead.
     *
     * @param gc        the graphics context
     * @param x         the x-coordinate of the arrowhead
     * @param y         the y-coordinate of the arrowhead
     * @param direction the direction vector
     */
    private void drawArrowhead(GraphicsContext gc, double x, double y, double[] direction) {
        double arrowHeadSize = 5;
        double angle = Math.atan2(-direction[1], direction[0]);

        double x1 = x - arrowHeadSize * Math.cos(angle - Math.PI / 6);
        double y1 = y - arrowHeadSize * Math.sin(angle - Math.PI / 6);
        double x2 = x - arrowHeadSize * Math.cos(angle + Math.PI / 6);
        double y2 = y - arrowHeadSize * Math.sin(angle + Math.PI / 6);

        gc.setFill(Color.RED);
        gc.fillPolygon(new double[]{x, x1, x2}, new double[]{y, y1, y2}, 3);
    }

    /**
     * Handles the hit button action.
     */
    @FXML
    private void hit() {
        if (this.ballMoving) {
            return;
        } else {
            ballHit(powerSlider.getValue(), circularSlider.getDirectionVector());
        }

    }

    private void ballHit(double power, double[] directionVector) {
        if (!REACHED_THE_HOLE) {
            // Clear the trajectory before each new hit
            fullTrajectory.clear();


            // System.out.println("Hit with power: " + power + ", direction: [" + directionVector[0] + ", " + directionVector[1] + "]");
            // System.out.println("StartBallPostion: " + BallPosition[0] + ", " + BallPosition[1]);

            // Call the engine to calculate the trajectory
            double[] x = {BallPosition[0], BallPosition[1], power * directionVector[0], power * directionVector[1]};
            ArrayList<double[]> xpath = this.golfGame.shoot(x, true);

            // Update ball position and shot count
            if (xpath != null && !xpath.isEmpty()) {
                fullTrajectory.addAll(xpath);  // Add new trajectory points to the full trajectory
                double[] finalPosition = xpath.get(xpath.size() - 1);
                BallPosition[0] = finalPosition[0];
                BallPosition[1] = finalPosition[1];
                shotCount++;
            }

            String shotLog = String.format(
                    "Shot %d: Hit to (%.2f, %.2f) with power %.2f.",
                    shotCount, BallPosition[0], BallPosition[1], power);
            logEvent(shotLog);
            updateShotCountLabel();
            // Animate the ball movement along the trajectory
            animateBallMovement(fullTrajectory, 0);

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Goal!");
            alert.setHeaderText("Goal Reached, The ball has already reached the hole.");
            alert.setContentText("Would you like to go back or see the stats?");

            ButtonType backButton = new ButtonType("Back");
            ButtonType seeStatsButton = new ButtonType("See the stat");
            ButtonType seeReplayButton = new ButtonType("See the replay");

            alert.getButtonTypes().setAll(backButton,seeReplayButton, seeStatsButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == backButton) {
                    Main mainInst = new Main();
                    mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
                } else if (result.get() == seeStatsButton) {
                    showStats();
                } else if(result.get() == seeReplayButton){
                    replay();
                }
            }
        }
    }

    private void ballHitMultiple(int step) {
        if (!REACHED_THE_HOLE) {
            shotCount = 0;
            double[] currentShot=shots.get(step).clone();
            // Clear the trajectory before each new hit
            fullTrajectory.clear();

            // Call the engine to calculate the trajectory
            
            ArrayList<double[]> xpath = this.golfGame.shoot(currentShot, true);

            // Update ball position and shot count
            if (xpath != null && !xpath.isEmpty()) {
                fullTrajectory.addAll(xpath);  // Add new trajectory points to the full trajectory
                double[] finalPosition = xpath.get(xpath.size() - 1).clone();
                BallPosition[0] = finalPosition[0];
                BallPosition[1] = finalPosition[1];
                shotCount++;
            }

            String shotLog = String.format(
                    "Shot %d: Hit to (%.2f, %.2f) with power %.2f.",
                    shotCount, BallPosition[0], BallPosition[1], Utility.getPowerFromVelocity(shots.get(step)));
            logEvent(shotLog);
            updateShotCountLabel();
            // Animate the ball movement along the trajectory
            animateBallMovement(fullTrajectory, step);

        } else {
            System.out.println("Number of shots: "+shotCount);
            playMusic("/music/goalSound.mp3");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Goal!");
            alert.setHeaderText("Goal Reached, The ball has already reached the hole.");
            alert.setContentText("Would you like to go back or see the stats?");

            ButtonType backButton = new ButtonType("Back");
            ButtonType seeReplayButton = new ButtonType("See the replay");
            ButtonType seeStatsButton = new ButtonType("See the stat");

            alert.getButtonTypes().setAll(backButton,seeReplayButton, seeStatsButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == backButton) {
                    Main mainInst = new Main();
                    mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
                } else if (result.get() == seeStatsButton) {
                    showStats();
                } else if (result.get() == seeReplayButton) {
                    replay();
                }
            }
        }
    }

    /**
     * Animates the ball movement along the trajectory.
     *
     * @param trajectory the trajectory of the ball
     * @param step      the power of the hit
     */
    public void animateBallMovement(ArrayList<double[]> trajectory, int step) {
        timeline = new Timeline();

        double duration = 5;
        this.ballMoving = true;
        for (int i = 0; i < trajectory.size(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(duration * i), event -> {
                double[] point = trajectory.get(index);
                // System.out.println(Arrays.toString(this.BallPosition));
                BallPosition[0] = point[0];
                BallPosition[1] = point[1];
                drawBallAndTrajectory(index);
                updateBallPositionLabel();
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setOnFinished(event -> {
            updateShotCountLabel();
            handlePostAnimation();
            this.ballMoving = false;
            if (shots!=null) {
                if (step<shots.size()-1) {
                    ballHitMultiple(step+1);
                }
            }
            
        });
        timeline.play();
    }

    /**
     * Handles post-animation events.
     */
    private void handlePostAnimation() {
        try {
            String message = golfGame.getMessage();
            if (golfGame.getTreeHit()) {
                logEvent("** The ball hit a tree and bounced off **");
            }

            if (message.contains("Water") && !this.botActivated) {
                if(!this.botActivated){
                    showAlert(Alert.AlertType.INFORMATION, "Ball in Water", "The ball landed in water.");
                }
                logEvent("!!--The ball landed in water--!!");
            } else if (golfGame.isGoal()) {
                this.REACHED_THE_HOLE = true;
                if(this.ruleBasedBot){
                    logEvent("The rule based bot reached the hole.");
                    // this.ruleBasedBot = false;

                    }
                logEvent("CONGRATULATIONS! The ball reached the hole.");
                showGoalAlert();
            }
        } catch (Exception e) {
            // System.out.println("Error in message");
        }
    }

    /**
     * Moves the ball to the end of the trajectory.
     */
    private void moveBallToEndOfTrajectory() {
        if (!fullTrajectory.isEmpty()) {
            double[] finalPosition = fullTrajectory.get(fullTrajectory.size() - 1);
            BallPosition[0] = finalPosition[0];
            BallPosition[1] = finalPosition[1];
            drawFullTrajectory();
            updateBallPositionLabel();
        }
    }

    /**
     * Draws the ball and the trajectory.
     *
     * @param currentIndex the current index in the trajectory
     */
    private void drawBallAndTrajectory(int currentIndex) {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());
        drawTrajectory(gc, currentIndex);

        double ballDiameter = 0.1 * Utility.ratio;
        double ballRadius = ballDiameter / 2.0;

        double ballX = Utility.coordinateToPixel_X(BallPosition[0]) - ballRadius;
        double ballY = Utility.coordinateToPixel_Y(BallPosition[1]) - ballRadius;

        gc.setFill(Color.WHITE);
        gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);
    }

    /**
     * Draws the full trajectory.
     */
    private void drawFullTrajectory() {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());
        drawTrajectory(gc, fullTrajectory.size() - 1);
        drawBallOnly();
    }

    /**
     * Draws the ball only.
     */
    private void drawBallOnly() {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        double ballDiameter = 0.1 * Utility.ratio;
        double ballRadius = ballDiameter / 2.0;

        double ballX = Utility.coordinateToPixel_X(BallPosition[0]) - ballRadius;
        double ballY = Utility.coordinateToPixel_Y(BallPosition[1]) - ballRadius;

        gc.setFill(Color.WHITE);
        gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);
    }

    /**
     * Logs an event message.
     *
     * @param message the message to log
     */
    private void logEvent(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
        });
    }

    /**
     * Handles the go back button action.
     */
    @FXML
    private void goBack() {
        if (REACHED_THE_HOLE) {
            Main mainInst = new Main();
            mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Retreat");
            alert.setHeaderText("Are you sure you want to go back?");
            alert.setContentText("All progress will be lost! \n\"Opportunities multiply as they are seized.\" – Sun Tzu, The Art of War");
            ButtonType backButton = new ButtonType("I have seen enough, take me back!");
            ButtonType stayButton = new ButtonType("Give me one more chance, captain!");


            alert.getButtonTypes().setAll(backButton, stayButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == backButton) {
                    Main mainInst = new Main();
                    mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
                } else if (result.get() == stayButton) {
                    alert.close();
                }
            }
        }


    }

    /**
     * Updates the ball position label.
     */
    private void updateBallPositionLabel() {
        ballPositionLabel.setText(String.format("Ball Position: (%.2f, %.2f)", BallPosition[0], BallPosition[1]));
    }

    /**
     * Updates the shot count label.
     */
    private void updateShotCountLabel() {
        shotCountLabel.setText("Shots: " + shotCount);
    }

    /**
     * Shows an alert with the given parameters.
     *
     * @param alertType the type of alert
     * @param title     the title of the alert
     * @param message   the message of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows a goal alert when the ball reaches the hole.
     */
    private void showGoalAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Goal!");
            alert.setHeaderText("CONGRATULATIONS! The ball reached the hole.");
            alert.setContentText("Would you like to go back or see the stats?");

            ButtonType backButton = new ButtonType("Back");
            ButtonType seeStatsButton = new ButtonType("See the stat");
    
            ButtonType seeReplayButton = new ButtonType("See the replay");
            if(this.ruleBasedBot){
                this.ruleBasedBot = false;
                alert.getButtonTypes().setAll(backButton, seeStatsButton);
            }else{
                alert.getButtonTypes().setAll(backButton, seeReplayButton, seeStatsButton);

                }

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == backButton) {
                    Main mainInst = new Main();
                    mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
                } else if (result.get() == seeStatsButton) {
                    showStats();
                } else if(result.get() == seeReplayButton){
                    replay();
                }
            }
        });
    }

    /**
     * Shows the statistics of the game.
     */
    private void showStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Total Shots: ").append(shotCount).append("\n");
        stats.append("Game Log:\n");
        stats.append(logTextArea.getText());

        logEvent("Showing stats.");

        showAlert(Alert.AlertType.INFORMATION, "Stats:", stats.toString());
    }

    /**
     * Rule-based bot plays the game.
     */
    // private void ruleBotPlay() {
    //     if(!ruleBasedBot){
    //         logEvent("!!--Rule-based bot entered the party--!!");
    //     }
    //     // this.shots = new ArrayList<>();

    //     this.ruleBasedBot = true;
    //     double[] ballP = BallPosition.clone();
    //     plays = distanceMeasure.playGame(ballP, HolePostion, REACHED_THE_HOLE);
    //     if (!plays.isEmpty()) {
    //         playBotShot(plays, 0);
    //     } 
    //     // ballHitMultiple(0);
    // }

    private void ruleBotPlay() {
        if(!ruleBasedBot){
            logEvent("!!--Rule-based bot entered the party--!!");
        }
        initPosit = BallPosition.clone();
        int i = 0;
        int currentShots = 0;
        int totalShots = 0;
        double currentDuration = 0;
        double totalDuration = 0;
        int succes = 0;
        // this.shots = new ArrayList<>();
        while(i<10){
            System.out.println(initPosit[0]+" "+initPosit[1]);
            System.out.println("Iteration: "+i);
            DistanceMeasure distanceMeasureT = new DistanceMeasure(initPosit, a, HolePostion, radiusHole, REACHED_THE_HOLE);
            plays = distanceMeasureT.playGame(initPosit, HolePostion, REACHED_THE_HOLE);
            currentShots = plays.size();
            currentDuration = distanceMeasureT.getDuration();
            totalDuration += currentDuration;
            totalShots += currentShots;
            if (distanceMeasureT.isGoal()) {
                succes++;
            }
            i++;
        }
        double averageShots = (double) totalShots / 10;
        double averageDuration = (double) totalDuration / 10;
        double successRate = (double) succes / 10 *100;
        System.out.println("-------------------------------");
        System.out.println("\nTotal shots: " + totalShots);
        System.out.println("Average shots per game: " + averageShots);
        System.out.println("Total duration: " + totalDuration+" seconds");
        System.out.println("Average duration per game: " + averageDuration+" seconds");
        System.out.println("Success rate: " + successRate+"%");
    
    }
    
    
    /**
     * Rule-based bot method to animate the shoot.
     */
    private void playBotShot(ArrayList<double[]> plays, int index) {
        if (index < plays.size()) {
            double[] play = plays.get(index);
            // double[] x ={play[0]*play[2], play[1]*play[2]};
            // this.shots.add(x.clone());
            // System.out.println(Arrays.toString(play));
            ballHit(play[2], new double[]{play[0], play[1]});


            timeline.setOnFinished(event -> {
                Platform.runLater(() -> {
                    try{
                        
                        playBotShot(plays, index + 1);
                        ruleBot.fire(); 

                    } catch (Exception e) {
                        this.ruleBasedBot = true;
                        handlePostAnimation();
                        
                    }

                });
            });
        } else {
            // 
        }
    }
    

    // @FXML
    // private void gaBotFunc() {
    //     // logEvent("!!--GA bot entered the party (it is slow, be patient)--!!");
    //     // AiBotGA gaBot = new AiBotGA(golfGame, HolePostion);
    //     // double[] x = {BallPosition[0], BallPosition[1], 0, 0};
    //     // gaBot.golfBot(x);
    //     // double[] solution = gaBot.getBest();
    //     // double[] velocity = {solution[2], solution[3]};
    //     // ballHit(Utility.getPowerFromVelocity(velocity), Utility.getDirectionFromVelocity(velocity));

    //     logEvent("!!--GA bot entered the party (it is slow, be patient)--!!");
    //     // AiBotMultiShots gaBot = new AiBotMultiShots(golfGame);
    //     // double[] x = {BallPosition[0], BallPosition[1], 0, 0};
    //     // shots=gaBot.golfBot(x);
    //     // ballHitMultiple(0);
        
    //     AiBotGAV gaBot = new AiBotGAV(golfGame);
    //     double[] x = {BallPosition[0], BallPosition[1], 0, 0};
    //     shots=gaBot.golfBot(x);
    //     ballHitMultiple(0);

    //     // ArrayList<double[]> test=new ArrayList<>();
    //     // double[] t={-3,0,0,2};
    //     // test.add(t.clone());
    //     // t=new double[]{-3,5,2,0};
    //     // test.add(t.clone());
    //     // t=new double[]{-3,5,2,2};
    //     // test.add(t.clone());
    //     // shots=test;
    //     // ballHit(0);
    // }

    // @FXML
    // private void gaBotFunc() {
    //     playMusic("/music/elevator-music-vanoss-gaming-background-music.mp3");
    //     logEvent("!!--GA bot entered the party (it is slow, be patient)--!!");

    //     Task<ArrayList<double[]>> task = new Task<>() {
    //         @Override
    //         protected ArrayList<double[]> call() {
    //             // AiBotMultiShots gaBot = new AiBotMultiShots(golfGame);
    //             AiBotGAV gaBot = new AiBotGAV(golfGame);
    //             double[] x = {BallPosition[0], BallPosition[1], 0, 0};
    //             return gaBot.golfBot(x);
    //         }

    //         @Override
    //         protected void succeeded() {
    //             stopMusic();
    //             ArrayList<double[]> velocities = getValue();
    //             shots = velocities;
    //             ballHitMultiple(0);
    //         }

    //         @Override
    //         protected void failed() {
    //             stopMusic();
    //             Throwable exception = getException();
    //             exception.printStackTrace();
    //         }
    //     };

    //     new Thread(task).start();
    // }

    @FXML
    private void gaBotFunc() {
        // playMusic("/music/elevator-music-vanoss-gaming-background-music.mp3");
        logEvent("!!--GA bot entered the party (it is slow, be patient)--!!");
        initPosit = BallPosition.clone();
        int i = 0;
        int currentShots = 0;
        int totalShots = 0;
        double currentDuration = 0;
        double totalDuration = 0;
        int succes = 0;
        while(i<10){
            System.out.println(initPosit[0]+" "+initPosit[1]);
            System.out.println("Iteration: "+i);
            AiBotGA gaBot = new AiBotGA(golfGame);
            double[] x = {initPosit[0], initPosit[1], 0, 0};
            ArrayList<double[]> velocities =gaBot.golfBot(x);
            currentShots = velocities.size();
            currentDuration = gaBot.getDuration();
            totalShots += currentShots;
            totalDuration += currentDuration;
            if (gaBot.isGoal()) {
                succes++;
            }
            i++;
        }
        double averageShots = (double) totalShots / 10;
        double averageDuration = (double) totalDuration / 10;
        double successRate = (double) succes / 10 *100;
        System.out.println("-------------------------------");
        System.out.println("\nTotal shots: " + totalShots);
        System.out.println("Average shots per game: " + averageShots);
        System.out.println("Total duration: " + totalDuration+" seconds");
        System.out.println("Average duration per game: " + averageDuration+" seconds");
        System.out.println("Success rate: " + successRate+"%");
    
    }


    /**
     * Handles the Play button action for selected bot.
     */
    @FXML
    private void playBot() {
        String selectedBot = botSelect.getValue();
        this.botActivated=true;
        if (selectedBot != null) {
            switch (selectedBot) {
                case "Rule Bot":
                    ruleBotPlay();
                    break;
                case "GA Bot":
                    gaBotFunc();
                    break;
                case "HC Bot":
                    hcBotPlay();
                    break;
                case "ML Bot":
                    mlBotPlay();
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Bot Selection Error", "Invalid bot selected.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Bot Selection Error", "Please select a bot.");
        }
    }

    // @FXML
    // private void hcBotPlay() {
        // playMusic("/music/elevator-music-vanoss-gaming-background-music.mp3");
    //     logEvent("!!--HC bot entered the party (it is slow, be patient)--!!");

    //         Task<ArrayList<double[]>> task = new Task<>() {
    //             @Override
    //             protected ArrayList<double[]> call() {
    //                 HillClimbingBotNEW chBot = new HillClimbingBotNEW(golfGame, initPosit, HolePostion, "src/main/resources/userInputMap.png", radiusHole);
    //                 return chBot.hillClimbingAlgorithm();
    //             }
    
    //             @Override
    //             protected void succeeded() {
    //                 stopMusic();
    //                 ArrayList<double[]> velocities = getValue();
    //                 shots = velocities;
    //                 ballHitMultiple(0);
    //             }
    
    //             @Override
    //             protected void failed() {
    //                 stopMusic();
    //                 Throwable exception = getException();
    //                 exception.printStackTrace();
    //             }
    //         };
    
    //         new Thread(task).start();

    // }



    @FXML
    private void hcBotPlay() {
        // playMusic("/music/elevator-music-vanoss-gaming-background-music.mp3");
        logEvent("!!--HC bot entered the party (it is slow, be patient)--!!");
        initPosit = BallPosition.clone();
        int i = 0;
        int currentShots = 0;
        int totalShots = 0;
        double currentDuration = 0;
        double totalDuration = 0;
        int succes = 0;
        while(i<10){
            System.out.println(initPosit[0]+" "+initPosit[1]);
            System.out.println("Iteration: "+i);
            HillClimbingBotNEW chBot = new HillClimbingBotNEW(golfGame, initPosit, HolePostion, "src/main/resources/userInputMap.png", radiusHole);
            ArrayList<double[]> velocities =chBot.hillClimbingAlgorithm(); 
            currentDuration = chBot.getDuration();
            
            // shots = velocities;
            currentShots = velocities.size();
            // ballHitMultiple(0);
            totalShots += currentShots;
            totalDuration += currentDuration;
            if (chBot.isGoal()) {
                succes++;
            }

            i++;
        }
        double averageShots = (double) totalShots / 10;
        double averageDuration = (double) totalDuration / 10;
        double successRate = (double) succes / 10 *100;
        System.out.println("-------------------------------");
        System.out.println("\nTotal shots: " + totalShots);
        System.out.println("Average shots per game: " + averageShots);
        System.out.println("Total duration: " + totalDuration+" seconds");
        System.out.println("Average duration per game: " + averageDuration+" seconds");
        System.out.println("Success rate: " + successRate+"%");
    }

    @FXML
    private void replay(){
        // shots = replayShots;
        // if (shots == null || shots.isEmpty()) {
        //     this.ruleBasedBot = false;

        //     return;
        // }
        this.REACHED_THE_HOLE=false;
        ballHitMultiple(0);
    }

    @FXML
    private void mlBotPlay(){
        return;
    }

    private void playMusic(String path) {
    // try {
        // String musicFile = "/music/elevator-music-vanoss-gaming-background-music.mp3";
        URL resource = getClass().getResource(path);
        Media sound = new Media(resource.toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setVolume(1.0);
        // mediaPlayer.setAutoPlay(true);

        mediaPlayer.play();
    // } catch (Exception e) {
    //     e.printStackTrace();
    // }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }


    @FXML
    private void nrBotPlay(){
        logEvent("!!--NR bot entered the party (it is slow, be patient)--!!");
        NewtonRaphsonBot nrBot = new NewtonRaphsonBot(golfGame, BallPosition, HolePostion);
        ArrayList<double[]> velocities = nrBot.NewtonRaphsonMethod();
        shots = velocities;
        ballHitMultiple(0);
        return;
    }
    
    
}


