package theskunk.environment;

public class BombTileState extends TileState {
	private int _width;
	private int _timeLayed;
	
	static int TimeToLive = 300; // TODO: what's the correct time?
	
	public BombTileState(int x, int y, int width) {
		super(TileState.BombTileTYpe, x, y);
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
