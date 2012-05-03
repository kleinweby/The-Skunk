package theskunk;

public class PathMoveStep extends PathStep {
	public enum Direction {
		Down,
		Up,
		Left,
		Right
	};
	
	private Direction _direction;
	
	protected PathMoveStep(Direction direction) {
		this._direction = direction;
	}
	
	public Direction direction() {
		return this._direction;
	}
}
