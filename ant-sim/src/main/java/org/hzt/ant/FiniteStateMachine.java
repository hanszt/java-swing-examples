package org.hzt.ant;

import java.util.ArrayList;
import java.util.List;

public final class FiniteStateMachine {
    final List<State> states;
    final State initialState;
    State currentState;


    public FiniteStateMachine(final List<State> states, final State initialState) {
        this.states = states;
        this.initialState = initialState;
        this.currentState = initialState;
    }

    public List<Action> update() {
        Transition triggeredTransition = null;
        for (final var transition : currentState.transitions()) {
            if (transition.isActive()) {
                triggeredTransition = transition;
                break;
            }
        }
        final var actions = new ArrayList<Action>();
        if (triggeredTransition != null) {
            final var targetState = triggeredTransition.getTargetState();

            actions.add(currentState.exitAction());
            actions.add(triggeredTransition.getAction());
            actions.add(targetState.entryAction());

            currentState = targetState;
            return actions;
        }
        actions.add(currentState.action());
        return actions;
    }

    public State getCurrentState() {
        return currentState;
    }
}
