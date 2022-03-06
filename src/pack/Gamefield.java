package pack;

import java.util.BitSet;
import java.util.ArrayList;
import java.util.Arrays;

public class Gamefield {
	private static final int GAMEFIELD_LENGTH = 3;
	private static final int GAMEFIELD_SIZE = GAMEFIELD_LENGTH * GAMEFIELD_LENGTH;

	/**
	 * Win patterns are longs denoted in binary notation. Bitset initialises using
	 * long array; win patterns are thus stored in 2D array to iterate over them;
	 * see: https://stackoverflow.com/a/40752880
	 */
	private static final long[][] WIN_PATTERNS = { { 0b111000000 }, { 0b000111000 }, { 0b000000111 }, { 0b100100100 },
			{ 0b010010010 }, { 0b001001001 }, { 0b100010001 }, { 0b001010100 } };

	private BitSet bitboardX;
	private BitSet bitboardO;
	private GameStatus currentGameStatus;

	public Gamefield() {
		this.bitboardX = new BitSet(GAMEFIELD_SIZE);
		this.bitboardO = new BitSet(GAMEFIELD_SIZE);
		this.currentGameStatus = GameStatus.INITIALIZED;
	}

	/**
	 * Method constructs and returns a bitset of the gamefield that does not
	 * differentiate between symbols. Occupied slots are denoted by true, unoccupied
	 * slots by false.
	 * 
	 * @return a representation of the complete GameField as a bitset
	 */
	private BitSet getCompleteGameFieldBitBoard() {
		BitSet completeBitboard = (BitSet) this.bitboardO.clone();
		completeBitboard.or(bitboardX);
		return completeBitboard;
	}

	/**
	 * Returns an int array containing all actions that are legal in the current
	 * state of the gamefield. An action is denoted by the index of the slot it
	 * fills. Legal actions are therefore the indexes of the unoccupied slots on the
	 * gamefield. If the game has ended no legal actions are available thus an empty
	 * array is returned
	 * 
	 * @return array containing all currently legal actions, empty if no legal are
	 *         actions possible
	 */
	public int[] getLegalActions() {
		if (this.hasGameEnded()) {
			return new int[0];
		}
		BitSet invertedCompleteBitBoard = this.getCompleteGameFieldBitBoard();
		invertedCompleteBitBoard.flip(0, 9);

		return invertedCompleteBitBoard.stream().toArray();
	}

	/**
	 * Checks if the passed action is legal, i.e. if the slot is currently not
	 * occupied
	 * 
	 * @param action to be checked for legality
	 * @return true if action is legal
	 */
	private boolean isActionLegal(int action) {
		return Arrays.binarySearch(this.getLegalActions(), action) >= 0;
	}

	/**
	 * Applies the passed action for the passed symbol after checking whether it is
	 * legal in the current state of the game. Method also updates and returns the
	 * current GameStatus
	 * 
	 * @param symbol for the action to be applied
	 * @param action to apply for the given symbol
	 * @return the status of the game after the action was applied for the given
	 *         symbol
	 */
	public GameStatus applyAction(Symbol symbol, int action) {

		if (this.hasGameEnded()) {
			throw new IllegalStateException("no action permitted as game ended with status: " + this.currentGameStatus);
		}

		if (!isActionLegal(action)) {
			throw new IllegalArgumentException(
					"passed action " + action + " is not legal; current gamestate is " + this.getState());
		}

		if (symbol.isX()) {
			this.bitboardX.set(action);
		} else {
			this.bitboardO.set(action);
		}

		return this.updateGameStatus(symbol);
	}

	/**
	 * Undoes the specified action of the passed symbol by setting the bit/index in
	 * the corresponding bitset position to false. Afterwards the GameStatus is
	 * updated and returned
	 * 
	 * @param symbol for that the action should be undone
	 * @param action that to be undone
	 */
	public GameStatus undoAction(Symbol symbol, int action) {

		if (symbol.isX()) {
			this.bitboardX.set(action, false);
		} else {
			this.bitboardO.set(action, false);
		}

		this.currentGameStatus = GameStatus.ON_GOING;
		return this.currentGameStatus;
	}

	/**
	 * Returns whether the game has ended or not
	 * 
	 * @return true if the game has ended with in a draw or a win for either player
	 */
	public boolean hasGameEnded() {
		return !(this.currentGameStatus == GameStatus.INITIALIZED || this.currentGameStatus == GameStatus.ON_GOING);
	}

	/**
	 * Updates and returns the gamestatus based on the bitset for the passed symbol.
	 * Therefore this method is to be called after the passed symbol has made an
	 * action
	 * 
	 * @param symbol that last made an action and thus should have its bitboard
	 *               checked for win conditions
	 * @return status of the game
	 */
	private GameStatus updateGameStatus(Symbol symbol) {

		BitSet bitsetToCheck;
		if (symbol.isX()) {
			bitsetToCheck = (BitSet) this.bitboardX.clone();
		} else {
			bitsetToCheck = (BitSet) this.bitboardO.clone();
		}

		boolean winningPatternFound = checkBitSetForWinPatterns(bitsetToCheck);

		if (winningPatternFound && !symbol.isX()) {
			this.currentGameStatus = GameStatus.WIN_O;
		} else if (winningPatternFound && symbol.isX()) {
			this.currentGameStatus = GameStatus.WIN_X;
		} else if (this.getCompleteGameFieldBitBoard().cardinality() == GAMEFIELD_SIZE) {
			this.currentGameStatus = GameStatus.DRAW;
		} else {
			this.currentGameStatus = GameStatus.ON_GOING;
		}
		return this.currentGameStatus;
	}

	/**
	 * Checks whether the passed bitset contains one of the eight win patterns. This
	 * is done by iterating through the patterns, AND joining each with the biset
	 * and comparing for equality with the original pattern
	 * 
	 * @param bitsetToCheck for win patterns
	 * @return true if the passed bitset contains a win pattern
	 */
	private boolean checkBitSetForWinPatterns(BitSet bitsetToCheck) {
		boolean winningPatternFound = false;
		for (int i = 0; i < WIN_PATTERNS.length && !winningPatternFound; i++) {
			BitSet winPattern = BitSet.valueOf(WIN_PATTERNS[i]);
			BitSet winPatternVerificationMask = BitSet.valueOf(WIN_PATTERNS[i]);
			winPattern.and(bitsetToCheck);
			winningPatternFound = winPattern.equals(winPatternVerificationMask);
		}
		return winningPatternFound;
	}

	/**
	 * Resets the bitboards for both symbols by setting all bits to false
	 */
	public void resetGameField() {
		this.bitboardX.clear();
		this.bitboardO.clear();
		this.currentGameStatus = GameStatus.INITIALIZED;
	}

	/**
	 * State representation as int that is at most 18 bits long. The returned value
	 * in binary notation denotes the configuration of the entire gamefield. The
	 * first nine bits denote the configuration for X, the last nine for O
	 * 
	 * @return int representing the current state of the gamefield
	 */
	public int getState() {

		int stateAsInt = 0;
		int bitBoardXASInt = Gamefield.convertBitBoardToInt(this.bitboardX);
		int bitBoardOASInt = Gamefield.convertBitBoardToInt(this.bitboardO);

		// bitshift the state number for X by the symbolspecific offset to avoid overlap
		int bitBoardXWithOffset = bitBoardXASInt << getSymbolSpecificOffset(Symbol.SYMBOL_X);

		stateAsInt = bitBoardXWithOffset + bitBoardOASInt;
		return stateAsInt;
	}

	/**
	 * Returns the current status of the game
	 * 
	 * @return
	 */
	public GameStatus getCurrentGameStatus() {
		return currentGameStatus;
	}

	/**
	 * Returns the current state of the gamefield encoded as string with the chars
	 * "X", "O" for the respective players and "-" for unoccupied slots
	 * 
	 * @return state represented as a string
	 */
	public String toString() {
		StringBuilder gameFieldStringRepresentataion = new StringBuilder("-".repeat(GAMEFIELD_SIZE));

		for (int i = 0; i < Gamefield.GAMEFIELD_SIZE; i++) {
			if (this.bitboardX.get(i)) {
				gameFieldStringRepresentataion.setCharAt(i, 'X');
			}
			if (this.bitboardO.get(i)) {
				gameFieldStringRepresentataion.setCharAt(i, 'O');
			}
		}
		return gameFieldStringRepresentataion.toString();
	}

	/**
	 * Returns the number of plies that have occurred to reach the passed state This
	 * method does not differentiate between symbols. The number of plies is equal
	 * to the bits set to true in the passed state
	 * 
	 * @param state for that the number of plies is to be returned
	 * @return number of plies to reach the passed state
	 */
	public static int getNumberOfPlies(int state) {
		return Integer.bitCount(state);
	}

	/**
	 * Returns the offset to the passed symbol. The offset is necessary to ensure
	 * that no overlap occurs when the state is calculated or actions are applied
	 * 
	 * @param symbol
	 * @return
	 */
	public static int getSymbolSpecificOffset(Symbol symbol) {
		if (symbol.isX()) {
			return GAMEFIELD_SIZE;
		} else {
			return 0;
		}
	}

	/**
	 * Returns an int array containing all legal actions, ie. unoccupied gamefield
	 * slots, available in the passed state
	 * 
	 * The actions are derived by creating a bitmap that represents the occupied and
	 * unoccupied slots on the board, ie. the bits for X are RSHIFT by 9 and OR
	 * joined with the bits for O. The resulting state (int) is used to initialise a
	 * bitboard to get the unset bits and thus unoccupied slots
	 * 
	 * @param state for that the legal actions are to be calculated
	 * @return int array containing all legal actions
	 */
	public static int[] getlegalActionsToState(int state) {
		// this method also works with illegal states ie. 262143 where all 18 bits are
		// set to 1
		ArrayList<Integer> legalActions = new ArrayList<>();
		int stateShiftedXBits = state >>> GAMEFIELD_SIZE;
		int joinedState = state | stateShiftedXBits;

		BitSet joinedStateAsBitset = BitSet.valueOf(new long[] { joinedState });
		for (int i = 0; i < GAMEFIELD_SIZE; i++) {
			if (!joinedStateAsBitset.get(i))
				legalActions.add(i);
		}
		return Utility.convertListToArray(legalActions);
	}

	/**
	 * Checks whether the passed action is legal in the passed state by verifying
	 * that the index of the slot that the action would occupy is 0 for both symbols
	 * 
	 * @param state  that the action is to checked against
	 * @param action to be examined
	 * @return true if action is legal in given state
	 */
	public static boolean isActionLegal(int state, int action) {
		BitSet completeBitBoard = BitSet.valueOf(new long[] { state });

		boolean isBitSetForX = completeBitBoard.get(action + Gamefield.getSymbolSpecificOffset(Symbol.SYMBOL_X));
		boolean isBitSetForO = completeBitBoard.get(action + Gamefield.getSymbolSpecificOffset(Symbol.SYMBOL_O));

		return !(isBitSetForX || isBitSetForO);
	}

	/**
	 * Applies the action to the specified state. The symbol for who the action is
	 * to be applied for is calculated based on the number of already occupied slots
	 * 
	 * @param state  that the action is to be applied to
	 * @param action to be applied to the passed state
	 * @return returns the resulting state encoded as long
	 */
	public static int applyAction(int state, int action) {

		if (!isActionLegal(state, action)) {
			throw new IllegalArgumentException("action " + action + " is not legal in state " + state);
		}

		Symbol currentSymbol = getCurrentTurnsSymbol(state);
		int symbolSpecificOffset = Gamefield.getSymbolSpecificOffset(currentSymbol);
		int encodedAction = Double.valueOf(Math.pow(2, (action + symbolSpecificOffset))).intValue();

		int stateAfterAction = state + encodedAction;
		return stateAfterAction;
	}

	/**
	 * Derives the symbol whose turn it is based on the number of occupied slots,
	 * i.e. set bits. Since X always starts the game it is Xs turn if an even number
	 * of slots is occupied
	 * 
	 * @param state for that the current symbol is to be calculated
	 * @return Symbol whose turn it is
	 */
	public static Symbol getCurrentTurnsSymbol(int state) {
		if (Integer.bitCount(state) % 2 == 0) {
			return Symbol.SYMBOL_X;
		} else {
			return Symbol.SYMBOL_O;
		}
	}

	/**
	 * Converts the passed bitset to a long that is at most nine digits long. The
	 * long is calculated through addition of the set bits where the index is used
	 * as the exponent of 10 thus the top-left slot/0-th bit is the rightmost digit
	 * 
	 * @param bitsetToConvert
	 * @return
	 */
	private static int convertBitBoardToInt(BitSet bitsetToConvert) {

		if (bitsetToConvert.isEmpty()) {
			return 0;
		}

		if (bitsetToConvert.length() > GAMEFIELD_SIZE) {
			throw new IllegalArgumentException(
					"passed bitset has atleast one bit set outside of the range of the gamefield and is thus invalid");
		}

		// If the bitset has only bits set inside gamefield range it can be converted to
		// and stored as an int
		long bitBoardAsLong = bitsetToConvert.toLongArray()[0];
		int bitBoardAsInt = Long.valueOf(bitBoardAsLong).intValue();
		return bitBoardAsInt;
	}

	/**
	 * Returns the passed state as a string with the chars "X", "O" for the
	 * respective players and "-" for unoccupied slots
	 * 
	 * @return state represented as a string
	 */
	public static String convertStateAsIntToString(int state) {
		StringBuilder gameFieldStringRepresentataion = new StringBuilder("-".repeat(GAMEFIELD_SIZE));
		BitSet completeBitBoard = BitSet.valueOf(new long[] { state });

		for (int i = 0; i < Gamefield.GAMEFIELD_SIZE; i++) {
			if (completeBitBoard.get(i)) {
				gameFieldStringRepresentataion.setCharAt(i, 'O');
			}
		}
		for (int i = 0; i < Gamefield.GAMEFIELD_SIZE; i++) {
			if (completeBitBoard.get(i + GAMEFIELD_SIZE)) {
				gameFieldStringRepresentataion.setCharAt(i, 'X');
			}
		}
		return gameFieldStringRepresentataion.toString();
	}

}
