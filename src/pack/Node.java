package pack;

import java.util.ArrayList;

public class Node {
	private int depth;
	private ArrayList<Integer> optimalActions;
	private float score;
	private Symbol symbol;

	public Node(Symbol symbol) {
		this.optimalActions = new ArrayList<>();
		this.symbol = symbol;
	}

	public ArrayList<Integer> getOptimalActions() {
		return optimalActions;
	}

	public void addOptimalAction(Integer action) {
		this.optimalActions.add(action);
	}

	public void clearBestActions() {
		this.optimalActions.clear();
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public float getScore() {
		return this.score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public Symbol getSymbol() {
		return this.symbol;
	}

	public void setSymbol(Symbol player) {
		this.symbol = player;
	}

}
