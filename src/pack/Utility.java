package pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

public class Utility {

	private static final Random RANDOM = new Random();

	/**
	 * Returns a pseudorandom uniformely distributed double between 0.0 and 1.0
	 * 
	 * @see Random#nextDouble() nextDouble
	 * 
	 * @return pseudorandom double between 0 and 1
	 */
	public static double getRandomDouble() {
		return RANDOM.nextDouble();
	}

	/**
	 * Returns a random integer smaller than the passed bound, including zero. The
	 * possibility of all ints to be returned is approximately equal
	 * 
	 * @see Random#nextInt(int) nextInt(int bound)
	 * 
	 * @param bound upper bound; returned int is smaller than this
	 * @return random integer from the half-open interval [zero; bound -1)
	 */
	public static int getRandomInt(int bound) {
		return RANDOM.nextInt(bound);
	}

	public static String formatDouble(double doubleToFormat) {
		return String.format("%.16f", doubleToFormat);
	}

	/**
	 * Encodes the passed boolean as an int; 1 if passed boolean is true otherwise 0
	 * 
	 * @param booleanToEncode boolean to encode as an int
	 * @return passed boolean encoded as an int
	 */
	public static int encodeBooleanAsInt(boolean booleanToEncode) {
		return booleanToEncode ? 1 : 0;
	}

	/**
	 * Returns true if the passed symbol has won the game that ended with the passed
	 * status
	 * 
	 * @param gamestatus with that the game ended
	 * @param symbol     to check for if it won
	 * @return true if the passed symbol won the game that ended with the pssed
	 *         status
	 */
	public static boolean wasGameWon(GameStatus gamestatus, Symbol symbol) {
		boolean didXWin = GameStatus.WIN_X == gamestatus && symbol.isX();
		boolean didOWin = GameStatus.WIN_O == gamestatus && !symbol.isX();

		return didXWin | didOWin;
	}

	/**
	 * Returns a random int from the passed list. The possibility of each element
	 * being selected is approximately equal
	 * 
	 * @param listToChooseFrom ArrayList that an int is chosen from
	 * @return a random int from the passed arrayList
	 */
	public static int getRandomElement(ArrayList<Integer> listToChooseFrom) {
		int bound = listToChooseFrom.size();
		return listToChooseFrom.get(getRandomInt(bound));
	}

	/**
	 * Converts the passed Integer ArrayList to an array of the primitive type int
	 * that contains all elements
	 * 
	 * @param listToConvert arrayList to convert to a primitive int array
	 * @return primitive int array that contains all elements of the listToConvert
	 */
	public static int[] convertListToArray(ArrayList<Integer> listToConvert) {
		int[] listAsArray = new int[listToConvert.size()];

		for (int i = 0; i < listToConvert.size(); i++) {
			listAsArray[i] = listToConvert.get(i);
		}
		return listAsArray;

	}

	/**
	 * Converts the passed actionQValueMap to a string that can be appended to the
	 * log. The output is used to assess the agent's evaluation of the available
	 * actions for a given state by providing this function the actionQValueMap to a
	 * certain state.
	 * 
	 * @param actionQValueMap of a state which is to be converted to a string
	 * @return formatted string containing number of entries in passed actionQValue
	 *         map as well as the action mappings
	 */
	public static String convertActionQValueMapToString(HashMap<Integer, Double> actionQValueMap) {

		if (actionQValueMap == null) {
			throw new IllegalArgumentException(
					"passed action-qValue-map is null and can thus not be converted to a string");
		}

		StringBuilder actionQValueString = new StringBuilder();
		Set<Integer> availableActions = actionQValueMap.keySet();

		actionQValueString.append("The action-qValue-map has " + availableActions.size()
				+ " entries, that map to the following values: " + System.lineSeparator());

		for (int availableAction : availableActions) {
			double mappedQValue = actionQValueMap.get(availableAction);
			actionQValueString.append("Action: " + availableAction + "; mapped qValue: "
					+ Utility.formatDouble(mappedQValue) + System.lineSeparator());
		}
		return actionQValueString.toString();
	}

	/**
	 * Converts the passed ArrayList to a string that concatenates all elements
	 * delimited by a semicolon
	 * 
	 * @param listToConvert arrayList to be converted to String
	 * @return string containing all elements of the passed list delimited by
	 *         semicolon
	 */
	public static String convertListToString(ArrayList<Integer> listToConvert) {
		StringJoiner listAsString = new StringJoiner(";");

		for (Integer entry : listToConvert) {
			listAsString.add(entry.toString());
		}
		return listAsString.toString();
	}

	/**
	 * Generates and formats a string that contains all passed evaluation data for
	 * the given state
	 * 
	 * @param state              state that the evaluation data belongs to
	 * @param purpose            purpose of the state evaluation
	 * @param nStateEncountered  number of times the state has been encountered
	 *                           during training
	 * @param bestActionsMinimax best actions for the state according to minimax
	 * @param actionQValueMap    action-QValue-map for the given state
	 * @return string containing all evaluation data
	 */
	public static String generateStateEvaluationString(int state, EvaluationPurpose purpose, int nStateEncountered,
			ArrayList<Integer> bestActionsMinimax, HashMap<Integer, Double> actionQValueMap) {
		StringBuilder stateEvaluation = new StringBuilder();
		stateEvaluation.append("State: " + state + System.lineSeparator());
		stateEvaluation.append("Purpose: " + purpose.toString() + System.lineSeparator());
		stateEvaluation.append("Number of times state was encountered: " + nStateEncountered + System.lineSeparator());
		stateEvaluation.append(
				"Optimal Minimax actions: " + Utility.convertListToString(bestActionsMinimax) + System.lineSeparator());

		stateEvaluation.append(Utility.convertActionQValueMapToString(actionQValueMap));
		return stateEvaluation.toString();

	}

	/**
	 * Generates the string that contains all the meta data on the experiment that
	 * will be logged
	 * 
	 * @param experimentparameters parameters used in the experiment
	 * @return String formatted String containing meta data about the experiment
	 */
	public static String generateMetaDataString(ExperimentParameters experimentparameters) {

		StringBuilder metaDataString = new StringBuilder();
		String algorithm = experimentparameters.isUSE_QLEARNING() ? "Q-Learning" : "SARSA";
		String experience = experimentparameters.isUSE_QTABLE() ? "Q-Table" : "W-Table";
		String trainingMethod = experimentparameters.isUSE_ALTERNATE_SELFPLAY() ? "Self-play alternating"
				: "Normal Self-play";
		metaDataString.append("Algorithm: " + algorithm + System.lineSeparator());
		metaDataString.append("Experience: " + experience + System.lineSeparator());
		metaDataString.append("depth penalty applied to reward: "
				+ experimentparameters.isUSE_REWARD_WITH_DEPTHPENATLY() + System.lineSeparator());
		metaDataString.append("Training method: " + trainingMethod + System.lineSeparator());
		if (experimentparameters.isUSE_ALTERNATE_SELFPLAY()) {
			metaDataString.append("  Batchsize: " + experimentparameters.getBATCH_SIZE() + System.lineSeparator());
		}
		metaDataString.append("Number of training episodes: " + experimentparameters.getNUMBER_OF_TRAINING_EPISODES()
				+ System.lineSeparator() + System.lineSeparator());
		metaDataString.append(experimentparameters.getHyperparameter().toString());
		return metaDataString.toString();
	}

	/**
	 * Generates and formats a string that contains the passed result of the games
	 * and which stage of the execution they belong to
	 * 
	 * @param header           header for the resultString
	 * @param symbolAgentPlays symbol for that the result is logged
	 * @param stage            tage during the execution that the results belong to
	 * @param numberOfEpisodes number of episodes that were run during the stage
	 * @param resultTracker    tracker that contains the cumulated game results
	 * @return string containing the results
	 */
	public static String generateResultString(String header, Symbol symbolAgentPlays, Stage stage, int numberOfEpisodes,
			GameResultTracker resultTracker) {
		StringBuilder resultString = new StringBuilder();
		resultString.append(header + System.lineSeparator());
		if (symbolAgentPlays != null) {
			resultString.append("Agent playing as: " + symbolAgentPlays.toString() + System.lineSeparator());
		}
		resultString
				.append("Number of " + stage.toString() + " episodes: " + numberOfEpisodes + System.lineSeparator());
		resultString.append("Games won by X: " + resultTracker.getNumberOfGamesXWon() + System.lineSeparator());
		resultString.append("Games won by O: " + resultTracker.getNumberOfGamesOWon() + System.lineSeparator());
		resultString.append("Games ended in draw: " + resultTracker.getNumberOfGamesDraw() + System.lineSeparator());
		return resultString.toString();
	}

	/**
	 * 
	 * Creates a record object of a ply that can be passed to the logging function
	 * 
	 * @see Logger#logToPlyCSV(java.util.LinkedList, Stage, boolean)
	 * 
	 * @param episodeCount         episode the ply belongs to
	 * @param batchCount           batch the ply belongs to
	 * @param recordedSymbol       symbol that executed the ply
	 * @param currentPly           number of the ply in the episode
	 * @param currentState         state at the start of the ply
	 * @param chosenAction         action chosen by the symbol
	 * @param afterState           state after action has been applied
	 * @param wasActionExploratory was action that the agent picked exploratory
	 * @param wasActionOptimal     was the action chosen by the agent optimal
	 *                             according to minimax
	 * @param bestActions          best actions according to minimax
	 * @param reward               reward that is attributed to the ply
	 * @return ply record that is to be collected and passed to @see
	 *         {@link Logger#logToPlyCSV(java.util.LinkedList, Stage, boolean)} at
	 *         the end of an episode
	 */
	public static Object[] createPlyRecord(int episodeCount, int batchCount, Symbol recordedSymbol, int currentPly,
			int currentState, int chosenAction, int afterState, boolean wasActionExploratory, boolean wasActionOptimal,
			ArrayList<Integer> bestActions, float reward) {
		Object[] plyRecord = new Object[11];

		plyRecord[0] = episodeCount;
		plyRecord[1] = batchCount;
		plyRecord[2] = recordedSymbol.toString();
		plyRecord[3] = currentPly;
		plyRecord[4] = currentState;
		plyRecord[5] = chosenAction;
		plyRecord[6] = afterState;
		plyRecord[7] = Utility.encodeBooleanAsInt(wasActionExploratory);
		plyRecord[8] = Utility.encodeBooleanAsInt(wasActionOptimal);
		plyRecord[9] = Utility.convertListToString(bestActions);
		plyRecord[10] = reward;

		return plyRecord;
	}

	/**
	 * Create a game record object out of the passed dataTracker for the current
	 * episode that can be passed to @see
	 * {@link Logger#logToGameCSV(Object[], Stage, boolean)}
	 * 
	 * @param epDataTracker data tracked for the current episode
	 * @return game record object that can be passed to @see
	 *         {@link Logger#logToGameCSV(Object[], Stage, boolean)}
	 */
	public static Object[] createGameRecord(EpisodeAgentTracker epDataTracker) {
		Object[] gameRecord = new Object[22];

		gameRecord[0] = epDataTracker.getCurrentEpisode();
		gameRecord[1] = epDataTracker.getCurrentBatch();
		gameRecord[2] = epDataTracker.getAGENT().toString();
		gameRecord[3] = epDataTracker.getNumberOfPlies();
		gameRecord[4] = epDataTracker.getNumberOfAgentActions();
		gameRecord[5] = epDataTracker.getNumberOfExploratoryActions();
		gameRecord[6] = epDataTracker.getNumberOfOptimalActions();
		gameRecord[7] = epDataTracker.getNumberOfOptimalActionsWOExploration();
		gameRecord[8] = Utility.formatDouble(epDataTracker.getCurrentEpsilon());
		gameRecord[9] = Utility.formatDouble(epDataTracker.getCurrentAlpha());
		gameRecord[10] = Utility.encodeBooleanAsInt(epDataTracker.isEpisodeWon());
		gameRecord[11] = Utility.encodeBooleanAsInt(epDataTracker.isEpisodeLost());
		gameRecord[12] = Utility.encodeBooleanAsInt(epDataTracker.isEpisodeDraw());
		gameRecord[13] = Utility.formatDouble(epDataTracker.getReward());
		gameRecord[14] = epDataTracker.getTotalNumberOfPlies();
		gameRecord[15] = epDataTracker.getTotalNumberAgentActions();
		gameRecord[16] = epDataTracker.getTotalNumberExploratoryActions();
		gameRecord[17] = epDataTracker.getTotalNumberOptimalActions();
		gameRecord[18] = epDataTracker.getTotalNumberOptimalActionsWOExploration();
		gameRecord[19] = epDataTracker.getTotalNumberGamesWon();
		gameRecord[20] = epDataTracker.getTotalNumberGamesLost();
		gameRecord[21] = epDataTracker.getTotalNumberGamesDraw();

		return gameRecord;
	}

}
