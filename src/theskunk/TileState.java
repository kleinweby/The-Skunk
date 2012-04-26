package theskunk;

class TileState {
	static int FreeTileType = (1 << 0);
	static int BushTileType = (1 << 1);
	static int BombTileTYpe = (1 << 2);
	static int GoodieTileType = (1 << 3);
	static int StoneTileType = (1 << 4);
	
	int _tileType;
	int _x;
	int _y;
	
	public TileState(int type, int x, int y) {
		this._tileType= type;
	}
	
	public int tileType() {
		return this._tileType;
	}
	
	public int x() {
		return this._x;
	}
	
	public int y() {
		return this._y;
	}
}

