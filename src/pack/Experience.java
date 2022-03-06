package pack;

import java.io.Serializable;
import java.util.HashMap;

public interface Experience extends Serializable {
	static final double QVALUE_TERMINAL_STATE = 0d;

	void initialiseQTableEntryIfNotExistent(int state, int[] legalActions);

	void updateQTableValue(int state, int action, double newQValue);

	int getBestAction(int state, int[] legalActions);

	double getQValueOfTerminalState();

	double getQValue(int state, int action);

	int getNumberOfDistinctVisitedStates();
	
	HashMap<Integer, Double> getActionQValueMapForState(int state);

}
