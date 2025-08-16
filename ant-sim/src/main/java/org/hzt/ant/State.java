package org.hzt.ant;

/*
 * org.hzt.ant.State class - used with the Finite org.hzt.ant.State Machine, represents the state of an active object
 */
public class State {
	private String action;// represents the action associated with the state
	private String entryAction;// represents the entryAction associated with the state
	private String exitAction;// represents the exitAction associated with the state
	private Transition[] transitions;// represents the transitions associated with the state
	
	// org.hzt.ant.State constructor - sets the action, entryAction, exitAvtion, and transitions of the instance
	public State(String action, String entryAction, String exitAction, Transition[] transitions) {
		this.action = action;// sets the action to that specified by the user
		this.entryAction = entryAction;// sets the entryAction to that specified by the user
		this.exitAction = exitAction;// sets the exitAction to that specified by the user
		this.transitions = transitions;// sets the transitions to those specified by the user
	}// org.hzt.ant.State(String, String, String, org.hzt.ant.Transition[]) constructor
	
	// getAction method - allows the action of the org.hzt.ant.State to be accessed
	public String getAction() {
		return this.action;// returns the action
	}// getAction() method
	
	// getEntryAction method - allows the entryAction of the org.hzt.ant.State to be accessed
	public String getEntryAction() {
		return this.entryAction;// returns the entryAction
	}// getEntryAction() method
	
	// getExitAction method - allows the exitAction of the org.hzt.ant.State to be accessed
	public String getExitAction() {
		return this.exitAction;// returns the exitAction
	}// getExitAction() method
	
	// getTransition method - allows a specific transition of the org.hzt.ant.State to be accessed
		public Transition getTransition(Transition transition) {
			for (Transition trans : transitions) {// loops through transitions
				if (trans.getAction().equals(transition.getAction()));// checks for the specified transition
				return trans;// returns the transitions
			}// for (transitions)
			return null;// if no suitable transition is found
		}// getTransition(org.hzt.ant.Transition) method
	
	// getTransitions method - allows the transitions of the org.hzt.ant.State to be accessed
	public Transition[] getTransitions() {
		return this.transitions;// returns the transitions
	}// getTransitions() method
	
	// toString method - overwrites the method to format the object for printing
	public String toString() {
		return this.getClass() + " " + action + " " + entryAction + " " + exitAction;// returns the formatted String
	}// toString() method
}// org.hzt.ant.State class