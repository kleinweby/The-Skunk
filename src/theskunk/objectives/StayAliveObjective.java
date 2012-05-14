package theskunk.objectives;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;

import theskunk.ExecutionState;
import theskunk.environment.BombTileState;
import theskunk.environment.Environment;
import theskunk.path.Path;
import theskunk.path.PathFinder;
import theskunk.path.PathFinder.Type;
import theskunk.path.assertions.PathBushAssertion;
import theskunk.path.steps.LayBombStep;
import theskunk.path.steps.MoveStep;
import theskunk.path.steps.Step;
import theskunk.path.steps.WaitStep;
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
	public void evaluate(Environment env, ExecutionState state) {	
		List<Step> remainingSteps = null;
		
		if (state.currentObjective != null) {
			Path p = state.currentObjective.path();
			if (state.stepIndex < p.steps().size()) {
				remainingSteps = p.steps().subList(state.stepIndex, p.steps().size() - 1);
			}
		}
		
		if (this.isBombThreatinging(env) && this.isThreatendAlongPath(env, remainingSteps)) {
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

	private boolean isBombThreatinging(Environment env) {
		Point p = env.playerPosition();
		boolean found = false;
		
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
		
		return found;
	}
	
	private boolean isThreatendAlongPath(Environment env, List<Step> pathSteps) {
		
		if (pathSteps == null)
			return isBombThreatinging(env);
		
		for (Step step : pathSteps) {
			if (!env.isPlayerAlive())
				return true;
			
			if (!isBombThreatinging(env)) {
				return false;
			}
			
			env = new Environment(env, step);
		}
		
		return isBombThreatinging(env);
	}
}
