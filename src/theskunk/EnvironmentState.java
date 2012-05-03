package theskunk;

import java.util.HashMap;
import java.util.HashSet;

class TileAlreadyChanged extends Exception {
	private static final long serialVersionUID = 1506153837316228004L;
}

class EnvironmentState {
	EnvironmentState _parentState;
	HashMap<Integer, TileState> _changedTileStates;
	int _miliTimeForTile;
	int _skunkWidth;
	int _maxSkunks;
	int _currentTime;
	
	public EnvironmentState(EnvironmentState parent, int timeAdvance) {
		this._parentState = parent;
		
		this._changedTileStates = new HashMap<Integer, TileState>();
		
		if (this._parentState != null)
			this._currentTime = this._parentState.currentTime();
		
		this._currentTime += timeAdvance;
		this._miliTimeForTile = -1;
		this._skunkWidth = -1;
		this._maxSkunks = -1;
		
		this.simulateEnvironment();
	}
	
	// Get state
	public TileState tileStateAt(int x, int y) {
		assert x < 0xFF;
		assert y < 0xFF;
		
		Integer mangeldTileName = (x << 8) | (y & 0xFF);
		
		if (this._changedTileStates.containsKey(mangeldTileName))
			return this._changedTileStates.get(mangeldTileName);
		else if (this._parentState != null)
			return this._parentState.tileStateAt(x, y);
		
		throw new RuntimeException("This environment state does not contain a tile for these coordinates and has no parent!");
	}
	
	public int miliTimeForTile() {
		if (this._miliTimeForTile > 0)
			return this._miliTimeForTile;
		else if (this._parentState != null)
			return this._parentState.miliTimeForTile();
		
		throw new RuntimeException("miliTimeForTile accessed but never set!");
	}
	
	public int skunkWidth() {
		if (this._skunkWidth > 0)
			return this._skunkWidth;
		else if (this._parentState != null)
			return this._parentState.skunkWidth();
		
		throw new RuntimeException("skunkWidth accessed but never set!");
	}
	
	public int maxSkunks() {
		if (this._maxSkunks > 0)
			return this._maxSkunks;
		else if (this._parentState != null)
			return this._parentState.maxSkunks();
		
		throw new RuntimeException("maxSkunks accessed but never set!");
	}
	
	public int currentTime() {
		return this._currentTime;
	}
	
	public HashSet<TileState> bombTiles() {
		HashSet<TileState> bombList = new HashSet<TileState>();
		
		// TODO: blabla
		
		return bombList;
	}
	
	// Modify state
	public void updateTileState(TileState state) {
		assert state != null;
		
		Integer mangeldTileName = (state.x() << 8) | (state.y() & 0xFF);
		
		if (!this._changedTileStates.containsKey(mangeldTileName))
			this._changedTileStates.put(mangeldTileName, state);
	}

	public void setMiliTimeForTile(int time) {
		assert time > 0;
		
		this._miliTimeForTile = time;
	}

	public void setSkunkWidth(int width) {
		assert width > 0;
		
		this._skunkWidth = width;
	}

	public void setMaxSkunks(int maxSkunks) {
		assert maxSkunks > 0;
		
		this._maxSkunks = maxSkunks;
	}
	
	private void simulateEnvironment() {
		// TODO: bombs and so
	}
}
