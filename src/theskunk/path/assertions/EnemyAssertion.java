package theskunk.path.assertions;

import java.awt.Point;

import apoSkunkman.ai.ApoSkunkmanAIEnemy;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class EnemyAssertion implements Assertion {
	private int _enemyID;
	private Point _expectedPoint;
	private int _expectedTolerance;
	
	public EnemyAssertion(ApoSkunkmanAIEnemy enemy, Point expectedPoint, int tolerance) {
		this._enemyID = enemy.getPlayer();
		this._expectedPoint = expectedPoint;
		this._expectedTolerance = tolerance;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		ApoSkunkmanAIEnemy enemy = this.enemy(level);
		
		// Enemy does not exists or is dead.
		if (enemy == null || !enemy.isVisible())
			return false;
		
		Point p = new Point((int)enemy.getX(), (int)enemy.getY());
		
		if (p.x == this._expectedPoint.x && Math.abs(p.y - this._expectedPoint.y) <= this._expectedTolerance)
			return true;
		if (p.y == this._expectedPoint.y && Math.abs(p.x - this._expectedPoint.x) <= this._expectedTolerance)
			return true;
		
		return false;
	}

	private ApoSkunkmanAIEnemy enemy(ApoSkunkmanAILevel level) {		
		for (ApoSkunkmanAIEnemy e : level.getEnemies()) {
			if (e.getPlayer() == this._enemyID)
				return e;
		}
		
		return null;
	}
}
