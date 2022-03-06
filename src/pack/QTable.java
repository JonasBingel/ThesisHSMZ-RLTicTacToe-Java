package pack;

import java.util.ArrayList;
import java.util.HashMap;

public class QTable implements Experience {

	private static final long serialVersionUID = -6442176524073724382L;
	private final double INITIAL_QVALUE;

	private HashMap<Integer, HashMap<Integer, Double>> qTable = new HashMap<>();

	public QTable(double initialQValue) {
		this.INITIAL_QVALUE = initialQValue;
	}

	/**
	 * If no qTable entry for the state exists, this method creates one and
	 * initialises every legalAction with the constant provided in the constructor
	 * 
	 * @param state        that is to be added to the qtable
	 * @param legalActions actions that are legal in the state, if legalActions is
	 *                     null a terminal state is assumed
	 */
	@Override
	public void initialiseQTableEntryIfNotExistent(int state, int[] legalActions) {
		if (this.qTable.get(state) == null) {
			this.initialiseQTableEntry(state, legalActions);
		}
	}

	/**
	 * Initialises the qTable entry for the given state and its legal actions. If
	 * legalActions is empty a terminal state is assumed and the constant
	 * QVALUE_TERMINAL_STATE is assigned as value and null is used a key
	 * 
	 * 
	 * @param state        that is to be added to the qtable
	 * @param legalActions actions that are legal in the state, if legalActions is
	 *                     null a terminal state is assumed
	 */
	private void initialiseQTableEntry(int state, int[] legalActions) {
		HashMap<Integer, Double> newInitializedQTableEntry = new HashMap<>();

		if (legalActions.length == 0) {
			// if no legal actions are available it is a terminal state
			newInitializedQTableEntry.put(null, QVALUE_TERMINAL_STATE);
		} else {
			for (int legalAction : legalActions) {
				newInitializedQTableEntry.put(legalAction, this.INITIAL_QVALUE);
			}
		}
		this.qTable.put(state, newInitializedQTableEntry);
	}

	/**
	 * Sets the qValue of the state-action tuple to the passed new qValue
	 * 
	 * @param state     of the state-action tuple to be updated
	 * @param action    of the state-action tuple to be updated
	 * @param newQValue new new qvalue of the state-action tuple
	 */
	@Override
	public void updateQTableValue(int state, int action, double newQValue) {

		HashMap<Integer, Double> updatedActionQValueMap = this.qTable.get(state);
		if (updatedActionQValueMap == null) {
			throw new NullPointerException(
					"state has not been initialised yet; According to the TD algorithm an update can not occur for these states ");
		} else {
			updatedActionQValueMap.put(action, newQValue);
			this.qTable.put(state, updatedActionQValueMap);
		}

	}

	/**
	 * Get the best action out of the set of legal actions for the given state. Best
	 * action means the state-action tuple that has the highest qValue, ties are
	 * broken arbitrarily
	 * 
	 * @param state        for that the best action is to be given
	 * @param legalActions set of actions that are legal in this state
	 * @return action with the highest qValue
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
	 * qValue
	 * 
	 * @param state        for that the best action is to be given
	 * @param legalActions set of actions that are legal in this state
	 * @return one or more actions out of legalActions with the highest qValue
	 */
	private ArrayList<Integer> getBestActions(int state, int[] legalActions) {

		if (legalActions.length == 0) {
			throw new IllegalArgumentException(
					"empty array was passed thus no optimal action available; method is not applicable for terminal states");
		}

		// No null check necessary as state must be initialised already
		HashMap<Integer, Double> actionValueMap = this.qTable.get(state);
		ArrayList<Integer> bestActions = new ArrayList<>();

		double bestQValue = actionValueMap.get(legalActions[0]);

		for (int legalAction : legalActions) {
			double qValueOfLegalAction = actionValueMap.get(legalAction);

			if (qValueOfLegalAction == bestQValue) {
				bestActions.add(legalAction);
			} else if (qValueOfLegalAction > bestQValue) {
				bestActions.clear();
				bestActions.add(legalAction);
				bestQValue = qValueOfLegalAction;
			}

		}
		return bestActions;

	}

	/**
	 * Returns the qValue constant that is assigned to terminal states. By
	 * convention this should be zero
	 * 
	 * @return qValue of terminal states
	 */
	@Override
	public double getQValueOfTerminalState() {
		return QVALUE_TERMINAL_STATE;
	}

	/**
	 * Returns qValue of the passed state-action tuple
	 * 
	 * @param state
	 * @param action
	 * @return
	 */
	@Override
	public double getQValue(int state, int action) {

		if (this.qTable.get(state) == null) {
			throw new NullPointerException(
					"state has not been initialised yet; According to the TD algorithm this state should not be accessed");
		}

		if (this.qTable.get(state).get(action) == null) {
			return QVALUE_TERMINAL_STATE;
		} else {
			// must have a valid qValue as the state is initialised and not a terminal state
			return this.qTable.get(state).get(action);
		}
	}

	/**
	 * Returns the number of distinct states the agent has visited so far, i.e. the
	 * number of states with an entry in the qTable
	 * 
	 * @return number of distinct states the agent has visited
	 */
	@Override
	public int getNumberOfDistinctVisitedStates() {
		return this.qTable.size();
	}

	/**
	 * Returns the HashMap that contains all legalActions mapped to their respective
	 * qValue for the given state.
	 * 
	 * This method is used to analyse the perception the agent has on the given
	 * state. This method is not contained in the interface as its signature differs
	 * from the one used in WTable and both methods are not used during training
	 * 
	 * @param state for that the action-qValue map is to be returned
	 * @return action-qValue map for the given state
	 */
	@Override
	public HashMap<Integer, Double> getActionQValueMapForState(int state) {
		return this.qTable.get(state);
	}

}
