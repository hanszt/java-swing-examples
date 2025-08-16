package org.hzt.ant;

public final class Transition {
	private State targetState;// represents the state that this object transitions to
	private final Action action;// represents the action that is associated with the transition taking place
	private boolean active;// represents the condition that indicates the transition is activated
	
	// org.hzt.ant.Transition constructor - the action associated and the trigger condition
	public Transition(Action action, boolean active) {
		this.targetState = null;// initially null, this is added later
		this.action = action;// sets the action to that specified
		this.active = active;// sets the condition to that specified
	}// org.hzt.ant.Transition(String, boolean) constructor
	
	// setTriggered method - sets the trigger condition to that specified
	public void setActive(boolean condition) {
		this.active = condition;// sets the trigger condition
	}// setTriggered(boolean) method
	
	// isTriggered method - returns a result indicating whether the condition has been triggered
	public boolean isActive() {
		return active;// returns result
	}// isTriggered() method
	
	// setTargetState method - sets the target state to that specified
	public void setTargetState(State targetState) {
		this.targetState = targetState;// sets the targetState to that specified
	}// setTargetState(org.hzt.ant.State) method
	
	// getTargetState method - allows the targetState variable to be accessed
	public State getTargetState() {
		return this.targetState;// returns the targetState
	}// getTargetState() method
	
	// getAction method - allows the action variable to be accessed
	public Action getAction() {
		return this.action;// returns the action
	}// getAction() method
	
	// toString method - overwrites the method to format the object for printing
	public String toString() {
		return this.getClass() + " " + targetState + " " + action + " " + active;// returns the formatted result
	}
}
