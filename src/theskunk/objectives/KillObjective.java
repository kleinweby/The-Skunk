package theskunk.objectives;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import theskunk.ExecutionState;
import theskunk.environment.Environment;
import theskunk.path.Finder;
import theskunk.path.Path;
import theskunk.path.steps.Step;
import theskunk.path.assertions.Assertion;
import theskunk.path.assertions.EnemyAssertion;
import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAIEnemy;

public class KillObjective implements Objective {
	private boolean _isSatisfied;
	// Lock on one player and try killing him
	// till succeeded or died doing so
	private int _currentPursuingPlayer;
	private Path _path;
	
	public KillObjective() {
		this._currentPursuingPlayer = -1;
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
	public void evaluate(Environment env, ExecutionState state) {
		ApoSkunkmanAIEnemy enemy = this.chooseEnemy(env, state);

		if (enemy != null) {
			if (this._path == null || !this._path.assertAgainstApo(state.level, state.player)) {
				if (state.player.canPlayerLayDownSkunkman()) {
					Finder finder = new Finder(env, 
							Finder.Type.BombAway, (int)enemy.getX(), (int)enemy.getY());
					
					Path p = finder.solution();
					List<Assertion> assertions = p.assertions();
					assertions.add(new EnemyAssertion(enemy, new Point((int)enemy.getX(), (int)enemy.getY()), env.skunkWidth()));
					this._path = new Path(p.steps(), assertions, p.finalState(), p.startPlayerPosition(), p.finalPlayerPosition());
				}
				else {
					this._path = new Path(new ArrayList<Step>(), new ArrayList<Assertion>(), env, env.playerPosition(), env.playerPosition());
				}
			}
		}
		else {
			this._isSatisfied = true;
		}
	}

	private ApoSkunkmanAIEnemy chooseEnemy(Environment env, ExecutionState state) {
		if (state.level.getEnemies().length == 0 || state.level.getType() == ApoSkunkmanAIConstants.LEVEL_TYPE_GOAL_X)
			return null;
		
		ApoSkunkmanAIEnemy enemy = state.level.getEnemies()[0];
		
		for (ApoSkunkmanAIEnemy e : state.level.getEnemies()) {
			// Found the one we're currently pursuing
			if (e.getPlayer() == this._currentPursuingPlayer) {
				enemy = e;
				break;
			}
			// We don't have a victem yet, use the one nearest to us.
			else if (this._currentPursuingPlayer < 0) {
				int currentDistance = (int) (Math.pow(enemy.getX() - env.playerPosition().x, 2) + Math.pow(enemy.getY() - env.playerPosition().y, 2));
				int distance = (int) (Math.pow(e.getX() - env.playerPosition().x, 2) + Math.pow(e.getY() - env.playerPosition().y, 2));
				
				if (distance < currentDistance) {
					enemy = e;
				}
			}
		}
		
		this._currentPursuingPlayer = enemy.getPlayer();
		
		return enemy;
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
	public void becomesActive() {
	}

	@Override
	public void resigns() {
		this._path = null;
	}
}
