package theskunk.environment;

import java.awt.Point;

public class BombTileState extends TileState {
	private int _width;
	private int _timeLayed;
	
	static int TimeToLive = 2700; // TODO: what's the correct time?
	
	public BombTileState(Point p, int width) {
		super(TileState.BombTileType, p);
		this._width = width;
	}

	public int timeLayed() {
		return this._timeLayed;
	}

	protected void setTimeLayed(int timeLayed) {
		this._timeLayed = timeLayed;
	}

	public int timeExploded() {
		return this._timeLayed + TimeToLive;
	}
	
	public int width() {
		return this._width;
	}
}
