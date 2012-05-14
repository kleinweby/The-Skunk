package theskunk.environment;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAILevelSkunkman;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.PathLayBombStep;
import theskunk.PathMoveStep;
import theskunk.PathStep;
import theskunk.PathWaitStep;


class TileAlreadyChanged extends RuntimeException {
	private static final long serialVersionUID = 1506153837316228004L;
}

public class Environment {
	protected Environment _parentState;
	protected TileState _tiles[][];
	protected HashSet<BombTileState> _bombTiles;
	protected PathStep _step;
	protected int _miliTimeForTile;
	protected int _skunkWidth;
	protected int _maxSkunks;
	protected int _currentTime;
	protected Point _playerPosition;
	protected boolean _isPlayerAlive;
	// Is true when it has become a parent
	// this is mainly for assertion to avoid
	// changing a parent env after a child has
	// been created
	protected boolean _hasChildren;
	
	public static int FIELD_WIDTH = 15;
	public static int FIELD_HEIGHT = 15;
	
	public static Environment envFromApo(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		MutableEnvironment env = new MutableEnvironment();
		
		env.setCurrentTime(level.getStartTime() - level.getTime());
		
		// Populate this state
		byte byteLevel[][] = level.getLevelAsByte();
		
		for (int x = 0; x < byteLevel.length; x++) {
			for (int y = 0; y < byteLevel[x].length; y++) {
				TileState tileState = null;
				Point p = new Point(x,y);
				
				switch (byteLevel[y][x]) {
				case ApoSkunkmanAIConstants.LEVEL_FREE:
					tileState = new TileState(TileState.FreeTileType, p);
					break;
				case ApoSkunkmanAIConstants.LEVEL_SKUNKMAN:
					ApoSkunkmanAILevelSkunkman skunk = level.getSkunkman(p.y, p.x);
					BombTileState bomb = new BombTileState(p, skunk.getSkunkWidth());
					bomb.setTimeToLive(skunk.getTimeToExplosion());
					tileState = bomb;
					break;
				case ApoSkunkmanAIConstants.LEVEL_BUSH:
					tileState = new TileState(TileState.BushTileType, p);
					break;
				case ApoSkunkmanAIConstants.LEVEL_GOODIE:
					tileState = new TileState(TileState.GoodieTileType, p);
					break;
				case ApoSkunkmanAIConstants.LEVEL_STONE:
					tileState = new TileState(TileState.StoneTileType, p);
					break;
				default:
					continue;
				}
				
				env.updateTileState(tileState);
			}
		}
		
		env.setMaxSkunks(player.getMaxSkunkman());
		env.setMiliTimeForTile(player.getMSForOneTile());
		env.setSkunkWidth(player.getSkunkWidth());
		env.setPlayerPosition(new Point(player.getPlayerX(), player.getPlayerY()));
		
		return env;
	}
	
	public Environment() {
		this(null);
	}
	
	public Environment(Environment parent) {
		this._parentState = parent;
		
		if (this._parentState != null) {
			this._parentState._hasChildren = true;
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
			this._hasChildren = false;
		}
	}
	
	public Environment(Environment parent, PathStep step) {
		this(parent);
		
		// Incoperate the step
		if (step instanceof PathLayBombStep) {
			// Set the tile to be a bomb
			this.updateTileState(new BombTileState(this.playerPosition(), this.skunkWidth()));
			this._currentTime += 20; // TODO: real thing
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
			
			// Can we even go there?
			if (this._playerPosition.y >= 0 && this._playerPosition.y <= FIELD_HEIGHT &&
					this._playerPosition.x >= 0 && this._playerPosition.x <= FIELD_WIDTH) {
				TileState tile = this._tiles[this._playerPosition.x][this._playerPosition.y];
				
				if (tile.tileType() == TileState.BushTileType || tile.tileType() == TileState.StoneTileType)
					throw new RuntimeException("Invalid step!");
			}
			else {
				throw new RuntimeException("Invalid step!");
			}
			
			this._currentTime += this.miliTimeForTile();
		}
		else if (step instanceof PathWaitStep) {
			PathWaitStep wait = (PathWaitStep)step;
			
			this._currentTime += wait.duration();
		}
		else {
			throw new RuntimeException("Unknown step!");
		}
		
		this._step = step;
		
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
	
	// Returns the steps of this env object
	// Does now go up like the others
	public List<PathStep> steps() {
		LinkedList<PathStep> steps = new LinkedList<PathStep>();
		
		Environment env = this;
		
		while (env != null) {
			if (env._step != null)
				steps.addFirst(env._step);
			
			env = env._parentState;
		}
		
		return steps;
	}
	
	public Point playerPosition() {
		return this._playerPosition;
	}
	
	public boolean isPlayerAlive()
	{
		return this._isPlayerAlive;
	}
	
	
	
	// Modify state
	protected void updateTileState(TileState state) {
		assert state != null;
		assert !this._hasChildren;

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

	protected void setMiliTimeForTile(int time) {
		assert time > 0;
		assert !this._hasChildren;

		this._miliTimeForTile = time;
	}

	protected void setSkunkWidth(int width) {
		assert width > 0;
		assert !this._hasChildren;

		this._skunkWidth = width;
	}

	protected void setMaxSkunks(int maxSkunks) {
		assert maxSkunks > 0;
		assert !this._hasChildren;
		
		this._maxSkunks = maxSkunks;
	}
	
	protected void setPlayerPosition(Point position) {
		assert !this._hasChildren;
		
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
				Point p = bomb.coordinate();
				
				// Now let this thing explode
				// First walk x upwards
				for (int x = p.x; x < FIELD_WIDTH && x <= p.x + bomb.width(); x++) {
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
				for (int x = p.x; x > 0 && x >= p.x - bomb.width(); x--) {
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
				for (int y = p.y; y < FIELD_HEIGHT && y <= p.y + bomb.width(); y++) {
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
				for (int y = p.y; y > 0 && y >= p.y - bomb.width(); y--) {
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
