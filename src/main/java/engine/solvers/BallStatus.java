package engine.solvers;

/**
 * This enum contains all the possible states the ball can be in.
 */
public enum BallStatus{
    /**
     * This status indicates the goal has been reached
     */
    Goal,
    /**
     * This status indicated that the ball has hit water.
     */
    HitWater,
    /**
     * This status indicates that everything is working normally.
     */
    Normal,
    /**
     * This status indicates that the ball has gone out of bounds.
     */
    OutOfBoundary,
}
