package theskunk;

import java.util.HashMap;
import java.util.HashSet;

class TileAlreadyChanged extends RuntimeException {
	private static final long serialVersionUID = 1506153837316228004L;
}

class EnvironmentState {
	EnvironmentState _parentState;
	HashMap<Integer, TileState> _changedTileStates;
	int _miliTimeForTile;
	int _skunkWidth;
	int _maxSkunks;
	int _currentTime;
	
	static int FIELD_WIDTH = 13;
	static int FIELD_HEIGHT = 13;
	
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
		assert x >= 0 && x < FIELD_WIDTH;
		assert y >= 0 && x < FIELD_HEIGHT;
		
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
	
	public HashSet<BombTileState> bombTiles() {
		HashSet<BombTileState> bombList = new HashSet<BombTileState>();
		
		// TODO: make this efficient
		for (int x = 0; x < FIELD_WIDTH; x++) {
			for (int y = 0; y < FIELD_WIDTH; y++) {
				if (x == 5 && y == 1)
					this.tileStateAt(x, y);
				
				TileState state = this.tileStateAt(x, y);
				
				if (state instanceof BombTileState)
					bombList.add((BombTileState)state);
			}
		}
		
		return bombList;
	}
	
	// Modify state
	public void updateTileState(TileState state) {
		assert state != null;
		
		Integer mangeldTileName = (state.x() << 8) | (state.y() & 0xFF);
		
		if (!this._changedTileStates.containsKey(mangeldTileName)) {
			// When a bomb is layed set its layed time
			// to know when it will explode
			if (state instanceof BombTileState) {
				BombTileState bomb = (BombTileState)state;
				bomb.setTimeLayed(this.currentTime());
			}
			
			this._changedTileStates.put(mangeldTileName, state);
		}
		else
			throw new TileAlreadyChanged();
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
		HashSet<BombTileState> bombs;
		
		// There is no parent state so we cannot simulate
		if (this._parentState == null)
			return;
		
		bombs = this.bombTiles();
		
		for (BombTileState bomb : bombs) {
			// This bomb exploded =/
			if (bomb.timeExploded() <= this.currentTime()) {
				// Now let this thing explode
				// First walk x upwards
				for (int x = bomb.x() + 1; x < FIELD_WIDTH && x <= bomb.x() + bomb.width(); x++) {
					TileState state = this.tileStateAt(x, bomb.y());
					
					if (state.tileType() != TileState.FreeTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, x, bomb.y()));
						// Only bomb one tile away
						break;
					}
				}
				
				// walk x downwards
				for (int x = bomb.x() - 1; x > 0 && x >= bomb.x() - bomb.width(); x--) {
					TileState state = this.tileStateAt(x, bomb.y());
					
					if (state.tileType() != TileState.FreeTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, x, bomb.y()));
						// Only bomb one tile away
						break;
					}
				}
				
				// walk y upwards
				for (int y = bomb.y() + 1; y < FIELD_HEIGHT && y <= bomb.y() + bomb.width(); y++) {
					TileState state = this.tileStateAt(bomb.x(), y);
					
					if (state.tileType() != TileState.FreeTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, bomb.x(), y));
						// Only bomb one tile away
						break;
					}
				}
				
				// walk y downwards
				for (int y = bomb.y() - 1; y > 0 && y >= bomb.y() - bomb.width(); y--) {
					TileState state = this.tileStateAt(bomb.x(), y);
					
					if (state.tileType() != TileState.FreeTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, bomb.x(), y));
						// Only bomb one tile away
						break;
					}
				}
				
				// Remove the bomb
				this.updateTileState(new TileState(TileState.FreeTileType, bomb.x(), bomb.y()));
			}
		}
	}
}
