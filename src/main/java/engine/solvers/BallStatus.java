package engine.solvers;

/**
 * This enum contains all the possible states the ball can be in.
 */
public enum BallStatus{
    Goal, // If the ball has reached the goal.
    HitWater, // If the ball has hit the water.
    Normal, // If everything works normally.
    OutOfBoundary, // If the ball goes out of bounds.
}
