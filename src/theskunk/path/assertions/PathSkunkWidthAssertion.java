package theskunk.path.assertions;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathSkunkWidthAssertion implements Assertion {
	private int _expectedWidth;
	
	public PathSkunkWidthAssertion(int expectedWidth) {
		this._expectedWidth = expectedWidth;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		return player.getSkunkWidth() == this._expectedWidth;
	}

	@Override
	public String toString() {
		return String.format("<SkunkWidthAssertion> (expected=%d)", this._expectedWidth);
	}
}
