package theskunk.environment;

import java.awt.Point;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import theskunk.PathLayBombStep;
import theskunk.PathMoveStep;
import theskunk.PathStep;;


class TileAlreadyChanged extends RuntimeException {
	private static final long serialVersionUID = 1506153837316228004L;
}

public class EnvironmentState {
	EnvironmentState _parentState;
	TileState _tiles[][];
	HashSet<BombTileState> _bombTiles;
	PathStep _step;
	int _miliTimeForTile;
	int _skunkWidth;
	int _maxSkunks;
	int _currentTime;
	Point _playerPosition;
	private boolean _isPlayerAlive;
	
	public static int FIELD_WIDTH = 15;
	public static int FIELD_HEIGHT = 15;
	
	public EnvironmentState(EnvironmentState parent, int timeAdvance) {
		this._parentState = parent;
		
		
		if (this._parentState != null) {
			this._currentTime = this._parentState.currentTime();
			this._tiles = this._parentState._tiles;
			this._bombTiles = this._parentState._bombTiles;
			
			this._miliTimeForTile = this._parentState._miliTimeForTile;
			this._skunkWidth = this._parentState._skunkWidth;
			this._maxSkunks = this._parentState._maxSkunks;
			this._playerPosition = new Point(this._parentState._playerPosition);
			this._isPlayerAlive = this._parentState._isPlayerAlive;
		}
		else {
			this._tiles = new TileState[FIELD_WIDTH][FIELD_HEIGHT];
			this._bombTiles = new HashSet<BombTileState>();
			this._playerPosition = new Point();
			this._isPlayerAlive = true;
		}
		
		this._currentTime += timeAdvance;

		this.simulateEnvironment();
	}
	
	// Get state
	public TileState tileStateAt(int x, int y) {
		assert x >= 0 && x < FIELD_WIDTH;
		assert y >= 0 && x < FIELD_HEIGHT;
		
		TileState state;
		
		state = this._tiles[x][y];
		
		if (state == null)
			throw new RuntimeException("This environment state does not contain a tile for these coordinates and has no parent!");
		
		return state;
	}
	
	public int miliTimeForTile() {
		return this._miliTimeForTile;
	}
	
	public int skunkWidth() {
		return this._skunkWidth;
	}
	
	public int maxSkunks() {
		return this._maxSkunks;
	}
	
	public int currentTime() {
		return this._currentTime;
	}
	
	public HashSet<BombTileState> bombTiles() {
		return this._bombTiles;
	}
	
	// Modify state
	public void updateTileState(TileState state) {
		assert state != null;
		TileState prevState;
		int x = state.coordinate().x;
		int y = state.coordinate().y;
		
		prevState = this._tiles[x][y];
		
		if (prevState instanceof BombTileState || state instanceof BombTileState) {
			// When we got the bomb tiles from the parent
			// state and it is the same object
			// we need to copy it here, in order not to change
			// the parent
			if (this._parentState != null && this._parentState._bombTiles == this._bombTiles)
				this._bombTiles = new HashSet<BombTileState>(this._bombTiles);
		}
		
		if (prevState instanceof BombTileState) {			
			this._bombTiles.remove(prevState);
		}
		
		if (state instanceof BombTileState) {
			BombTileState bomb = (BombTileState)state;
			bomb.setTimeLayed(this.currentTime());
			this._bombTiles.add(bomb);
		}
		
		// Inhered parend state and did not copy it yet
		if (this._parentState != null && this._tiles == this._parentState._tiles) {
			this._tiles = this._tiles.clone();
		}
		
		// Inhered parent state and did not copy column
		if (this._parentState != null && this._tiles[x] == this._parentState._tiles[x]) {
			this._tiles[x] = this._tiles[x].clone();
		}
		
		this._tiles[x][y] = state;
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
	
	// Returns the steps of this env object
	// Does now go up like the others
	public List<PathStep> steps() {
		LinkedList<PathStep> steps = new LinkedList<PathStep>();
		
		EnvironmentState env = this;
		
		while (env != null) {
			if (env._step != null)
				steps.addFirst(env._step);
			
			env = env._parentState;
		}
		
		return steps;
	}
	
	public void setStep(PathStep step) {
		if (this._step != null)
			throw new RuntimeException("This env already has an step set!");
		
		if (step instanceof PathLayBombStep) {
			// Set the tile to be a bomb
			this.updateTileState(new BombTileState(this.playerPosition(), this.skunkWidth()));
		}
		else if (step instanceof PathMoveStep) {
			PathMoveStep move = (PathMoveStep)step;
			
			switch (move.direction()) {
			case Up:
				this._playerPosition.y -= 1;
				break;
			case Down:
				this._playerPosition.y += 1;
				break;
			case Left:
				this._playerPosition.x -= 1;
				break;
			case Right:
				this._playerPosition.x += 1;
				break;
			}
		}
		
		this._step = step;
	}
	
	public Point playerPosition() {
		return this._playerPosition;
	}
	
	public void setPlayerPosition(Point position) {
		this._playerPosition = position;
	}
	
	public boolean isPlayerAlive()
	{
		return this._isPlayerAlive;
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
				Point p = bomb.coordinate();
				
				// Now let this thing explode
				// First walk x upwards
				for (int x = p.x + 1; x < FIELD_WIDTH && x <= p.x + bomb.width(); x++) {
					TileState state = this.tileStateAt(x, p.y);
					
					if (this._playerPosition.equals(new Point(x, p.y)))
						this._isPlayerAlive = false;
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, new Point(x, p.y)));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// walk x downwards
				for (int x = p.x - 1; x > 0 && x >= p.x - bomb.width(); x--) {
					TileState state = this.tileStateAt(x, p.y);
					
					if (this._playerPosition.equals(new Point(x, p.y)))
						this._isPlayerAlive = false;
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, new Point(x, p.y)));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// walk y upwards
				for (int y = p.y + 1; y < FIELD_HEIGHT && y <= p.y + bomb.width(); y++) {
					TileState state = this.tileStateAt(p.x, y);
					
					if (this._playerPosition.equals(new Point(p.x, y)))
						this._isPlayerAlive = false;
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, new Point(p.x, y)));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// walk y downwards
				for (int y = p.y - 1; y > 0 && y >= p.y - bomb.width(); y--) {
					TileState state = this.tileStateAt(p.x, y);
					
					if (this._playerPosition.equals(new Point(p.x, y)))
						this._isPlayerAlive = false;
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, new Point(p.x, y)));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// Remove the bomb
				this.updateTileState(new TileState(TileState.FreeTileType, p));
			}
		}
	}
}
