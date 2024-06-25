package engine.bot.ml_bot.agent;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * This class represents an action as an Object.
 */
public class Action implements Serializable {
    private RealVector action;

    public Action(RealVector action) {
        this.action = action;
    }

    public RealVector getAction() {
        return action;
    }

    public void setAction(RealVector action) {
        this.action = action;
    }
}
