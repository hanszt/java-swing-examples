package org.hzt.ant;

import java.util.ArrayList;
import java.util.List;

public final class FiniteStateMachine {
    final List<State> states;// represents the states used with the finite state machine
    final State initialState;// represents the initial state of the object using the state machine
    State currentState;// represents the current state of the object using the state machine

    // org.hzt.ant.FiniteStateMachine constructor - sets the states, the initial state, and the current state
    public FiniteStateMachine(final List<State> states, final State initialState) {
        this.states = states;// sets the states to those specified
        this.initialState = initialState;// sets the initial state to that specified
        this.currentState = initialState;// initially sets the current state to the initial state
    }

    public List<Action> update() {
        Transition triggeredTransition = null;// will hold the transition, if one is triggered
        final var actions = new ArrayList<Action>();// will hold the actions resulting from the update

        for (final var transition : currentState.transitions()) {
            if (transition.isActive()) {
                triggeredTransition = transition;
                break;
            }
        }

        if (triggeredTransition != null) {// checks to see if a transition was triggered
            final var targetState = triggeredTransition.getTargetState();// gets the target state of the triggered transition

            actions.add(currentState.exitAction());// adds the exit action of the current state to the list to be returned
            actions.add(triggeredTransition.getAction());// adds the action of the current state to the list to be returned
            actions.add(targetState.entryAction());// adds the entry action of the current state to the list to be returned

            currentState = targetState;// sets the target state of the triggered transition to the current state
            return actions;// returns the actions associated with the update
        }

        actions.add(currentState.action());// adds the action of the current state to the list returned
        return actions;// returns the actions
    }// update() method

    // getCurrentState method - allows the current state of the fsm to be accessed
    public State getCurrentState() {
        return currentState;// returns the currentState of the fsm
    }
}
