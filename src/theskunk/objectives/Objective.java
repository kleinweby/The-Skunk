package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.Path;
import theskunk.environment.Environment;

public interface Objective extends Comparable<Objective> {
	public void evaluate(Environment env, ExecutionState state);
	public boolean isSatisfied();
	public int priority();
	public Path path();
	
	// Feedback
	public void pathFailed();
}
