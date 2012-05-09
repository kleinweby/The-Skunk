package theskunk.environment;

import java.awt.Point;

public class TileState {
	public static int FreeTileType = (1 << 0);
	public static int BushTileType = (1 << 1);
	public static int BombTileType = (1 << 2);
	public static int GoodieTileType = (1 << 3);
	public static int StoneTileType = (1 << 4);
	
	int _tileType;
	Point _coordinate;
	
	public TileState(int type, Point p) {
		assert p.x <= 0xFF;
		assert p.y <= 0xFF;
		
		this._tileType= type;
		this._coordinate = p;
	}
	
	public int tileType() {
		return this._tileType;
	}
	
	public Point coordinate() {
		return this._coordinate;
	}
}

