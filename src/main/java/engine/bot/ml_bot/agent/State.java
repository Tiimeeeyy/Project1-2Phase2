package engine.bot.ml_bot.agent;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * The state class represents the state the system is in.
 */
public class State implements Serializable {
    private transient RealVector currentPosition;

    /**
     * Class constructor.
     * @param currentPosition The position representing the state.
     */
    public State(RealVector currentPosition) {
        this.currentPosition = currentPosition;
    }


    /**
     * Gets the current position.
     * @return The current position as a Real Vector.
     */
    public RealVector getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Sets the current position for a State object.
     * @param currentPosition The position to be set.
     */
    public void setCurrentPosition(RealVector currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Converts a Vector into a State object.
     * @param position The Vector holding the current position.
     * @return The State object containing that position.
     */
    public State toStateVector(RealVector position) {
        return new State(position);
    }

    /**
     * Converts a double array into a State object.
     * @param position The position to be converted.
     * @return The State based on the position.
     */
    public State toStateArray(double[] position) {
        return new State(new ArrayRealVector(position));
    }
}
