package pack;

import java.util.ArrayList;
import java.util.HashMap;

public class WTable implements Experience {

	private final double INITIAL_WVALUE;

	private static final long serialVersionUID = 9061616761656712940L;

	private HashMap<Integer, HashMap<Integer, Integer>> afterstateTable = new HashMap<>();
	private HashMap<Integer, Double> wTable = new HashMap<>();

	public WTable(double initialWValue) {
		this.INITIAL_WVALUE = initialWValue;
	}

	/**
	 * If no afterstateTable entry for the state exists, this method creates one and
	 * maps all state-action tuple to their respective afterstate If the calculated
	 * afterstates are not already in the wTable they are initialised with the
	 * default qValue
	 * 
	 * @param state        that the action is currently in, state will be added to
	 *                     afterstateTable
	 * @param legalActions actions that are legal in the state, if legalActions is
	 *                     null a terminal state is assumed
	 */
	@Override
	public void initialiseQTableEntryIfNotExistent(int state, int[] legalActions) {

		// if state has not been initialised
		if (this.afterstateTable.get(state) == null) {
			initialiseAfterstateAndWTable(state, legalActions);
		}
	}

	/**
	 * Initialise afterstateTable and wTable entries for the given state and all of
	 * the reachable afterstates. All state-action tuple are mapped to their
	 * respective afterstate If the calculated afterstates are not already in the
	 * wTable they are initialised with the default qValue
	 * 
	 * @param state
	 * @param legalActions
	 */
	private void initialiseAfterstateAndWTable(int state, int[] legalActions) {

		// if terminal state
		if (legalActions.length == 0) {
			// terminal state, add to afterstate directly
			this.wTable.put(state, QVALUE_TERMINAL_STATE);

		} else {
			// if not a terminalstate iterate over actions
			HashMap<Integer, Integer> actionAfterStateMap = new HashMap<>();

			for (int legalAction : legalActions) {

				// get the resulting afterstate and add it to the SA -> Y map
				int afterstate = Gamefield.applyAction(state, legalAction);
				actionAfterStateMap.put(legalAction, afterstate);

				// if the afterstate has not been initialised yet initialise it
				if (this.wTable.get(afterstate) == null) {
					this.wTable.put(afterstate, this.INITIAL_WVALUE);
				}
			}
			// save the afterStateMap for the current state
			this.afterstateTable.put(state, actionAfterStateMap);
		}

	}

	/**
	 * Update the wTable entry for the given state-action tuple. The afterstate
	 * value is derived using the state-action tuple and the afterstateTable
	 * 
	 * @param state     of the state-action tuple to be updated
	 * @param action    of the state-action tuple to be updated
	 * @param newQValue new new qvalue of the state-action tuple
	 */
	@Override
	public void updateQTableValue(int state, int action, double newQValue) {
		int afterState = this.afterstateTable.get(state).get(action);
		this.wTable.put(afterState, newQValue);
	}

	/**
	 * Get the best action out of the set of legal actions for the given state. Best
	 * action means the state-action tuple that results in the afterstate with the
	 * highest wValue, ties are broken arbitrarily
	 * 
	 * @param state        for that the best action is to be given
	 * @param legalActions set of actions that are legal in this state
	 * @return action with the highest wValue
	 */
	@Override
	public int getBestAction(int state, int[] legalActions) {

		if (legalActions.length == 0) {
			throw new IllegalArgumentException(
					"empty array was passed thus no optimal action available; method is not applicable for terminal states");
		}
		ArrayList<Integer> bestActions = this.getBestActions(state, legalActions);
		int bestAction;

		// if there are multiple actions with an equally high qValue one is selected
		// arbitrarily
		if (bestActions.size() > 1) {
			bestAction = Utility.getRandomElement(bestActions);
		} else {
			bestAction = bestActions.get(0);
		}
		return bestAction;
	}

	/**
	 * Returns one or more actions out of the set of legal actions with the highest
	 * that result in the afterstate with the highest wValue
	 * 
	 * @param state        for that the best action is to be given
	 * @param legalActions set of actions that are legal in this state
	 * @return one or more actions out of legalActions with the highest wValue
	 */
	public ArrayList<Integer> getBestActions(int state, int[] legalActions) {
		// iterate through legalACtions and make calls to wTable passing
		// afterstateTable-Entry

		if (legalActions.length == 0) {
			throw new IllegalArgumentException(
					"empty array was passed thus no optimal action available; method is not applicable for terminal states");
		}

		HashMap<Integer, Integer> actionAfterStateMap = this.afterstateTable.get(state);

		// No null check necessary as state and its respective afterstates must be
		// initialised already

		ArrayList<Integer> bestActions = new ArrayList<>();
		int firstAvailableAfterstate = actionAfterStateMap.get(legalActions[0]);

		double bestQValue = this.wTable.get(firstAvailableAfterstate);

		for (int legalAction : legalActions) {
			int afterState = actionAfterStateMap.get(legalAction);
			double qValueOfAfterstate = this.wTable.get(afterState);

			if (qValueOfAfterstate == bestQValue) {
				bestActions.add(legalAction);
			} else if (qValueOfAfterstate > bestQValue) {
				bestActions.clear();
				bestActions.add(legalAction);
				bestQValue = qValueOfAfterstate;
			}

		}
		return bestActions;

	}

	/**
	 * Returns the qValue that is assigned to terminal states, this is 0 by
	 * definition
	 */
	@Override
	public double getQValueOfTerminalState() {
		return Experience.QVALUE_TERMINAL_STATE;
	}

	/**
	 * Returns the qValue for the given state-action tuple by calculating the
	 * resulting afterstate and looking up its value in the wTable
	 */
	@Override
	public double getQValue(int state, int action) {
		int afterState = this.afterstateTable.get(state).get(action);
		double qValueOfAfterstate = this.wTable.get(afterState);
		return qValueOfAfterstate;
	}

	/**
	 * Returns the number of distinct states the agent has visited so far, i.e. the
	 * number of states with an entry in the afterstateTable/qTable.
	 * 
	 * @return number of distinct states the agent has visited
	 */
	@Override
	public int getNumberOfDistinctVisitedStates() {
		return this.afterstateTable.size();
	}

	/**
	 * Returns the number of entries inside the wTable table, i.e. the table mapping
	 * afterstates to their respective wValue that is analogous to the qValue except
	 * that multiple SA tuples can cause the same afterstate. wTable is thus a
	 * reduction or compression of SA tuples
	 * 
	 * Since only afterstates are used as keys in wTable the maximal return value is
	 * 5477 thus being one entry shy off the 5478 total states in tic-tac-toe. This
	 * is because the root state (0) is not an afterstate and thus not included
	 */
	public int getNumberOfInitialisedAfterstates() {
		return this.wTable.size();
	}

	/**
	 * Returns the HashMap that contains all legalActions mapped to their respective
	 * qValue for the given state.
	 * 
	 * This method is used to analyse the perception the agent has on the given
	 * state. This method is not contained in the interface as its signature differs
	 * from the one used in QTable and both methods are not used during training
	 * 
	 * @param state        for that the action-qValue map is to be returned
	 * @param legalActions actions that are available to the agent in the given
	 *                     state
	 * @return action-qValue map for the given state
	 */
	@Override
	public HashMap<Integer, Double> getActionQValueMapForState(int state) {
		int[] legalActions = Gamefield.getlegalActionsToState(state);

		if (legalActions.length == 0) {
			throw new IllegalArgumentException(
					"empty array thus terminal state is assumed; every action in a terminal state has qValue of 0 by definition");
		}

		HashMap<Integer, Double> actionQValueMap = new HashMap<>();

		for (int legalAction : legalActions) {
			int afterstate = this.afterstateTable.get(state).get(legalAction);
			double qValueOfAfterstate = this.wTable.get(afterstate);
			// double entries can not occur since no two legal actions of the same state
			// can cause the same resulting afterstate
			actionQValueMap.put(legalAction, qValueOfAfterstate);
		}

		return actionQValueMap;

	}

}
