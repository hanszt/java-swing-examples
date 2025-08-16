package org.hzt.ant;

/**
 * @param action      represents the action associated with the state
 * @param entryAction represents the entryAction associated with the state
 * @param exitAction  represents the exitAction associated with the state
 * @param transitions represents the transitions associated with the state
 */
public record State(String action, String entryAction, String exitAction, Transition[] transitions) {

    public Transition getTransition(Transition transition) {
        if (transitions.length > 0) {
            return transitions[0];
        }
        return null;
    }

    public String toString() {
        return this.getClass() + " " + action + " " + entryAction + " " + exitAction;
    }
}