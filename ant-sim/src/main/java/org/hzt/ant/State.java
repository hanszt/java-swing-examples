package org.hzt.ant;

import java.util.List;

/**
 * @param action      represents the action associated with the state
 * @param entryAction represents the entryAction associated with the state
 * @param exitAction  represents the exitAction associated with the state
 * @param transitions represents the transitions associated with the state
 */
public record State(Action action, Action entryAction, Action exitAction, List<Transition> transitions) {

    public Transition firstTransitionOrThrow() {
        if (!transitions.isEmpty()) {
            return transitions.getFirst();
        }
        throw new IllegalArgumentException("No transitions available for this state");
    }

    public String toString() {
        return this.getClass() + " " + action + " " + entryAction + " " + exitAction;
    }
}
