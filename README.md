# Project 1–2 Phase 2

## Description
In this project,
the goal was to familiarize ourselves with numerical approximation methods for Ordinary Differential Equations.
Those equations were used to simulate a game called "Crazy Putting," which gave us the following tasks:
1. Solver to simulate the game.
2. GUI to graphically represent the game.
3. Bot, that plays the game automatically.

This presented us with many challenges, however, you can read about those in our report.

## How to use:

Make sure maven is installed and do a clean compile (`mvn clean compile`) of the project to make sure all components are working. 
After that use the main class located in `src/main/java/ui` to run the project.
In the screen that opens you are able to input all of your desired parameters, such as the height function, as well as the starting location for the ball and the hole location.
If you press next, you wil be transported to a screen in which you can "design" the map however you desire. You are able to select a surface and draw that surface on the map.
If you wish to play the game, hit the next button again, and you will be presented with the options to play. The Slider can increase or decrease the velocity and the circular slider can be used to change the direction. To hit the ball simply press hit.
If you wish to have the game played for you, select one of the bots and press their respective buttons to let them play the game.

## More information:

To view javadocs please navigate to:
`src/JavaDoc/apidocs`
directory and locate the `index.html` file and run it.

To view test results, please navigate to:
`target/surefire-reports`
directory and locate the test report you want to look at.

