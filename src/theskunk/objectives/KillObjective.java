package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.environment.Environment;
import theskunk.path.Path;

public class KillObjective implements Objective {
	
	@Override
	public int compareTo(Objective otherObject) {
		if (this.priority() < otherObject.priority())
			return -1;
		else if (this.priority() > otherObject.priority())
			return 1;
		return 0;
	}

	@Override
	public void evaluate(Environment env, ExecutionState state) {
		
	}

	@Override
	public boolean isSatisfied() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int priority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Path path() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void becomesActive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resigns() {
		// TODO Auto-generated method stub

	}

}
