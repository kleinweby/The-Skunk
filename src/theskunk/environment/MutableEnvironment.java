package theskunk.environment;

import java.awt.Point;

import theskunk.PathStep;

public class MutableEnvironment extends Environment {

	public MutableEnvironment() {
		super();
	}
	
	public MutableEnvironment(Environment parent) {
		super(parent);
	}
	
	public MutableEnvironment(Environment parent, PathStep step) {
		super(parent, step);
	}

	// Make some interfaces public
	public void setPlayerPosition(Point position) {
		super.setPlayerPosition(position);
	}
	
	public void updateTileState(TileState state) {
		super.updateTileState(state);
	}
	
	public void setMiliTimeForTile(int time) {
		super.setMiliTimeForTile(time);
	}
	
	public void setSkunkWidth(int width) {
		super.setSkunkWidth(width);
	}
	
	public void setMaxSkunks(int maxSkunks) {
		super.setMaxSkunks(maxSkunks);
	}
	
	public void setCurrentTime(int currentTime) {
		assert !this._hasChildren;
		
		this._currentTime = currentTime;
	}
}
