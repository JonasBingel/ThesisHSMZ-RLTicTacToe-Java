package pack;

public class EpisodeAgentTracker {
	private final Symbol AGENT;
	private int currentEpisode;
	private int currentBatch;
	private double currentEpsilon;
	private double currentAlpha;

	private int numberOfPlies = 0;
	private int numberOfAgentActions = 0;
	private int numberOfExploratoryActions = 0;
	private int numberOfOptimalActions = 0;
	private int numberOfOptimalActionsWOExploration = 0;
	private float reward = 0;

	private boolean episodeWon = false;
	private boolean episodeLost = false;
	private boolean episodeDraw = false;

	private int totalNumberOfPlies = 0;
	private int totalNumberAgentActions = 0;
	private int totalNumberExploratoryActions = 0;
	private int totalNumberOptimalActions = 0;
	private int totalNumberOptimalActionsWOExploration = 0;

	private int totalNumberGamesWon = 0;
	private int totalNumberGamesLost = 0;
	private int totalNumberGamesDraw = 0;

	public EpisodeAgentTracker(Symbol agent) {
		this.AGENT = agent;
		this.currentEpisode = 0;
		this.currentBatch = 0;

	}

	/**
	 * Method is to be called at the begining of an episode to reset the tracking
	 * variables from the last episode and to set the current hyperparameter values
	 * 
	 * @param currentEpsilon epsilon for the current episode
	 * @param currentAlpha   alpha for the current episode
	 */
	public void resetEpisodeTracking(double currentEpsilon, double currentAlpha) {
		this.currentAlpha = currentAlpha;
		this.currentEpsilon = currentEpsilon;

		this.numberOfPlies = 0;
		this.numberOfAgentActions = 0;
		this.numberOfExploratoryActions = 0;
		this.numberOfOptimalActions = 0;
		this.numberOfOptimalActionsWOExploration = 0;
		this.reward = 0;

		this.episodeWon = false;
		this.episodeLost = false;
		this.episodeDraw = false;
	}

	/**
	 * Updates the cumulative tracking variables with the data collected in the last
	 * episode
	 */
	public void updateTotalNumbers() {
		this.totalNumberOfPlies += this.numberOfPlies;
		this.totalNumberAgentActions += this.numberOfAgentActions;
		this.totalNumberExploratoryActions += this.numberOfExploratoryActions;
		this.totalNumberOptimalActions += this.numberOfOptimalActions;
		this.totalNumberOptimalActionsWOExploration += this.numberOfOptimalActionsWOExploration;
	}

	/**
	 * Passed result of the game is one hot encoded and the total number of games
	 * won/lost/draw is updated
	 * 
	 * @param result
	 */
	public void setOneHotEncodedEpisodeResult(GameStatus result) {
		if (Utility.wasGameWon(result, this.AGENT)) {
			this.episodeWon = true;
			this.totalNumberGamesWon++;
		} else {
			if (result == GameStatus.DRAW) {
				this.episodeDraw = true;
				this.totalNumberGamesDraw++;
			} else {
				this.episodeLost = true;
				this.totalNumberGamesLost++;
			}
		}
	}

	public int getCurrentEpisode() {
		return this.currentEpisode;
	}

	public void increaseCurrentEpisode() {
		this.currentEpisode++;
	}

	public int getCurrentBatch() {
		return this.currentBatch;
	}

	public void increaseCurrentBatch() {
		this.currentBatch++;
	}

	public Symbol getAGENT() {
		return this.AGENT;
	}

	public double getCurrentEpsilon() {
		return this.currentEpsilon;
	}

	public double getCurrentAlpha() {
		return this.currentAlpha;
	}

	public int getNumberOfPlies() {
		return this.numberOfPlies;
	}

	public void setNumberOfPlies(int numberOfPlies) {
		this.numberOfPlies = numberOfPlies;
	}

	public int getNumberOfAgentActions() {
		return this.numberOfAgentActions;
	}

	public void increaseNumberOfAgentActions() {
		this.numberOfAgentActions++;
	}

	public int getNumberOfExploratoryActions() {
		return this.numberOfExploratoryActions;
	}

	public void increaseNumberOfExploratoryActions() {
		this.numberOfExploratoryActions++;
	}

	public int getNumberOfOptimalActions() {
		return this.numberOfOptimalActions;
	}

	public void increaseNumberOfOptimalActions() {
		this.numberOfOptimalActions++;
	}

	public int getNumberOfOptimalActionsWOExploration() {
		return this.numberOfOptimalActionsWOExploration;
	}

	public void increaseNumberOfOptimalActionsWOExploration() {
		this.numberOfOptimalActionsWOExploration++;
	}

	public float getReward() {
		return this.reward;
	}

	public void setReward(float reward) {
		this.reward = reward;
	}

	public boolean isEpisodeWon() {
		return this.episodeWon;
	}

	public boolean isEpisodeLost() {
		return this.episodeLost;
	}

	public boolean isEpisodeDraw() {
		return this.episodeDraw;
	}

	public int getTotalNumberOfPlies() {
		return this.totalNumberOfPlies;
	}

	public int getTotalNumberAgentActions() {
		return this.totalNumberAgentActions;
	}

	public int getTotalNumberExploratoryActions() {
		return this.totalNumberExploratoryActions;
	}

	public int getTotalNumberOptimalActions() {
		return this.totalNumberOptimalActions;
	}

	public int getTotalNumberOptimalActionsWOExploration() {
		return this.totalNumberOptimalActionsWOExploration;
	}

	public int getTotalNumberGamesWon() {
		return this.totalNumberGamesWon;
	}

	public int getTotalNumberGamesLost() {
		return this.totalNumberGamesLost;
	}

	public int getTotalNumberGamesDraw() {
		return this.totalNumberGamesDraw;
	}

}
