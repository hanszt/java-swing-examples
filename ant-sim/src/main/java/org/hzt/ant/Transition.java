package org.hzt.ant;

public final class Transition {
	private State targetState;
	private final Action action;
	private boolean active;
	
	public Transition(final Action action, final boolean active) {
		this.targetState = null;
		this.action = action;
		this.active = active;
	}

	public void setActive(final boolean condition) {
		this.active = condition;
	}

	public boolean isActive() {
		return active;
	}

	public void setTargetState(final State targetState) {
		this.targetState = targetState;
	}

	public State getTargetState() {
		return this.targetState;
	}

	public Action getAction() {
		return this.action;
	}

	public String toString() {
		return this.getClass() + " " + targetState + " " + action + " " + active;// returns the formatted result
	}
}
