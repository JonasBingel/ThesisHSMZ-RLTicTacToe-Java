package pack;

/**
 * This is just a wrapper for so that a random agent can be evaluated using the
 * methods that expect an instance of AgentRLTD class
 */
public class AgentRandom extends AgentRLTD {

	/**
	 * Since just a random move is returned the passed qTable or wTable can be null
	 * 
	 * @param qTable can be null since random does not it
	 */
	public AgentRandom(Experience qTable) {
		super(qTable);
	}

	/**
	 * Returns a random action from the passed array of legalActions
	 */
	@Override
	public int move(int state, int[] legalActions, float reward) {
		return legalActions[Utility.getRandomInt(legalActions.length)];
	}

}
