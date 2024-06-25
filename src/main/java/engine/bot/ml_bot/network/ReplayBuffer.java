package engine.bot.ml_bot.network;

import engine.bot.ml_bot.agent.Action;
import engine.bot.ml_bot.agent.State;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to save the Experiences of the Artificial neural network.
 */
public class ReplayBuffer implements Serializable {
    private static final int MAX_SIZE = 60;
    private static List<Experience> experiences = new ArrayList<>();

    /**
     * Creates a random batch of experiences out of the List in memory.
     *
     * @param batchSize The size of the batch.
     * @return A list containing random experiences from the list in memory.
     */
    public static List<Experience> sampleBatch(int batchSize) {

        batchSize = Math.min(batchSize, experiences.size());

        List<Experience> batch = new ArrayList<>();

        Collections.shuffle(experiences);

        for (int i = 0; i < batchSize; i++) {
            batch.add(experiences.get(i));
        }
        return batch;
    }

    /**
     * Adds an experience to the list ("to memory")
     *
     * @param experience The experience to be added.
     */
    public void addExperience(Experience experience) {
        if (experiences.size() >= MAX_SIZE) {
            experiences.removeFirst();
        }
        experiences.add(experience);
    }
    public static List<Experience> getAllExperiences() {
        return new ArrayList<>(experiences);
    }

    // Nested static class to make sure the objects are immutable.
    public static class Experience implements Serializable {
        public State state;
        public Action action;
        public double reward;
        public State nextState;

        public Experience(State state, Action action, double reward, State nextState) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
        }

        /*
         *      _                    ____        _ _                 _       _
         *     | | __ ___   ____ _  | __ )  ___ (_) | ___ _ __ _ __ | | __ _| |_ ___
         *  _  | |/ _` \ \ / / _` | |  _ \ / _ \| | |/ _ \ '__| '_ \| |/ _` | __/ _ \
         * | |_| | (_| |\ V / (_| | | |_) | (_) | | |  __/ |  | |_) | | (_| | ||  __/
         *  \___/ \__,_| \_/ \__,_| |____/ \___/|_|_|\___|_|  | .__/|_|\__,_|\__\___|
         *                                                    |_|
         */
        public Action getAction() {
            return action;
        }

        public double getReward() {
            return reward;
        }

        public State getNextState() {
            return nextState;
        }

        public State getState() {
            return state;
        }
    }


}


