package org.hzt.ant;

import java.util.ArrayList;
import java.util.List;

/**
 * org.hzt.ant.Ant class - represent ants in an ant simulation using a Finite org.hzt.ant.State Machine
 */
public final class Ant {
	private final int home; // represents an ant's home ant hill location
	private int current; // represents an ant's current location
	private List<Action> actions; // represents the actions that an ant has in the queue

	public Ant(final int home, final int current) {
		this.home = home;
		this.current = current;
		this.actions = new ArrayList<>();
	}
	
	public int getHome() {
		return this.home;
	}
	
	public void setCurrent(final int current) {
		this.current = current;
	}

	public int getCurrent() {
		return this.current;
	}

	public void setActions(final List<Action> actions) {
		this.actions = actions;
	}

	public List<Action> getActions() {
		return this.actions;
	}
}
