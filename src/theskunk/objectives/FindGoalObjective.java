package theskunk.objectives;

import theskunk.ExecutionState;
import theskunk.Path;
import theskunk.PathFinder;
import theskunk.environment.EnvironmentState;
import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class FindGoalObjective implements Objective {
	private Path _path;
	private boolean _isSatisfied;
	
	@Override
	public void evaluate(EnvironmentState env, ExecutionState state) {
		if (state.level.getType() != ApoSkunkmanAIConstants.LEVEL_TYPE_GOAL_X) {
			this._path = null;
			this._isSatisfied = true;
		}
		else if (!(this._path != null && this._path.assertAgainstApo(state.level, state.player))){
			PathFinder finder = new PathFinder(env, 
					PathFinder.Type.FindGoal, state.level.getGoalXPoint().x, state.level.getGoalXPoint().y);
			
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
	public void pathFailed() {
		this._path = null;
	}

}
