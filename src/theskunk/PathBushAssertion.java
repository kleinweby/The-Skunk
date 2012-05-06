package theskunk;

import java.awt.Point;

import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathBushAssertion extends PathAssertion {
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

}