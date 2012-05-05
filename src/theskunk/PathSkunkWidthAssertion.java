package theskunk;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathSkunkWidthAssertion extends PathAssertion {
	private int _expectedWidth;
	
	public PathSkunkWidthAssertion(int expectedWidth) {
		this._expectedWidth = expectedWidth;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		return player.getSkunkWidth() == this._expectedWidth;
	}

}
