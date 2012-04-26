package theskunk;

public class BombTileState extends TileState {
	int timeToLive;
	int width;
	
	public BombTileState(int x, int y, int timeToLive, int width) {
		super(TileState.BombTileTYpe, x, y);
		
	}

}
