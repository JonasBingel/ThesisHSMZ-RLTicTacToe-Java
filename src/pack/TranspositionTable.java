package pack;

import java.util.HashMap;

public class TranspositionTable {

	private HashMap<Integer, Node> transTable;

	public TranspositionTable() {
		this.transTable = new HashMap<>();
	}

	public Node retrieveNode(int state) {
		return this.transTable.get(state);
	}

	public void putNode(int state, Node nodeToBeAdded) {
		this.transTable.put(state, nodeToBeAdded);
	}

	public int getSize() {
		return this.transTable.size();
	}

}
