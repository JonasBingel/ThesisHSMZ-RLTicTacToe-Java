package pack;

import java.util.ArrayList;
import java.util.LinkedList;

public class GameManager {
	private static final int NUMBER_OF_EVALUATION_EPISODES = 10000;
	private static boolean GENERATE_PLY_LOGS = true;
	public static final MinimaxAlgorithm MINIMAX = new MinimaxAlgorithm(new Gamefield(), true);

	/**
	 * Main method of the program where the functions to conduct the experiment can
	 * be called. However, before anything can be done the paths for the logs and
	 * experience need to be changed in {@link Logger}
	 * 
	 * Since the program writes a lot of CSV data it might be necessary to
	 * deactivate your antivirus program. At least on my computer this significantly
	 * cut down the running time
	 * 
	 * @param args are not processed so none should be passed
	 */
	public static void main(String[] args) {

		// Experiment with normal qTable - parameters need to be set inside the function
		// itself
		conductExperiment();

		// Experiment with wTable - parameters need to be set in side the function
		/**
		 * TODO There is currently a bug that necessiates two different experience
		 * instances. O overwrite values of X and vice versa. The strange phenomen is
		 * that X and O do not share any afterstates so theoretically should not be able
		 * to overwrite values of the other one. Exception being terminalstates but
		 * these are 0 by definition. I exported the afterstate keysets of both wTables
		 * if trained separately and the only intersection are the 958 Terminalstates.
		 * However the training still occurs via Self-play so the actual training and
		 * and objective of the thesis is not different. This bug will be dealt with
		 * after I have finished the thesis
		 */
		conductWTableExperimentSeparate();

		// The function below is used to load and deserialize some experience.
		// Afterwards the value inside some of the states can be analysed
		// int[] statesToAnalyse = new int[] {0};
		// loadAndAnalyseExperience("experience to load", statesToAnalyse);
	}

	/**
	 * Function to establish a baseline for the evaluation of the results. This is
	 * done by evaluating the minimax and random agent
	 */
	public static void analyseTempAgents() {

		AgentRLTD agent = new AgentRandom(null);
		Logger.generateFilenames("RANDOM");

		for (int i = 0; i < 5; i++) {
			evaluateAgentAgainstMinimax(agent, Symbol.SYMBOL_X);
			evaluateAgentAgainstRandom(agent, Symbol.SYMBOL_X);
			evaluateAgentAgainstMinimax(agent, Symbol.SYMBOL_O);
			evaluateAgentAgainstRandom(agent, Symbol.SYMBOL_O);
		}

		agent = new AgentRandom(null);
		Logger.generateFilenames("MINIMAX");

		for (int i = 0; i < 5; i++) {
			evaluateAgentAgainstMinimax(agent, Symbol.SYMBOL_X);
			evaluateAgentAgainstRandom(agent, Symbol.SYMBOL_X);
			evaluateAgentAgainstMinimax(agent, Symbol.SYMBOL_O);
			evaluateAgentAgainstRandom(agent, Symbol.SYMBOL_O);
		}

	}

	/**
	 * Function creates the hyperparameter and experimentparameter instance to use
	 * in the self-play experiment with wTable. The individual parameters can be set
	 * using the variables below
	 * 
	 * TODO since there is still the bug mentioned above this method is separate
	 * from the conductExperiment method that is used for all the other experiments.
	 * This will be fixed later and is only a temporary workaround
	 */
	public static void conductWTableExperimentSeparate() {
		final int NUMBER_OF_TRAINING_EPISODES = 150000;

		final boolean USE_QL = true;
		final boolean USE_QTABLE = false;
		final boolean USE_REWARD_WITH_DEPTHPENATLY = true;

		final double INITAL_ALPHA = 0.1;
		final double FINAL_ALPHA = 0.1;
		final HyperparameterChangeMode CHANGEMODE_ALPHA = HyperparameterChangeMode.CONSTANT;

		final double INITAL_EPSILON = 1;
		final double FINAL_EPSILON = 0.1;
		final HyperparameterChangeMode CHANGEMODE_EPSILON = HyperparameterChangeMode.DEGRESSIVE_DECAY;
		final int EPISODES_TO_REACH_FINAL_VALUE_EPSILON = NUMBER_OF_TRAINING_EPISODES * 2 / 3;

		Hyperparameter hyperparameter = new Hyperparameter(INITAL_ALPHA, FINAL_ALPHA, CHANGEMODE_ALPHA,
				NUMBER_OF_TRAINING_EPISODES, INITAL_EPSILON, FINAL_EPSILON, CHANGEMODE_EPSILON,
				EPISODES_TO_REACH_FINAL_VALUE_EPSILON);
		System.out.println(hyperparameter.toString());

		// Experiment parameters to be used in normal self-play
		ExperimentParameters experimentparamNormal = new ExperimentParameters(USE_QL, USE_QTABLE, false,
				USE_REWARD_WITH_DEPTHPENATLY, NUMBER_OF_TRAINING_EPISODES, 0, hyperparameter);

		for (int i = 0; i < 5; i++) {
			System.out.println("Iteration " + i);

			Logger.generateFilenames(experimentparamNormal.getBASE_FILENAME());

			WTable experienceX = (WTable) experimentparamNormal.getExperience(0);
			WTable experienceO = (WTable) experimentparamNormal.getExperience(0);
			AgentRLTD agentX = experimentparamNormal.getAgent(experienceX);
			AgentRLTD agentO = experimentparamNormal.getAgent(experienceO);

			Logger.logMetaData(experimentparamNormal);

			trainAgentSelfplay(agentX, agentO, experimentparamNormal);
			Logger.logToTxtFile("ExperienceX entries after training: " + experienceX.getNumberOfDistinctVisitedStates(),
					true);
			Logger.logToTxtFile("ExperienceO entries after training: " + experienceO.getNumberOfDistinctVisitedStates(),
					true);

			evaluateAgentAgainstMinimax(agentX, Symbol.SYMBOL_X);
			evaluateAgentAgainstRandom(agentX, Symbol.SYMBOL_X);

			evaluateAgentAgainstMinimax(agentO, Symbol.SYMBOL_O);
			evaluateAgentAgainstRandom(agentO, Symbol.SYMBOL_O);

			System.out.println("Completed Evaluation");
			Logger.logToTxtFile(
					"ExperienceO entries after training X: " + experienceX.getNumberOfDistinctVisitedStates(), true);
			Logger.logToTxtFile(
					"ExperienceO entries after training O: " + experienceO.getNumberOfDistinctVisitedStates(), true);
			Logger.serialiseExperience(experienceX, "X" + experimentparamNormal.getEXPERIENCE_FILENAME());
			Logger.serialiseExperience(experienceO, "O" + experimentparamNormal.getEXPERIENCE_FILENAME());
		}

	}

	/**
	 * Function creates the hyperparameter and experimentparameter instance to use
	 * in the self-play experiment. The individual parameters can be set using the
	 * variables below
	 */
	public static void conductExperiment() {
		final int NUMBER_OF_TRAINING_EPISODES = 150000;

		final int BATCH_SIZE = 100;
		final boolean USE_QL = true;
		final boolean USE_QTABLE = false;
		final boolean USE_REWARD_WITH_DEPTHPENATLY = true;

		final double INITAL_ALPHA = 0.1;
		final double FINAL_ALPHA = 0.1;
		final HyperparameterChangeMode CHANGEMODE_ALPHA = HyperparameterChangeMode.CONSTANT;

		final double INITAL_EPSILON = 1;
		final double FINAL_EPSILON = 0.1;
		final HyperparameterChangeMode CHANGEMODE_EPSILON = HyperparameterChangeMode.DEGRESSIVE_DECAY;
		final int EPISODES_TO_REACH_FINAL_VALUE_EPSILON = NUMBER_OF_TRAINING_EPISODES * 2 / 3;

		Hyperparameter hyperparameter = new Hyperparameter(INITAL_ALPHA, FINAL_ALPHA, CHANGEMODE_ALPHA,
				NUMBER_OF_TRAINING_EPISODES, INITAL_EPSILON, FINAL_EPSILON, CHANGEMODE_EPSILON,
				EPISODES_TO_REACH_FINAL_VALUE_EPSILON);
		System.out.println(hyperparameter.toString());

		// Experiment parameters to be used in normal self-play
		ExperimentParameters experimentparamNormal = new ExperimentParameters(USE_QL, USE_QTABLE, false,
				USE_REWARD_WITH_DEPTHPENATLY, NUMBER_OF_TRAINING_EPISODES, 0, hyperparameter);

		// Experiment parameters to be used in alternating self-play
		ExperimentParameters experimentparamAlternate = new ExperimentParameters(USE_QL, USE_QTABLE, true,
				USE_REWARD_WITH_DEPTHPENATLY, NUMBER_OF_TRAINING_EPISODES, BATCH_SIZE, hyperparameter);

		for (int i = 0; i < 5; i++) {
			System.out.println("Iteration: " + i);
			trainAndEvaluateAgent(experimentparamNormal);
			trainAndEvaluateAgent(experimentparamAlternate);
		}

	}

	/**
	 * Creates agents and trains them using either normal or alternating self-play
	 * according to the passed experiment parameters. Afterwards the agents are
	 * evaluated against minimax and a random player
	 * 
	 * @param experimentparameters parameters to use in the experiment
	 */
	public static void trainAndEvaluateAgent(ExperimentParameters experimentparameters) {
		Logger.generateFilenames(experimentparameters.getBASE_FILENAME());

		Experience experience = experimentparameters.getExperience(0);
		AgentRLTD agentX = experimentparameters.getAgent(experience);
		AgentRLTD agentO = experimentparameters.getAgent(experience);

		Logger.logMetaData(experimentparameters);

		if (experimentparameters.isUSE_ALTERNATE_SELFPLAY()) {
			trainAgentSelfplayAlternateLearning(agentX, agentO, experimentparameters);
		} else {
			trainAgentSelfplay(agentX, agentO, experimentparameters);
		}

		System.out.println("Completed Training");
		Logger.logToTxtFile("Experience entries after training: " + experience.getNumberOfDistinctVisitedStates(),
				true);

		evaluateAgentAgainstMinimax(agentX, Symbol.SYMBOL_X);
		evaluateAgentAgainstRandom(agentX, Symbol.SYMBOL_X);
		evaluateAgentAgainstMinimax(agentO, Symbol.SYMBOL_O);
		evaluateAgentAgainstRandom(agentO, Symbol.SYMBOL_O);
		System.out.println("Completed Evaluation");
		Logger.logToTxtFile("Experience entries after evaluation: " + experience.getNumberOfDistinctVisitedStates(),
				true);
		Logger.serialiseExperience(experience, experimentparameters.getEXPERIENCE_FILENAME());
	}

	public static void trainAndEvaluateAgentWTable(ExperimentParameters experimentparameters) {
		Logger.generateFilenames(experimentparameters.getBASE_FILENAME());

		Experience experienceX = experimentparameters.getExperience(0);
		Experience experienceO = experimentparameters.getExperience(0);
		AgentRLTD agentX = experimentparameters.getAgent(experienceX);
		AgentRLTD agentO = experimentparameters.getAgent(experienceO);

		Logger.logMetaData(experimentparameters);

		if (experimentparameters.isUSE_ALTERNATE_SELFPLAY()) {
			trainAgentSelfplayAlternateLearning(agentX, agentO, experimentparameters);
		} else {
			trainAgentSelfplay(agentX, agentO, experimentparameters);
		}

		System.out.println("Completed Training");
		Logger.logToTxtFile("ExperienceX entries after training: " + experienceX.getNumberOfDistinctVisitedStates(),
				true);
		Logger.logToTxtFile("ExperienceO entries after training: " + experienceO.getNumberOfDistinctVisitedStates(),
				true);

		evaluateAgentAgainstMinimax(agentX, Symbol.SYMBOL_X);
		evaluateAgentAgainstRandom(agentX, Symbol.SYMBOL_X);
		evaluateAgentAgainstMinimax(agentO, Symbol.SYMBOL_O);
		evaluateAgentAgainstRandom(agentO, Symbol.SYMBOL_O);
		System.out.println("Completed Evaluation");
		Logger.logToTxtFile("ExperienceX entries after evaluation: " + experienceX.getNumberOfDistinctVisitedStates(),
				true);
		Logger.logToTxtFile("ExperienceO entries after evaluation: " + experienceO.getNumberOfDistinctVisitedStates(),
				true);
		Logger.serialiseExperience(experienceX, experimentparameters.getEXPERIENCE_FILENAME() + "_X");
		Logger.serialiseExperience(experienceO, experimentparameters.getEXPERIENCE_FILENAME() + "_O");
	}

	/**
	 * Calculates the final reward for the passed symbol using the other parameters.
	 * If usedepthPentaly is used a penalty based on the depth in the gametree when
	 * the game ended is applied
	 * 
	 * @param useDepthPenalty apply depthpenalty to the reward
	 * @param gamestatus      status that the game ended with
	 * @param symbol          symbol to calculate the reward for
	 * @param depth           depth in the gametree when the game ended
	 * @return
	 */
	public static float calculateFinalReward(boolean useDepthPenalty, GameStatus gamestatus, Symbol symbol, int depth) {
		final float DRAW_REWARD = 0;
		int modifier = -1;
		float reward = -1;

		if (GameStatus.DRAW == gamestatus) {
			return DRAW_REWARD;
		}

		if (Utility.wasGameWon(gamestatus, symbol)) {
			reward = 1;
			modifier = 1;
		}
		if (useDepthPenalty) {
			reward = reward - (modifier * depth * 0.1f);
		}
		return reward;
	}

	/**
	 * Returns true if the passed chosen action is an element in the list of optimal
	 * actions according to minimax
	 * 
	 * @param chosenAction          action that he agent chose
	 * @param optimalActionsMinimax list of actions that are optimal according to
	 *                              minimax
	 * @return true if the action is in the list of optimal actions, else false
	 */
	public static boolean isActionOptimalAccordingToMinimax(int chosenAction,
			ArrayList<Integer> optimalActionsMinimax) {
		return optimalActionsMinimax.contains(chosenAction);
	}

	/**
	 * Train the agent using alternating self-play. For the duration of
	 * {@link ExperimentParameters#getBATCH_SIZE()} one agent learns while the other
	 * agent uses its current greedy strategy Both agents should access a shared
	 * experience table {@link Experience}
	 * 
	 * @param agentX               agent that plays as X
	 * @param agentO               agent that plays as O
	 * @param experimentparameters parameters to use during training with
	 *                             alternating self-play
	 */
	public static void trainAgentSelfplayAlternateLearning(AgentRLTD agentX, AgentRLTD agentO,
			ExperimentParameters experimentparameters) {

		Hyperparameter hyperparameterAgentX = experimentparameters.getHyperparameter();
		Hyperparameter hyperparameterAgentO = experimentparameters.getHyperparameter();

		GameResultTracker resultTrackerX = new GameResultTracker();
		GameResultTracker resultTrackerO = new GameResultTracker();
		EpisodeAgentTracker epAgentTrackerX = new EpisodeAgentTracker(Symbol.SYMBOL_X);
		EpisodeAgentTracker epAgentTrackerO = new EpisodeAgentTracker(Symbol.SYMBOL_O);

		Symbol symbolCurrentlyTraining = Symbol.SYMBOL_X;
		AgentRLTD currentlyTrainedAgent = agentX;
		Hyperparameter currentHyperparameters = hyperparameterAgentX;
		GameResultTracker currentResultTracker = resultTrackerX;
		EpisodeAgentTracker currentAgentTracker = epAgentTrackerX;

		Gamefield trainGamefield = new Gamefield();
		boolean isFirstGame = true;
		agentO.setHyperparameters(0, 1, 0);

		for (int episodeCount = 0; episodeCount < experimentparameters.getNUMBER_OF_TRAINING_EPISODES()
				* 2; episodeCount++) {

			LinkedList<Object[]> plyRecordList = new LinkedList<>();
			int numberOfPlies = 0;
			trainGamefield.resetGameField();
			currentlyTrainedAgent.setIsFirstStateOfNewEpisode();
			Symbol currentSymbol = Symbol.SYMBOL_X;

			int currentEpisodeForAgent = currentAgentTracker.getCurrentEpisode();

			double currentAlpha = currentHyperparameters.updateAndGetAlpha(currentEpisodeForAgent);
			double currentEpsilon = currentHyperparameters.updateAndGetEpsilon(currentEpisodeForAgent);
			currentlyTrainedAgent.setHyperparameters(currentEpsilon, 1, currentAlpha);
			currentAgentTracker.resetEpisodeTracking(currentEpsilon, currentAlpha);

			while (!trainGamefield.hasGameEnded()) {
				numberOfPlies++;
				int currentState = trainGamefield.getState();
				int[] legalActions = trainGamefield.getLegalActions();
				int chosenAction;
				boolean wasActionExploratory;

				if (currentSymbol.isX()) {
					chosenAction = agentX.move(currentState, legalActions, 0);
				} else {
					chosenAction = agentO.move(currentState, legalActions, 0);
				}

				trainGamefield.applyAction(currentSymbol, chosenAction);
				int afterState = trainGamefield.getState();
				ArrayList<Integer> bestActionsAccordingToMinimax = MINIMAX.getBestActions(currentState);
				boolean wasActionOptimal = isActionOptimalAccordingToMinimax(chosenAction,
						bestActionsAccordingToMinimax);

				if (currentSymbol == symbolCurrentlyTraining) {
					currentAgentTracker.increaseNumberOfAgentActions();
					wasActionExploratory = currentlyTrainedAgent.wasLastActionExploratory();

					if (wasActionExploratory) {
						currentAgentTracker.increaseNumberOfExploratoryActions();
					}

					if (wasActionOptimal) {
						currentAgentTracker.increaseNumberOfOptimalActions();
						if (!wasActionExploratory) {
							currentAgentTracker.increaseNumberOfOptimalActionsWOExploration();
						}
					}
				} else {
					wasActionExploratory = false;
				}

				if (GENERATE_PLY_LOGS) {
					Object[] plyRecord = Utility.createPlyRecord(currentEpisodeForAgent,
							currentAgentTracker.getCurrentBatch(), currentSymbol, numberOfPlies, currentState,
							chosenAction, afterState, wasActionExploratory, wasActionOptimal,
							bestActionsAccordingToMinimax, 0);
					plyRecordList.add(plyRecord);
				}

				currentSymbol = Symbol.getNextSymbol(currentSymbol);
			}
			int terminalState = trainGamefield.getState();
			GameStatus gameResult = trainGamefield.getCurrentGameStatus();
			float finalReward = calculateFinalReward(experimentparameters.isUSE_REWARD_WITH_DEPTHPENATLY(), gameResult,
					symbolCurrentlyTraining, numberOfPlies);

			currentlyTrainedAgent.distributeFinalReward(terminalState, finalReward);
			currentAgentTracker.setNumberOfPlies(numberOfPlies);
			currentAgentTracker.setOneHotEncodedEpisodeResult(gameResult);
			currentAgentTracker.setReward(finalReward);
			currentAgentTracker.updateTotalNumbers();
			currentResultTracker.updateTrackedGameResults(gameResult);

			if (GENERATE_PLY_LOGS) {
				Logger.logToPlyCSV(plyRecordList, Stage.TRAIN, isFirstGame);
			}

			Logger.logToGameCSV(Utility.createGameRecord(currentAgentTracker), Stage.TRAIN, isFirstGame);

			isFirstGame = false;

			currentAgentTracker.increaseCurrentEpisode();

			if (currentAgentTracker.getCurrentEpisode() % experimentparameters.getBATCH_SIZE() == 0) {
				// Set hyperparameters of previously trained agent to 0
				currentlyTrainedAgent.setHyperparameters(0, 1, 0);
				currentAgentTracker.increaseCurrentBatch();

				if (symbolCurrentlyTraining.isX()) {
					currentlyTrainedAgent = agentO;
					currentHyperparameters = hyperparameterAgentO;
					currentAgentTracker = epAgentTrackerO;
					currentResultTracker = resultTrackerO;
				} else {
					currentlyTrainedAgent = agentX;
					currentHyperparameters = hyperparameterAgentX;
					currentAgentTracker = epAgentTrackerX;
					currentResultTracker = resultTrackerX;
				}
				symbolCurrentlyTraining = Symbol.getNextSymbol(symbolCurrentlyTraining);
			}
		}
		String resultString = Utility.generateResultString("Training using Self-play ", Symbol.SYMBOL_X, Stage.TRAIN,
				experimentparameters.getNUMBER_OF_TRAINING_EPISODES(), resultTrackerX);
		Logger.logToTxtFile(resultString, true);
		resultString = Utility.generateResultString("Training using Self-play ", Symbol.SYMBOL_O, Stage.TRAIN,
				experimentparameters.getNUMBER_OF_TRAINING_EPISODES(), resultTrackerO);
		Logger.logToTxtFile(resultString, true);
	}

	/**
	 * Train the agent using self-play. Both agents should access a shared
	 * experience table {@link Experience}
	 * 
	 * @param agentX               agent that plays as X
	 * @param agentO               agent that plays as O
	 * @param experimentparameters parameters to use during training with self-play
	 */
	public static void trainAgentSelfplay(AgentRLTD agentX, AgentRLTD agentO,
			ExperimentParameters experimentparameters) {

		Hyperparameter hyperparameterAgentX = experimentparameters.getHyperparameter();
		Hyperparameter hyperparameterAgentO = experimentparameters.getHyperparameter();

		boolean isFirstGame = true;

		GameResultTracker resultTracker = new GameResultTracker();
		EpisodeAgentTracker epAgentTrackerX = new EpisodeAgentTracker(Symbol.SYMBOL_X);
		EpisodeAgentTracker epAgentTrackerO = new EpisodeAgentTracker(Symbol.SYMBOL_O);

		Gamefield trainGamefield = new Gamefield();

		for (int episodeCount = 0; episodeCount < experimentparameters
				.getNUMBER_OF_TRAINING_EPISODES(); episodeCount++) {
			LinkedList<Object[]> plyRecordList = new LinkedList<>();
			int numberOfPlies = 0;
			trainGamefield.resetGameField();
			agentX.setIsFirstStateOfNewEpisode();
			agentO.setIsFirstStateOfNewEpisode();
			Symbol currentSymbol = Symbol.SYMBOL_X;

			double currentAlphaX = hyperparameterAgentX.updateAndGetAlpha(episodeCount);
			double currentEpsilonX = hyperparameterAgentX.updateAndGetEpsilon(episodeCount);
			agentX.setHyperparameters(currentEpsilonX, 1, currentAlphaX);
			epAgentTrackerX.resetEpisodeTracking(currentEpsilonX, currentAlphaX);
			epAgentTrackerX.increaseCurrentEpisode();

			double currentAlphaO = hyperparameterAgentO.updateAndGetAlpha(episodeCount);
			double currentEpsilonO = hyperparameterAgentO.updateAndGetEpsilon(episodeCount);
			agentO.setHyperparameters(currentEpsilonO, 1, currentAlphaO);
			epAgentTrackerO.resetEpisodeTracking(currentEpsilonO, currentAlphaO);
			epAgentTrackerO.increaseCurrentEpisode();

			while (!trainGamefield.hasGameEnded()) {
				numberOfPlies++;
				int currentState = trainGamefield.getState();
				int[] legalActions = trainGamefield.getLegalActions();
				int chosenAction;
				boolean wasActionExploratory;

				if (currentSymbol.isX()) {
					chosenAction = agentX.move(currentState, legalActions, 0);
				} else {
					chosenAction = agentO.move(currentState, legalActions, 0);
				}

				trainGamefield.applyAction(currentSymbol, chosenAction);
				int afterState = trainGamefield.getState();
				ArrayList<Integer> bestActionsAccordingToMinimax = MINIMAX.getBestActions(currentState);
				boolean wasActionOptimal = isActionOptimalAccordingToMinimax(chosenAction,
						bestActionsAccordingToMinimax);

				if (currentSymbol.isX()) {
					epAgentTrackerX.increaseNumberOfAgentActions();

					wasActionExploratory = agentX.wasLastActionExploratory();
					if (wasActionExploratory) {
						epAgentTrackerX.increaseNumberOfExploratoryActions();
					}

					if (wasActionOptimal) {
						epAgentTrackerX.increaseNumberOfOptimalActions();
						if (!wasActionExploratory) {
							epAgentTrackerX.increaseNumberOfOptimalActionsWOExploration();
						}
					}
				} else {
					epAgentTrackerO.increaseNumberOfAgentActions();

					wasActionExploratory = agentO.wasLastActionExploratory();
					if (wasActionExploratory) {
						epAgentTrackerO.increaseNumberOfExploratoryActions();
					}

					if (wasActionOptimal) {
						epAgentTrackerO.increaseNumberOfOptimalActions();
						if (!wasActionExploratory) {
							epAgentTrackerO.increaseNumberOfOptimalActionsWOExploration();
						}
					}
				}

				if (GENERATE_PLY_LOGS) {
					Object[] plyRecord = Utility.createPlyRecord(episodeCount, experimentparameters.getBATCH_SIZE(),
							currentSymbol, numberOfPlies, currentState, chosenAction, afterState, wasActionExploratory,
							wasActionOptimal, bestActionsAccordingToMinimax, 0);
					plyRecordList.add(plyRecord);
				}
				currentSymbol = Symbol.getNextSymbol(currentSymbol);
			}
			int terminalState = trainGamefield.getState();
			GameStatus gameResult = trainGamefield.getCurrentGameStatus();

			float finalRewardX = calculateFinalReward(experimentparameters.isUSE_REWARD_WITH_DEPTHPENATLY(), gameResult,
					Symbol.SYMBOL_X, numberOfPlies);
			float finalRewardO = calculateFinalReward(experimentparameters.isUSE_REWARD_WITH_DEPTHPENATLY(), gameResult,
					Symbol.SYMBOL_O, numberOfPlies);
			agentX.distributeFinalReward(terminalState, finalRewardX);
			agentO.distributeFinalReward(terminalState, finalRewardO);

			resultTracker.updateTrackedGameResults(gameResult);

			// GameRecord
			epAgentTrackerX.setNumberOfPlies(numberOfPlies);
			epAgentTrackerX.setOneHotEncodedEpisodeResult(gameResult);
			epAgentTrackerX.setReward(finalRewardX);
			epAgentTrackerX.updateTotalNumbers();

			epAgentTrackerO.setNumberOfPlies(numberOfPlies);
			epAgentTrackerO.setOneHotEncodedEpisodeResult(gameResult);
			epAgentTrackerO.setReward(finalRewardO);
			epAgentTrackerO.updateTotalNumbers();

			if (GENERATE_PLY_LOGS) {
				Logger.logToPlyCSV(plyRecordList, Stage.TRAIN, isFirstGame);
			}
			Logger.logToGameCSV(Utility.createGameRecord(epAgentTrackerX), Stage.TRAIN, isFirstGame);
			Logger.logToGameCSV(Utility.createGameRecord(epAgentTrackerO), Stage.TRAIN, false);

			isFirstGame = false;

		}
		String resultString = Utility.generateResultString("Training using Self-play", null, Stage.TRAIN,
				experimentparameters.getNUMBER_OF_TRAINING_EPISODES(), resultTracker);
		Logger.logToTxtFile(resultString, true);
	}

	public static void trainAgentAgainstMinimax(AgentRLTD agent, Symbol symbolToTrain,
			ExperimentParameters experimentparameters) {
		GameManager.trainAgentAgainstNonRLOpponent(agent, symbolToTrain, true, experimentparameters);
	}

	public static void trainAgentAgainstRandom(AgentRLTD agent, Symbol symbolToTrain,
			ExperimentParameters experimentparameters) {
		GameManager.trainAgentAgainstNonRLOpponent(agent, symbolToTrain, false, experimentparameters);
	}

	public static void trainAgentAgainstNonRLOpponent(AgentRLTD agent, Symbol symbolToTrain,
			boolean trainAgainstMinimax, ExperimentParameters experimentparameters) {

		final int BATCH_COUNT = 0;

		boolean isFirstGame = true;

		GameResultTracker resultTracker = new GameResultTracker();
		EpisodeAgentTracker epAgentTracker = new EpisodeAgentTracker(symbolToTrain);

		Gamefield trainGamefield = new Gamefield();

		for (int episodeCount = 0; episodeCount < experimentparameters
				.getNUMBER_OF_TRAINING_EPISODES(); episodeCount++) {
			LinkedList<Object[]> plyRecordList = new LinkedList<>();

			int numberOfPlies = 0;
			Hyperparameter hyperparameterOfAgent = experimentparameters.getHyperparameter();

			trainGamefield.resetGameField();
			agent.setIsFirstStateOfNewEpisode();
			Symbol currentSymbol = Symbol.SYMBOL_X;

			double currentAlpha = hyperparameterOfAgent.updateAndGetAlpha(episodeCount);
			double currentEpsilon = hyperparameterOfAgent.updateAndGetEpsilon(episodeCount);

			agent.setHyperparameters(currentEpsilon, 1, currentAlpha);
			epAgentTracker.resetEpisodeTracking(currentEpsilon, currentAlpha);
			epAgentTracker.increaseCurrentEpisode();

			while (!trainGamefield.hasGameEnded()) {
				numberOfPlies++;
				int currentState = trainGamefield.getState();
				int[] legalActions = trainGamefield.getLegalActions();
				int chosenAction;
				boolean wasActionExploratory = false;

				if (currentSymbol == symbolToTrain) {
					chosenAction = agent.move(currentState, legalActions, 0);
				} else {
					if (trainAgainstMinimax) {
						chosenAction = MINIMAX.move(currentState, true);
					} else {
						chosenAction = legalActions[Utility.getRandomInt(legalActions.length)];
					}
				}

				trainGamefield.applyAction(currentSymbol, chosenAction);
				int afterState = trainGamefield.getState();
				ArrayList<Integer> bestActionsAccordingToMinimax = MINIMAX.getBestActions(currentState);
				boolean wasActionOptimal = isActionOptimalAccordingToMinimax(chosenAction,
						bestActionsAccordingToMinimax);

				if (currentSymbol == symbolToTrain) {
					epAgentTracker.increaseNumberOfAgentActions();

					wasActionExploratory = agent.wasLastActionExploratory();
					if (wasActionExploratory) {
						epAgentTracker.increaseNumberOfExploratoryActions();
					}

					if (wasActionOptimal) {
						epAgentTracker.increaseNumberOfOptimalActions();
						if (!wasActionExploratory) {
							epAgentTracker.increaseNumberOfOptimalActionsWOExploration();
						}
					}
				}

				if (GENERATE_PLY_LOGS) {
					Object[] plyRecord = Utility.createPlyRecord(episodeCount, BATCH_COUNT, symbolToTrain,
							numberOfPlies, currentState, chosenAction, afterState, wasActionExploratory,
							wasActionOptimal, bestActionsAccordingToMinimax, 0);
					plyRecordList.add(plyRecord);
				}

				currentSymbol = Symbol.getNextSymbol(currentSymbol);
			}

			int terminalState = trainGamefield.getState();
			GameStatus gameResult = trainGamefield.getCurrentGameStatus();

			float finalReward = calculateFinalReward(experimentparameters.isUSE_REWARD_WITH_DEPTHPENATLY(), gameResult,
					symbolToTrain, numberOfPlies);
			agent.distributeFinalReward(terminalState, finalReward);
			resultTracker.updateTrackedGameResults(gameResult);

			// GameRecord
			epAgentTracker.setNumberOfPlies(numberOfPlies);
			epAgentTracker.setOneHotEncodedEpisodeResult(gameResult);
			epAgentTracker.setReward(finalReward);
			epAgentTracker.updateTotalNumbers();

			Logger.logToPlyCSV(plyRecordList, Stage.TRAIN, isFirstGame);
			Logger.logToGameCSV(Utility.createGameRecord(epAgentTracker), Stage.TRAIN, isFirstGame);

			isFirstGame = false;
		}

		String resultString = Utility.generateResultString("", symbolToTrain, Stage.TRAIN,
				experimentparameters.getNUMBER_OF_TRAINING_EPISODES(), resultTracker);
		Logger.logToTxtFile(resultString, true);
	}

	/**
	 * Evaluate the agent as the passed symbol against an optimal minimax algorithm
	 * 
	 * @param agent            agent to test against minimax
	 * @param symbolToEvaluate symbol the agent should play during the evaluation
	 */
	public static void evaluateAgentAgainstMinimax(AgentRLTD agent, Symbol symbolToEvaluate) {
		GameManager.evaluateAgent(agent, symbolToEvaluate, true);
	}

	/**
	 * Evaluate the agent as the passed symbol against a random player
	 * 
	 * @param agent            agent to test a random player
	 * @param symbolToEvaluate symbol the agent plays during the evaluation
	 */
	public static void evaluateAgentAgainstRandom(AgentRLTD agent, Symbol symbolToEvaluate) {
		GameManager.evaluateAgent(agent, symbolToEvaluate, false);
	}

	/**
	 * Evaluate the agent as the passed symbol either against minimax or a random
	 * player
	 * 
	 * @param agent                  agent to evaluate
	 * @param symbolToEvaluate       symbol the agent plays during the evaluation
	 * @param evaluateAgainstMinimax true if agent should play against minimax,
	 *                               false if agent should play against random
	 *                               player
	 */
	public static void evaluateAgent(AgentRLTD agent, Symbol symbolToEvaluate, boolean evaluateAgainstMinimax) {
		final int CURRENT_ALPHA = 0;
		final int CURRENT_EPSILON = 0;
		final int DISTRIBUTED_REWARD = 0;
		final int BATCH_COUNT = 0;
		final boolean ACTION_EXPLORATORY = false;
		final boolean useDepthpenalty = true;

		agent.setStepSizeAlpha(CURRENT_ALPHA);
		agent.setExplorationProbabilityEpsilon(CURRENT_EPSILON);

		GameResultTracker resultTracker = new GameResultTracker();
		EpisodeAgentTracker epAgentTracker = new EpisodeAgentTracker(symbolToEvaluate);
		boolean isFirstGame = true;

		Gamefield evalGamefield = new Gamefield();

		for (int episodeCount = 0; episodeCount < NUMBER_OF_EVALUATION_EPISODES; episodeCount++) {

			int numberOfPlies = 0;
			Symbol currentSymbol = Symbol.SYMBOL_X;
			LinkedList<Object[]> plyRecordList = new LinkedList<>();
			epAgentTracker.resetEpisodeTracking(CURRENT_EPSILON, CURRENT_ALPHA);
			epAgentTracker.increaseCurrentEpisode();

			evalGamefield.resetGameField();
			agent.setIsFirstStateOfNewEpisode();

			while (!evalGamefield.hasGameEnded()) {
				numberOfPlies++;
				int currentState = evalGamefield.getState();
				int[] legalActions = evalGamefield.getLegalActions();
				int chosenAction;

				if (currentSymbol == symbolToEvaluate) {
					chosenAction = agent.move(currentState, legalActions, DISTRIBUTED_REWARD);
					epAgentTracker.increaseNumberOfAgentActions();
				} else {
					if (evaluateAgainstMinimax) {
						chosenAction = MINIMAX.move(currentState, true);
					} else {
						chosenAction = legalActions[Utility.getRandomInt(legalActions.length)];
					}
				}

				evalGamefield.applyAction(currentSymbol, chosenAction);

				ArrayList<Integer> bestActionsAccordingToMinimax = MINIMAX.getBestActions(currentState);
				boolean wasActionOptimal = isActionOptimalAccordingToMinimax(chosenAction,
						bestActionsAccordingToMinimax);
				int afterState = evalGamefield.getState();

				Object[] plyRecord = Utility.createPlyRecord(episodeCount, BATCH_COUNT, currentSymbol, numberOfPlies,
						currentState, chosenAction, afterState, ACTION_EXPLORATORY, wasActionOptimal,
						bestActionsAccordingToMinimax, DISTRIBUTED_REWARD);
				plyRecordList.add(plyRecord);

				if (currentSymbol == symbolToEvaluate) {
					// update variablesForGameLog
					if (wasActionOptimal) {
						epAgentTracker.increaseNumberOfOptimalActions();
						epAgentTracker.increaseNumberOfOptimalActionsWOExploration();
					}
				}
				currentSymbol = Symbol.getNextSymbol(currentSymbol);
			}

			GameStatus result = evalGamefield.getCurrentGameStatus();
			resultTracker.updateTrackedGameResults(result);
			float finalReward = calculateFinalReward(useDepthpenalty, result, symbolToEvaluate, numberOfPlies);

			epAgentTracker.setNumberOfPlies(numberOfPlies);
			epAgentTracker.setOneHotEncodedEpisodeResult(result);
			epAgentTracker.setReward(finalReward);
			epAgentTracker.updateTotalNumbers();

			if (GENERATE_PLY_LOGS) {
				Logger.logToPlyCSV(plyRecordList, Stage.EVAL, isFirstGame);
			}

			Logger.logToGameCSV(Utility.createGameRecord(epAgentTracker), Stage.EVAL, isFirstGame);
			isFirstGame = false;

		}
		String opponent = evaluateAgainstMinimax ? "Minimax" : "Random";
		String resultHeader = "Evaluation against" + opponent;
		String resultString = Utility.generateResultString(resultHeader, symbolToEvaluate, Stage.EVAL,
				NUMBER_OF_EVALUATION_EPISODES, resultTracker);
		Logger.logToTxtFile(resultString, true);
	}

	// State Analysis

	// stateAnalysis (Experience)
	// opening,

	// stateAnalysis (Agent, int[] listOfStates)

	/**
	 * @param experienceToLoad
	 * @param statesToLoad
	 */
	public static void loadAndAnalyseExperience(String experienceToLoad, int[] statesToLoad) {
		Logger.setFilenameLogState(experienceToLoad);
		Experience readExperience = Logger.deserialiseExperience(experienceToLoad);

		for (int state : statesToLoad) {
			System.out.println("state: " + state + "; " + Gamefield.convertStateAsIntToString(state));
			String actionQValue = Utility
					.convertActionQValueMapToString(readExperience.getActionQValueMapForState(state));
			System.out.println(actionQValue);
			Logger.logToTxtFile(actionQValue, false);
		}

	}
}
