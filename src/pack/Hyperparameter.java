package pack;

public class Hyperparameter {

	private final double INITIAL_ALPHA;
	private final double FINAL_ALPHA;
	private final double INITIAL_EPSILON;
	private final double FINAL_EPSILON;

	private final int TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA;
	private final int TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON;

	private final HyperparameterChangeMode alphaChangeMode;
	private final HyperparameterChangeMode epsilonChangeMode;

	public Hyperparameter(double initAlpha, double finalAlpha, HyperparameterChangeMode alphaChangeMode,
			int numberOfEpisodesToReachFinalValueAlpha, double initEpsilon, double finalEpsilon,
			HyperparameterChangeMode epsilonChangeMode, int numberOfEpisodesToReachFinalValueEpsilon) {
		this.INITIAL_ALPHA = initAlpha;
		this.FINAL_ALPHA = finalAlpha;
		this.INITIAL_EPSILON = initEpsilon;
		this.FINAL_EPSILON = finalEpsilon;

		this.alphaChangeMode = alphaChangeMode;
		this.epsilonChangeMode = epsilonChangeMode;
		this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA = numberOfEpisodesToReachFinalValueAlpha;
		this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON = numberOfEpisodesToReachFinalValueEpsilon;
	}

	/**
	 * Copy constructor to create a copy of the passed hyperparameter
	 * 
	 * @param hyperparameterToCopy hyperparameter to copy
	 */
	public Hyperparameter(Hyperparameter hyperparameterToCopy) {
		this.INITIAL_ALPHA = hyperparameterToCopy.getINITIAL_ALPHA();
		this.FINAL_ALPHA = hyperparameterToCopy.getFINAL_ALPHA();
		this.INITIAL_EPSILON = hyperparameterToCopy.getINITIAL_EPSILON();
		this.FINAL_EPSILON = hyperparameterToCopy.getFINAL_EPSILON();

		this.alphaChangeMode = hyperparameterToCopy.getAlphaChangeMode();
		this.epsilonChangeMode = hyperparameterToCopy.getEpsilonChangeMode();
		this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA = hyperparameterToCopy
				.getTOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA();
		this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON = hyperparameterToCopy
				.getTOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON();
	}

	/**
	 * Update the hyperparameter alpha and return its value
	 * 
	 * @param currentEpisode episode the agent is currently in
	 * @return value of alpha after the update
	 */
	public double updateAndGetAlpha(int currentEpisode) {
		return this.updateHyperParameter(this.alphaChangeMode, currentEpisode, this.INITIAL_ALPHA, this.FINAL_ALPHA,
				this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA);
	}

	/**
	 * Update the hyperparameter epsilon and return its value
	 * 
	 * @param currentEpisode episode the agent is currently in
	 * @return value of epsilon after the update
	 */
	public double updateAndGetEpsilon(int currentEpisode) {
		return this.updateHyperParameter(this.epsilonChangeMode, currentEpisode, this.INITIAL_EPSILON,
				this.FINAL_EPSILON, this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON);
	}

	/**
	 * Updates the hyperparameter according to the passed passed parameters
	 * 
	 * @param changeMode                        changemode of the hyperparameter
	 * @param currentEpisode                    episode the agent is currently in
	 * @param initalValue                       inital value of the hyperparameter
	 * @param finalValue                        final value of the hyperparameter
	 * @param numberOfEpisodesToReachFinalValue number of episodes before the
	 *                                          hyperparameter reaches the final
	 *                                          value
	 * @return
	 */
	private double updateHyperParameter(HyperparameterChangeMode changeMode, int currentEpisode, double initalValue,
			double finalValue, int numberOfEpisodesToReachFinalValue) {
		if (changeMode == HyperparameterChangeMode.DEGRESSIVE_DECAY) {
			return this.calculateDegressiveDecay(currentEpisode, initalValue, finalValue,
					numberOfEpisodesToReachFinalValue);
		} else {
			return finalValue;
		}
	}

	/**
	 * Calculates the degressive decayed new parameterValue according to the
	 * step-based decay formula used
	 * 
	 * @param totalNumberOfEpisodes total number of episodes in the training
	 * @param currentEpisode        current episode in the training
	 * @param initialParameterValue value the parameter is initially set to
	 * @param finalParameterValue   value the parameter is supposed to reach in the
	 *                              final episode
	 * @return degressive decayed parameter
	 */
	private double calculateDegressiveDecay(int currentEpisode, double initialParameterValue,
			double finalParameterValue, int numberOfEpisodesToReachFinalValue) {

		if (currentEpisode <= numberOfEpisodesToReachFinalValue) {
			double relativeParameterChange = finalParameterValue / initialParameterValue;
			// Typecast is unproblematic as int is typecasted to double
			double relativeEpsiode = (double) currentEpisode / numberOfEpisodesToReachFinalValue;
			double decayedParameterValue = initialParameterValue * Math.pow(relativeParameterChange, relativeEpsiode);
			return decayedParameterValue;
		} else {
			return finalParameterValue;
		}
	}

	/**
	 * Formats the passed hyperparameter values as a string to be used in the
	 * toString method
	 * 
	 * @param parameterName                     name of the parameter
	 * @param changeMode                        changemode that the hyperparameter
	 *                                          uses
	 * @param initalValue                       initial value of the hyperparameter
	 * @param finalValue                        final value of the hyperparameter
	 * @param numberOfEpisodesToReachFinalValue number of episodes before the
	 *                                          hyperparameter reaches the final
	 *                                          value
	 * @return
	 */
	private String formatHyperparameterString(String parameterName, HyperparameterChangeMode changeMode,
			double initalValue, double finalValue, int numberOfEpisodesToReachFinalValue) {
		StringBuilder hyperparameterString = new StringBuilder();

		hyperparameterString.append(parameterName + " Change Mode: " + changeMode.toString() + System.lineSeparator());
		if (changeMode == HyperparameterChangeMode.CONSTANT) {
			hyperparameterString.append("  Value of " + parameterName + ": " + initalValue + System.lineSeparator());
		} else {
			hyperparameterString
					.append("  Initial Value of " + parameterName + ": " + initalValue + System.lineSeparator());
			hyperparameterString
					.append("  Final Value of " + parameterName + ": " + finalValue + System.lineSeparator());
			hyperparameterString.append("  Final value reached after " + numberOfEpisodesToReachFinalValue + " episodes"
					+ System.lineSeparator());
		}
		return hyperparameterString.toString();

	}

	@Override
	public String toString() {
		StringBuilder hyperparameterString = new StringBuilder();
		hyperparameterString.append(this.formatHyperparameterString("Alpha", this.alphaChangeMode, this.INITIAL_ALPHA,
				this.FINAL_ALPHA, this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA));
		hyperparameterString.append(this.formatHyperparameterString("Epsilon", this.epsilonChangeMode,
				this.INITIAL_EPSILON, this.FINAL_EPSILON, this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON));

		return hyperparameterString.toString();
	}

	public double getINITIAL_ALPHA() {
		return this.INITIAL_ALPHA;
	}

	public double getFINAL_ALPHA() {
		return this.FINAL_ALPHA;
	}

	public double getINITIAL_EPSILON() {
		return this.INITIAL_EPSILON;
	}

	public double getFINAL_EPSILON() {
		return this.FINAL_EPSILON;
	}

	public int getTOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA() {
		return this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_ALPHA;
	}

	public int getTOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON() {
		return this.TOTAL_NUMBER_OF_EPISODES_TO_REACH_FINAL_VALUE_EPSILON;
	}

	public HyperparameterChangeMode getAlphaChangeMode() {
		return this.alphaChangeMode;
	}

	public HyperparameterChangeMode getEpsilonChangeMode() {
		return this.epsilonChangeMode;
	}

}
