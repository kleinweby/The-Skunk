package theskunk;

import java.awt.Point;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathPlayerPositionAssertion extends PathAssertion {
	private Point _expectedLocation;
	
	public PathPlayerPositionAssertion(Point location) {
		this._expectedLocation = location;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		return this._expectedLocation.x == player.getPlayerX() && this._expectedLocation.y == player.getPlayerY();
	}
	
	@Override
	public String toString() {
		return String.format("<PlayerPositionAssertion> (expected=%s)", this._expectedLocation.toString());
	}
}
