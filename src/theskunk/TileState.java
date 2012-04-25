package theskunk;

class TileState {
	static int FreeTileType = (1 << 0);
	static int BushTileType = (1 << 1);
	static int BombTileTYpe = (1 << 2);
	static int GoodieTileType = (1 << 3);
	static int StoneTileType = (1 << 4);
	
	int _tileType;
	
	public TileState(int type) {
		this._tileType= type;
	}
	
	public int tileType() {
		return this._tileType;
	}
}

