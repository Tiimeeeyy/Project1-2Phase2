package engine.bot.ml_bot.agent;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

public class State implements Serializable {
    private RealVector currentPosition;


    public State(RealVector currentPosition) {
        this.currentPosition = currentPosition;
    }

    public RealVector getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(RealVector currentPosition) {
        this.currentPosition = currentPosition;
    }
}
