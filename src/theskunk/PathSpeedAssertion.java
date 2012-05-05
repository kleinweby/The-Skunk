package theskunk;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathSpeedAssertion extends PathAssertion {
	private int _expectedSpeed;
	
	public PathSpeedAssertion(int speed) {
		this._expectedSpeed = speed;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		return player.getMSForOneTile() == this._expectedSpeed;
	}

}
