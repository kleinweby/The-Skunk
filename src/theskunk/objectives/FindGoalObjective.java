package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.environment.Environment;
import theskunk.path.Path;
import theskunk.path.Finder;
import apoSkunkman.ai.ApoSkunkmanAIConstants;

public class FindGoalObjective implements Objective {
	private Path _path;
	private boolean _isSatisfied;
	
	@Override
	public void evaluate(Environment env, ExecutionState state) {
		if (state.level.getType() != ApoSkunkmanAIConstants.LEVEL_TYPE_GOAL_X) {
			this._path = null;
			this._isSatisfied = true;
		}
		else if (!(this._path != null && this._path.assertAgainstApo(state.level, state.player))){
			Finder finder = new Finder(env, 
					Finder.Type.FindGoal, state.level.getGoalXPoint().x, state.level.getGoalXPoint().y);
			
			this._path = finder.solution();
			this._isSatisfied = false;
		}
	}

	@Override
	public boolean isSatisfied() {
		return this._isSatisfied;
	}

	@Override
	public int priority() {
		return 0;
	}

	@Override
	public Path path() {
		return this._path;
	}

	@Override
	public int compareTo(Objective otherObject) {
		if (this.priority() < otherObject.priority())
			return -1;
		else if (this.priority() > otherObject.priority())
			return 1;
		return 0;
	}

	@Override
	public void becomesActive() {
		// Not interested
	}

	@Override
	public void resigns() {
		this._path = null;
	}

	@Override
	public String toString() {
		if (this._path != null)
			return String.format("FindGoal(isSatisfied=%b, steps=%d)", this._isSatisfied, this._path.steps().size());
		return String.format("FindGoal(isSatisfied=%b)", this._isSatisfied);
	}
}
