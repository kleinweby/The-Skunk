package theskunk.objectives;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;

import theskunk.ExecutionState;
import theskunk.Path;
import theskunk.PathBushAssertion;
import theskunk.PathFinder;
import theskunk.PathLayBombStep;
import theskunk.PathMoveStep;
import theskunk.PathStep;
import theskunk.PathWaitStep;
import theskunk.PathFinder.Type;
import theskunk.environment.BombTileState;
import theskunk.environment.EnvironmentState;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class StayAliveObjective implements Objective {
	private Path _path;
	private boolean _isSatisfied;
	
	@Override
	public int compareTo(Objective otherObject) {
		if (this.priority() < otherObject.priority())
			return -1;
		else if (this.priority() > otherObject.priority())
			return 1;
		return 0;
	}

	@Override
	public void evaluate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player, ExecutionState state) {
		EnvironmentState env = PathFinder.environmentFromApo(level, player);
		
		if (this.isBombThreatinging(env, state)) {
			PathFinder finder = new PathFinder(env, Type.AvoidBomb, 0, 0);
			
			this._isSatisfied = false;
			this._path = finder.solution();
		}
		else if (state.currentObjective != this) {
			this._path = null;
			this._isSatisfied = true;
		}
	}

	@Override
	public boolean isSatisfied() {
		return this._isSatisfied;
	}

	@Override
	public int priority() {
		// Staying alive is the most imporant thing ;)
		return -100;
	}

	@Override
	public Path path() {
		return this._path;
	}

	@Override
	public void pathFailed() {
		this._path = null;
	}

	private boolean isBombThreatinging(EnvironmentState env, ExecutionState state) {
		Point p = env.playerPosition();
		boolean found = false;
		
		// Make a shadow copy to not change the calling env
		env = new EnvironmentState(env, 0);
		
		for (BombTileState bomb : env.bombTiles()) {
			Point b = bomb.coordinate();
			
			// TODO: check if there is already a bush/stone
			// that would mark us save =)
			if (p.y == b.y) {
				if (Math.abs(p.x - b.x) <= bomb.width()) {
					found = true;
					break;
				}
			}
			else if (p.x == b.x) {
				if (Math.abs(p.y - b.y) <= bomb.width()) {
					found = true;
					break;
				}
			}
		}
		
		// We have points, now look whether the current path will avoid them
		if (found && state.currentObjective != null) {
			List<PathStep> steps = state.currentObjective.path().steps();
			
			if (state.stepIndex >= steps.size())
				return true;
			
			steps = steps.subList(state.stepIndex, steps.size() - 1);
			
			for (PathStep step : steps) {
				if (!env.isPlayerAlive())
					return true;
				
				if (env.bombTiles().isEmpty())
					return false;
				
				// Now move the env forward
				if (step instanceof PathMoveStep) {					
					env.setStep(step);
					env = new EnvironmentState(env, env.miliTimeForTile());
				}
				else if (step instanceof PathLayBombStep) {					
					env.setStep(step);
					env = new EnvironmentState(env, env.miliTimeForTile());
				}
				else if (step instanceof PathWaitStep) {	
					PathWaitStep wait = (PathWaitStep) step;
					env.setStep(step);
					env = new EnvironmentState(env, wait.duration());
				}
			}
			return true;
		}
		
		return found;
	}
}
