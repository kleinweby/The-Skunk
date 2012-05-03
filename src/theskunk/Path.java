package theskunk;

import java.awt.Point;
import java.util.List;

public class Path {
	private List<PathStep> _steps;
	
	protected Path(List<PathStep> steps) {
		this._steps = steps;
	}
	
	public List<PathStep> steps() {
		return this._steps;
	}
}
