package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.Path;
import theskunk.environment.EnvironmentState;
import apoSkunkman.ai.*;

public interface Objective extends Comparable<Objective> {
	public void evaluate(EnvironmentState env, ExecutionState state);
	public boolean isSatisfied();
	public int priority();
	public Path path();
	
	// Feedback
	public void pathFailed();
}
