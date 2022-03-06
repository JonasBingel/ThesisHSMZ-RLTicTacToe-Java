package pack;

/**
 * This is just a wrapper for the MinimaxAlgorithm class so that it can be
 * evaluated using the methods that expect an instance of AgentRLTD class
 */
public class AgentMinimax extends AgentRLTD {

	/**
	 * Since the MinimaxAlgorithm does not store experience inside a qTable or
	 * wTable one can pass null
	 * 
	 * @param qTable can be null since MinimaxAlgorithm does not use a qTable
	 */
	public AgentMinimax(Experience qTable) {
		super(qTable);
	}

	/**
	 * Get one of the optimal moves by calling the move-Method of an instance of the
	 * MinimaxAlgorithm class The static instance from the GameManager is used
	 */
	@Override
	public int move(int state, int[] legalActions, float reward) {
		return GameManager.MINIMAX.move(state, true);
	}

}
