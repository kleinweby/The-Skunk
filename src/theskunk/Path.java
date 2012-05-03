package theskunk;

import java.awt.Point;
import java.util.List;

public class Path {
	private List<Point> _points;
	
	protected Path(List<Point> points) {
		this._points = points;
	}
	
	public List<Point> points() {
		return this._points;
	}
}
