package pack;

public class AgentSARSA extends AgentRLTD {

	public AgentSARSA(Experience qTable) {
		super(qTable);
	}

	@Override
	public int move(int state, int[] legalActions, float reward) {
		if (legalActions.length == 0) {
			throw new IllegalArgumentException(
					"legalActions is empty, ie. a terminal state has been reached. After reaching a terminal state the method distributeFinalReward should be used instead ");
		}
		
		this.qTable.initialiseQTableEntryIfNotExistent(state, legalActions);

		int bestAction = this.qTable.getBestAction(state, legalActions);

		int chosenAction = this.pickActionUsingEpsilonGreedy(bestAction, legalActions);

		// The TD update is not executed if it is the first state of a new episode for
		// the agent
		if (!this.isFirstStateOfNewEpisode) {
			this.updateQValueOfLastSATuple(state, chosenAction, reward);
		}

		this.isFirstStateOfNewEpisode = false;
		this.lastState = state;
		this.lastAction = chosenAction;

		return chosenAction;
	}

}
