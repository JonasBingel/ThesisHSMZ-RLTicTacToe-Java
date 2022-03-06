package pack;

public enum Symbol {
	SYMBOL_X, SYMBOL_O;

	/**
	 * Gets the next symbol that follows the passed symbol
	 * 
	 * @param currentSymbol symbol for that the next symbol is to be returned
	 * @return next symbol to move
	 */
	public static Symbol getNextSymbol(Symbol currentSymbol) {
		if (currentSymbol == Symbol.SYMBOL_X) {
			return Symbol.SYMBOL_O;
		} else {
			return Symbol.SYMBOL_X;
		}
	}

	/**
	 * Returns true if the symbol is X
	 * 
	 * @return true if symbol is X otherwise false
	 */
	public boolean isX() {
		return this == Symbol.SYMBOL_X;
	}

}
