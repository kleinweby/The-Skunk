package theskunk.path.assertions;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathSpeedAssertion implements Assertion {
	private int _expectedSpeed;
	
	public PathSpeedAssertion(int speed) {
		this._expectedSpeed = speed;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		return player.getMSForOneTile() == this._expectedSpeed;
	}

	@Override
	public String toString() {
		return String.format("<SpeedAssertion> (expected=%d)", this._expectedSpeed);
	}
}
