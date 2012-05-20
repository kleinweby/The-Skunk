package theskunk.path.assertions;

import java.awt.Point;


import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathBushAssertion implements Assertion {
	private boolean _shouldBeBush;
	private Point _location;
	
	public PathBushAssertion(Point location, boolean shouldBeBush) {
		this._location = location;
		this._shouldBeBush = shouldBeBush;
	}
	
	@Override
	public boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		return (level.getLevelAsByte()[this._location.y][this._location.x] == ApoSkunkmanAIConstants.LEVEL_BUSH) == this._shouldBeBush;
	}

	@Override
	public String toString() {
		return String.format("<PushAssertion> (location=%s, shouldBeBush=%b)", this._location.toString(), this._shouldBeBush);
	}
}
