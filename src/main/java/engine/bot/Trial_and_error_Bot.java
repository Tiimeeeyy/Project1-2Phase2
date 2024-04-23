package engine.bot;

// Import the RK4 Solver
/**
 * The Trial and Error bot
 */

import engine.solvers.RK4;

/**
 * Notes:
 * If no Obstacle -> velocity straight to the hole
 * Else -> Check 30-40° from the initial point for collisions
 * Then -> Go 30-40° from the point with velocity 5 m/s
 * If collision is not avoidable -> run it and calculate the collision
 * Maybe: If collision can be used as an advantage
 */

public class Trial_and_error_Bot implements BotInterface {
    double gravity = g;
    private RK4 rk4;

    public Trial_and_error_Bot() {
        this.rk4 = new RK4();
    }
}
