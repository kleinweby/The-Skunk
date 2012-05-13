package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.Path;
import apoSkunkman.ai.*;

public interface Objective extends Comparable<Objective> {
	public void evaluate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player, ExecutionState state);
	public boolean isSatisfied();
	public int priority();
	public Path path();
	
	// Feedback
	public void pathFailed();
}
