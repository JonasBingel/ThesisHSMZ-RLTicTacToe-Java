package pack;

public abstract class AgentRLTD {

	public AgentRLTD(Experience qTable) {
		this.qTable = qTable;
		this.isFirstStateOfNewEpisode = true;
	}

	protected Experience qTable;
	protected boolean isFirstStateOfNewEpisode;

	// last state that the agent was in, as well as the action taken are necessary
	// for updating the qValue
	protected int lastState;
	protected int lastAction;

	private boolean wasLastActionExploratory;
	private double explorationProbabilityEpsilon;
	private double discountRateGamma;
	private double stepSizeAlpha;

	/**
	 * Agent is passed the current state and available actions as well as the reward
	 * for the last chosen action The agent chooses one of the actions using the
	 * epsilon-greedy policy. The agent also executes the TD update to update the
	 * QValue of the previous SAtuple - however only if it is not the first state of
	 * the episode for the agent. The TD update is different for SARSA and
	 * Q-Learning
	 * 
	 * @param state        the action is currently in; this should not be a terminal
	 *                     state
	 * @param legalActions set of Actions available to the agent, this should not be
	 *                     empty as it denotes a terminal state
	 * @param reward       reward the environment assigned to the last chosen action
	 * @return action encoded as the index as a slot on the gamefield; the returned
	 *         action is an element of legalActions
	 */
	public abstract int move(int state, int[] legalActions, float reward);

	/**
	 * Distribute the final reward for the last action taken in the previous state
	 * Method updates the qValue of the previous state-action tuple and adds an
	 * entry for the terminal state in the qTable
	 * 
	 * Should only be used when a terminalState has been reached
	 * 
	 * @param terminalState that ended the game
	 * @param reward        for the last state-action tuple, this should be the
	 *                      reward for winning, losing, drawing
	 */
	public void distributeFinalReward(int terminalState, float reward) {
		this.qTable.initialiseQTableEntryIfNotExistent(terminalState, new int[0]);
		double qValueLastSATuple = this.qTable.getQValue(this.lastState, this.lastAction);
		double updatedQValuePreviousSATuple = this.calculateUpdatedQValue(qValueLastSATuple,
				this.qTable.getQValueOfTerminalState(), reward);

		this.qTable.updateQTableValue(this.lastState, this.lastAction, updatedQValuePreviousSATuple);

	}

	/**
	 * Picks an action using the eps-greedy policy, i.e. with a probability of eps a
	 * random action is chosen otherwise, i.e. (1-eps), the bestAction
	 * 
	 * @param bestAction   best action for the current state that the agent is in;
	 *                     selected with probability 1-eps
	 * @param legalActions set of legal actions that an entry is randomly selected
	 *                     from with possibility eps
	 * @return action chosen according to the eps-greedy policy
	 */
	protected int pickActionUsingEpsilonGreedy(int bestAction, int[] legalActions) {
		int chosenAction = bestAction;

		if (Utility.getRandomDouble() < this.explorationProbabilityEpsilon) {
			chosenAction = legalActions[Utility.getRandomInt(legalActions.length)];
			this.wasLastActionExploratory = true;
		} else {
			this.wasLastActionExploratory = false;
		}

		return chosenAction;
	}

	/**
	 * Calculates and updates the qValue of state-action tuple last taken by the
	 * agent in the qTable using information from this ply (i.e. the next state and
	 * next action)
	 * 
	 * @param currentState           state the agent is currently in; in the
	 *                               calculation this is denoted as S'
	 * @param actionToBeUsedInUpdate action that is to be used in the update; in
	 *                               case of SARSA this the action the agent will
	 *                               take, in case of Q-Learning this is the greedy
	 *                               action
	 * @param reward                 reward that the environment distributed for the
	 *                               last state-action tuple
	 */
	protected void updateQValueOfLastSATuple(int currentState, int actionToBeUsedInUpdate, float reward) {
		double qValueOfLastSATuple = this.qTable.getQValue(this.lastState, this.lastAction);
		double qValueNextSATuple = this.qTable.getQValue(currentState, actionToBeUsedInUpdate);

		double updatedQValuePreviousSATuple = calculateUpdatedQValue(qValueOfLastSATuple, qValueNextSATuple, reward);

		this.qTable.updateQTableValue(this.lastState, this.lastAction, updatedQValuePreviousSATuple);
	}

	/**
	 * Calculates the updated qValue using the hyperparameters attributes and the
	 * passed rewards and qValues of last and next state-action tuples
	 * 
	 * @param qValueSATupleToUpdate qValue of the state-action tuple that was last
	 *                              taken and is to be updated
	 * @param qValueNextSATuple     qValue of the state-action tuple that will be
	 *                              taken next (in case of sarsa) or with the max
	 *                              qValue (in case of sarsa)
	 * @param reward                reward that the environment distributed for the
	 *                              last state-action tuple/ SA tuple to update
	 * @return
	 */
	private double calculateUpdatedQValue(double qValueSATupleToUpdate, double qValueNextSATuple, float reward) {
		double tdError = this.discountRateGamma * qValueNextSATuple + reward - qValueSATupleToUpdate;
		double updatedQValue = qValueSATupleToUpdate + (this.stepSizeAlpha * tdError);

		return updatedQValue;
	}

	/**
	 * Declares to the agent that the next state it receives from the environment is
	 * the first state of a new episode.
	 * 
	 * @param isFirstState
	 */
	public void setIsFirstStateOfNewEpisode() {
		this.isFirstStateOfNewEpisode = true;
	}

	/**
	 * Set all hyperparameters that are used while choosing the next move and
	 * updating the qValues
	 * 
	 * @param epsilon probability that an exploratory, i.e. random, move is taken
	 * @param gamma   discount rate for future rerwards
	 * @param alpha   step size or learning rate used during the udpate process
	 */
	public void setHyperparameters(double epsilon, double gamma, double alpha) {
		this.explorationProbabilityEpsilon = epsilon;
		this.discountRateGamma = gamma;
		this.stepSizeAlpha = alpha;
	}

	public double getExplorationProbabilityEpsilon() {
		return explorationProbabilityEpsilon;
	}

	public void setExplorationProbabilityEpsilon(double explorationProbabilityEpsilon) {
		this.explorationProbabilityEpsilon = explorationProbabilityEpsilon;
	}

	public double getStepSizeAlpha() {
		return stepSizeAlpha;
	}

	public void setStepSizeAlpha(double stepSizeAlpha) {
		this.stepSizeAlpha = stepSizeAlpha;
	}

	/**
	 * Method returns the discount rate gamma No Setter for the discount rate gamma
	 * is implemented as gamma can only be set in the beginning Changing gamma means
	 * the underlying MDP and thus the entire problem is changed
	 * 
	 * @return value of the discount rate gamma
	 */
	public double getDiscountRateGamma() {
		return discountRateGamma;
	}

	public boolean wasLastActionExploratory() {
		return wasLastActionExploratory;
	}

}
