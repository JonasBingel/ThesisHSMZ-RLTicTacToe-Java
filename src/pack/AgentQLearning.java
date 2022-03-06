package pack;

public class AgentQLearning extends AgentRLTD {

	public AgentQLearning(Experience qTable) {
		super(qTable);
	}

	@Override
	public int move(int state, int[] legalActions, float reward) {
		if (legalActions.length == 0) {
			throw new IllegalArgumentException(
					"legalActions is empty, ie. a terminal state has been reached. After reaching a terminal state the method distributeFinalReward should be used instead ");
		}

		this.qTable.initialiseQTableEntryIfNotExistent(state, legalActions);

		// The TD update is not executed if it is the first state of a new episode for
		// the agent
		if (!this.isFirstStateOfNewEpisode) {
			this.updateQValueOfLastSATuple(state, this.qTable.getBestAction(state, legalActions), reward);
		}

		// First the update for the previous SA tuple using the best action at the time
		// is done and then the next action is chosen according to the policy 
		// This is only noticeable/ relevant in edge cases where S' of S is S itself. It
		// does not occur in the implementation but is implemented here for the sake of
		// rigour
		int bestAction = this.qTable.getBestAction(state, legalActions);

		int chosenAction = this.pickActionUsingEpsilonGreedy(bestAction, legalActions);

		this.isFirstStateOfNewEpisode = false;
		this.lastState = state;
		this.lastAction = chosenAction;

		return chosenAction;
	}
}
