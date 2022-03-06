package pack;

import java.util.ArrayList;

public class MinimaxAlgorithm {
	private TranspositionTable transpositionTable;
	private Gamefield internalGamefield;
	private boolean considerDepthInScoreCalculation = false;

	/**
	 * Constructor of the AgentMiniMax class that creates a new instance and builds
	 * the transposition table based on the passed gamefield & scoremodifier
	 * 
	 * @param gamefield                       gamefield that the transposition table
	 *                                        is constructed for
	 * @param considerDepthInScoreCalculation flag to adjust whether the depth in
	 *                                        the gametree should negatively affect
	 *                                        the score (depthpenalty)
	 */
	public MinimaxAlgorithm(Gamefield gamefield, boolean considerDepthInScoreCalculation) {
		this.transpositionTable = new TranspositionTable();
		this.internalGamefield = gamefield;
		this.considerDepthInScoreCalculation = considerDepthInScoreCalculation;

		this.buildTranspositionTable();
	}

	/**
	 * Method builds the transposition table for the Gamefield passed in the
	 * constructor. The game starts with symbol X
	 */
	private void buildTranspositionTable() {

		Node rootNode = this.minimax(0, Symbol.SYMBOL_X);
		this.transpositionTable.putNode(this.internalGamefield.getState(), rootNode);
		this.outputTranspositionTableSize();
	}

	private void outputTranspositionTableSize() {
		System.out.println("number of entries in the transposition table: " + this.transpositionTable.getSize());
	}

	/**
	 * Implementation of the recursive minimax algorithm that constructs a complete
	 * gametree for the passed gamefield and derives the optimal moves for each
	 * player in every node The nodes are saved in the transposition table
	 * 
	 * @param depth  current depth in the game tree of the node for the node to be
	 *               calculated
	 * @param symbol player that is allowed to act in this node
	 * @return Node in the gametree that was analyzed containing optimal actions and
	 *         expected score
	 */
	private Node minimax(int depth, Symbol symbol) {

		int[] legalActions = this.internalGamefield.getLegalActions();

		if (legalActions.length == 0) {
			// if terminal state (leaf) create a leafnode, assign the score and return
			Node leaf = new Node(symbol);
			leaf.setDepth(depth);
			leaf.setScore(this.calculateScore(depth));

			return leaf;
		}

		Node currentNode = new Node(symbol);

		int initialScore = symbol.isX() ? -1 : 1;
		currentNode.setScore(initialScore);

		Symbol nextSymbol = Symbol.getNextSymbol(symbol);

		for (int legalAction : legalActions) {

			this.internalGamefield.applyAction(symbol, legalAction);

			Node nextNode = this.transpositionTable.retrieveNode(this.internalGamefield.getState());

			if (nextNode == null) {
				nextNode = this.minimax(depth + 1, nextSymbol);
				this.transpositionTable.putNode(this.internalGamefield.getState(), nextNode);
			}

			this.updateCurrentNode(currentNode, nextNode, legalAction, symbol);

			this.internalGamefield.undoAction(symbol, legalAction);

		}
		return currentNode;
	}

	/**
	 * Update the currentNode if score of the next node is better from the
	 * perspective of the passed symbol
	 * 
	 * @param currentNode node to be updated
	 * @param nextNode    node that is following the current node
	 * @param legalAction legal action that is taken by the symbol to traverse from
	 *                    the current node to next node
	 * @param symbol      symbol that chooses an action in the current node
	 */
	private void updateCurrentNode(Node currentNode, Node nextNode, int legalAction, Symbol symbol) {
		if (currentNode.getScore() == nextNode.getScore()) {
			currentNode.setDepth(nextNode.getDepth());
			currentNode.addOptimalAction(legalAction);
		}

		boolean isScoreBetterThanCurrentForX = symbol.isX() && (currentNode.getScore() < nextNode.getScore());
		boolean isScoreBetterThanCurrentForO = !symbol.isX() && (currentNode.getScore() > nextNode.getScore());

		if (isScoreBetterThanCurrentForX || isScoreBetterThanCurrentForO) {
			currentNode.setScore(nextNode.getScore());
			currentNode.setDepth(nextNode.getDepth());
			currentNode.clearBestActions();
			currentNode.addOptimalAction(legalAction);
		}

	}

	/**
	 * Calculate the score of the current game if the flag was set to consider depth
	 * a depthpentalty is applied to the scoring
	 * 
	 * Should be only called inside leaf nodes, ie. when no legal actions are left
	 * and the game is over
	 * 
	 * @param depth depth of the leaf inside the gametree for that the score is
	 *              calculated
	 * @return score of leaf as a float conatined in the interval -1 and 1
	 */
	private float calculateScore(int depth) {
		float score = 0;

		if (this.internalGamefield.getCurrentGameStatus() == GameStatus.WIN_O) {
			score = -1;
		}
		if (this.internalGamefield.getCurrentGameStatus() == GameStatus.WIN_X) {
			score = 1;
		}

		if (this.considerDepthInScoreCalculation) {
			score = score - (score * depth * 0.1f);
		}

		return score;

	}

	/**
	 * Retrieves the node corresponding to the passed state from the transposition
	 * table. if no node is found a NullPointereException is thrown. As the entire
	 * gametree is constructed and stored in the transposition table this means the
	 * state must be illegal and no sensible evaluation is possible
	 * 
	 * This method is a helper method for methods that are used during evaluation
	 * 
	 * @param state for that the corresponding node shall be retrieved
	 * @return node corresponding to the passed state
	 */
	private Node getNodeFromTranspositionTable(int state) {
		Node nodeToGivenState = this.transpositionTable.retrieveNode(state);

		if (nodeToGivenState == null) {
			// If there is no state inside the transposition table it must be an illegal
			// state and thus no sensible evaluation is possible
			throw new NullPointerException("no node inside the transposition table (of size: "
					+ this.transpositionTable.getSize() + ") for the passed state: " + state);
		}
		return nodeToGivenState;
	}

	/**
	 * Returns the optimal action for the passed state. If multiple actions are
	 * equally optimal the second parameter can be used to toggle between
	 * deterministic (false) and nondeterministic (true) action selection
	 * 
	 * @param state                  for that the optimal action is to be calculated
	 * @param choseActionArbitrarily if true an action is chosen arbitrarily, if
	 *                               false the action with the smallest index is
	 *                               chosen; only applies multiple actions are
	 *                               optimal
	 * @return optimal action encoded as int
	 */
	public int move(int state, boolean choseActionArbitrarily) {
		ArrayList<Integer> bestActions = this.getNodeFromTranspositionTable(state).getOptimalActions();
		int bestAction;

		if (bestActions.size() == 0) {
			throw new IllegalArgumentException(
					"move cannot be called for terminal states as there are no actions and thus no optimal one available");
		} else if (bestActions.size() > 1 && choseActionArbitrarily) {
			bestAction = Utility.getRandomElement(bestActions);
		} else {
			bestAction = bestActions.get(0);
		}
		return bestAction;
	}

	/**
	 * Returns an arrayList of optimal actions encoded as ints for the passed state
	 * 
	 * @param state for that the optimal actions are to be returned
	 * @return list of optimal actions encoded as ints
	 */
	public ArrayList<Integer> getBestActions(int state) {
		return this.getNodeFromTranspositionTable(state).getOptimalActions();
	}

}
