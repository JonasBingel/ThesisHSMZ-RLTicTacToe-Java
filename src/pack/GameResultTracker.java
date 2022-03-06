package pack;

public class GameResultTracker {
	private int numberOfGamesXWon;
	private int numberOfGamesOWon;
	private int numberOfGamesDraw;

	public GameResultTracker() {
		this.numberOfGamesXWon = 0;
		this.numberOfGamesOWon = 0;
		this.numberOfGamesDraw = 0;
	}

	/**
	 * Updates game results that are being tracked according to the passed result
	 * 
	 * @param result of the last game to track
	 */
	public void updateTrackedGameResults(GameStatus result) {
		if (result == GameStatus.WIN_X) {
			this.numberOfGamesXWon++;

		} else if (result == GameStatus.WIN_O) {
			this.numberOfGamesOWon++;
		} else if (result == GameStatus.DRAW) {
			this.numberOfGamesDraw++;
		} else {
			// no trackable result
		}
	}

	/**
	 * Returns the total number of games tracked
	 * 
	 * @return int of the total number of games
	 */
	public int getTotalNumberOfGames() {
		return this.numberOfGamesXWon + this.numberOfGamesOWon + this.numberOfGamesDraw;
	}

	public int getNumberOfGamesXWon() {
		return this.numberOfGamesXWon;
	}

	public int getNumberOfGamesOWon() {
		return this.numberOfGamesOWon;
	}

	public int getNumberOfGamesDraw() {
		return this.numberOfGamesDraw;
	}

}
