package pack;

public class ExperimentParameters {
	private final boolean USE_QLEARNING;
	private final boolean USE_QTABLE;
	private final boolean USE_ALTERNATE_SELFPLAY;
	private final boolean USE_REWARD_WITH_DEPTHPENATLY;
	private final int NUMBER_OF_TRAINING_EPISODES;
	private final int BATCH_SIZE;
	private final String BASE_FILENAME;
	private final String EXPERIENCE_FILENAME;

	private final Hyperparameter hyperparameter;

	public ExperimentParameters(boolean useQL, boolean useQTable, boolean useAlternateSelfplay,
			boolean useRewardWithDepthpenalty, int numberOfTrainingEpisodes, int batchSize,
			Hyperparameter hyperparameter) {
		this.USE_QLEARNING = useQL;
		this.USE_QTABLE = useQTable;
		this.USE_ALTERNATE_SELFPLAY = useAlternateSelfplay;
		this.USE_REWARD_WITH_DEPTHPENATLY = useRewardWithDepthpenalty;
		this.NUMBER_OF_TRAINING_EPISODES = numberOfTrainingEpisodes;
		this.BATCH_SIZE = batchSize;
		this.BASE_FILENAME = this.generateBasefilename();
		this.EXPERIENCE_FILENAME = this.generateExperienceFilename();
		this.hyperparameter = hyperparameter;
	}

	/**
	 * Generates the basefilename according to the convention that includes all
	 * parameters passed to the constructor Convention is:
	 * <Algorithm>_[<Afterstate>_][<Alternate>_][<Reward>]
	 * 
	 * @return basefilename for the logs
	 */
	private String generateBasefilename() {
		StringBuilder baseFilename = new StringBuilder();

		baseFilename.append(this.USE_QLEARNING ? "QLEARNING_" : "SARSA_");
		baseFilename.append(this.USE_QTABLE ? "" : "AFTERSTATE_");
		baseFilename.append(USE_ALTERNATE_SELFPLAY ? "ALTERNATE_" : "");
		baseFilename.append(this.USE_REWARD_WITH_DEPTHPENATLY ? "DP_" : "");

		return baseFilename.toString();

	}

	/**
	 * Generates the filename for the experience according to the convention
	 * 
	 * @return Filename for the experience
	 */
	private String generateExperienceFilename() {
		StringBuilder experienceFilename = new StringBuilder();

		experienceFilename.append(this.USE_QTABLE ? "QTABLE_" : "WTABLE_");
		experienceFilename.append(this.USE_QLEARNING ? "QLEARNING_" : "SARSA_");
		experienceFilename.append(USE_ALTERNATE_SELFPLAY ? "ALTERNATE_" : "");
		experienceFilename.append(this.USE_REWARD_WITH_DEPTHPENATLY ? "DP" : "");

		return experienceFilename.toString();

	}

	/**
	 * Returns a copy of the hyperparameter object
	 * 
	 * @return copy of the hyperparameter object
	 */
	public Hyperparameter getHyperparameter() {
		return new Hyperparameter(this.hyperparameter);
	}

	/**
	 * Creates and returns either a QTable or WTable according to the value passed
	 * to the constructor
	 * 
	 * @param initialQValue
	 * @return
	 */
	public Experience getExperience(double initialQValue) {
		if (this.USE_QTABLE) {
			return new QTable(initialQValue);
		} else {
			return new WTable(initialQValue);
		}
	}

	/**
	 * Returns either a SARSA or Q-Learning agent according to the value passed to
	 * the constructor that uses the passed experience
	 * 
	 * @param experience experience taht hte agent should use
	 * @return Q-Learning or SARSA agent that uses the passed experience
	 */
	public AgentRLTD getAgent(Experience experience) {
		if (this.USE_QLEARNING) {
			return new AgentQLearning(experience);
		} else {
			return new AgentSARSA(experience);
		}
	}

	public boolean isUSE_QLEARNING() {
		return this.USE_QLEARNING;
	}

	public boolean isUSE_QTABLE() {
		return this.USE_QTABLE;
	}

	public boolean isUSE_ALTERNATE_SELFPLAY() {
		return this.USE_ALTERNATE_SELFPLAY;
	}

	public boolean isUSE_REWARD_WITH_DEPTHPENATLY() {
		return this.USE_REWARD_WITH_DEPTHPENATLY;
	}

	public int getNUMBER_OF_TRAINING_EPISODES() {
		return this.NUMBER_OF_TRAINING_EPISODES;
	}

	public int getBATCH_SIZE() {
		return this.BATCH_SIZE;
	}

	public String getBASE_FILENAME() {
		return this.BASE_FILENAME;
	}

	public String getEXPERIENCE_FILENAME() {
		return EXPERIENCE_FILENAME;
	}
}
