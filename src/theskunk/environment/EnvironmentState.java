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
	
	public static int FIELD_WIDTH = 15;
	public static int FIELD_HEIGHT = 15;
	
	public EnvironmentState(EnvironmentState parent, int timeAdvance) {
		this._parentState = parent;
		
		
		if (this._parentState != null) {
			this._currentTime = this._parentState.currentTime();
			this._tiles = new TileState[FIELD_WIDTH][];
			
			for (int x = 0; x < FIELD_WIDTH; x++)
				this._tiles[x] = this._parentState._tiles[x].clone();
			
			this._bombTiles = (HashSet<BombTileState>) this._parentState._bombTiles.clone();
			
			this._miliTimeForTile = this._parentState._miliTimeForTile;
			this._skunkWidth = this._parentState._skunkWidth;
			this._maxSkunks = this._parentState._maxSkunks;
		}
		else {
			this._tiles = new TileState[FIELD_WIDTH][FIELD_HEIGHT];
			this._bombTiles = new HashSet<BombTileState>();
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
		
		prevState = this._tiles[state.x()][state.y()];
		
		if (prevState instanceof BombTileState) {
			this._bombTiles.remove(prevState);
		}
		
		if (state instanceof BombTileState) {
			BombTileState bomb = (BombTileState)state;
			bomb.setTimeLayed(this.currentTime());
			this._bombTiles.add(bomb);
		}
			
		this._tiles[state.x()][state.y()] = state;
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
			this.updateTileState(new BombTileState(this.playerPosition().x, this.playerPosition().y, this.skunkWidth()));
		}
		else if (step instanceof PathMoveStep) {
			PathMoveStep move = (PathMoveStep)step;
			
			switch (move.direction()) {
			case Up:
				this._playerPosition = new Point(this.playerPosition().x, this.playerPosition().y - 1);
				break;
			case Down:
				this._playerPosition = new Point(this.playerPosition().x, this.playerPosition().y + 1);
				break;
			case Left:
				this._playerPosition = new Point(this.playerPosition().x - 1, this.playerPosition().y);
				break;
			case Right:
				this._playerPosition = new Point(this.playerPosition().x + 1, this.playerPosition().y);
				break;
			}
		}
		
		this._step = step;
	}
	
	public Point playerPosition() {
		if (this._playerPosition != null)
			return this._playerPosition;
		else if (this._parentState != null)
			return this._parentState.playerPosition();
		
		throw new RuntimeException("Player position unknown!?");
	}
	
	public void setPlayerPosition(Point position) {
		this._playerPosition = position;
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
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, x, bomb.y()));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// walk x downwards
				for (int x = bomb.x() - 1; x > 0 && x >= bomb.x() - bomb.width(); x--) {
					TileState state = this.tileStateAt(x, bomb.y());
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, x, bomb.y()));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// walk y upwards
				for (int y = bomb.y() + 1; y < FIELD_HEIGHT && y <= bomb.y() + bomb.width(); y++) {
					TileState state = this.tileStateAt(bomb.x(), y);
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, bomb.x(), y));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// walk y downwards
				for (int y = bomb.y() - 1; y > 0 && y >= bomb.y() - bomb.width(); y--) {
					TileState state = this.tileStateAt(bomb.x(), y);
					
					if (state.tileType() == TileState.BushTileType) {
						this.updateTileState(new TileState(TileState.FreeTileType, bomb.x(), y));
						// Only bomb one tile away
						break;
					}
					else if (state.tileType() == TileState.StoneTileType) {
						break;
					}
				}
				
				// Remove the bomb
				this.updateTileState(new TileState(TileState.FreeTileType, bomb.x(), bomb.y()));
			}
		}
	}
}
