package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.environment.Environment;
import theskunk.path.Path;

public interface Objective extends Comparable<Objective> {
	public void evaluate(Environment env, ExecutionState state);
	public boolean isSatisfied();
	public int priority();
	public Path path();
	
	public void becomesActive();
	public void resigns();
}
