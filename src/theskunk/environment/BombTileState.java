package theskunk.environment;

import java.awt.Point;

public class BombTileState extends TileState {
	private int _width;
	private int _timeLayed;
	private int _timeToLive;
	private int _playerLayed;
	
	static int TimeToLive = 2700; // TODO: what's the correct time?
	
	public BombTileState(Point p, int width) {
		super(TileState.BombTileType, p);
		this._width = width;
		this._timeToLive = TimeToLive;
		this._playerLayed = -1;
	}

	public int timeLayed() {
		return this._timeLayed;
	}

	protected void setTimeLayed(int timeLayed) {
		this._timeLayed = timeLayed;
	}

	public int timeExploded() {
		return this._timeLayed + this._timeToLive;
	}
	
	public int width() {
		return this._width;
	}
	
	public void setTimeToLive(int ttl) {
		this._timeToLive = ttl;
	}
	
	protected void setPlayerLayed(int player) {
		this._playerLayed = player;
	}
	
	public int playerLayed() {
		return this._playerLayed;
	}
	
	@Override
	public String toString() {
		return "Bomb " + this._coordinate;
	}
}
