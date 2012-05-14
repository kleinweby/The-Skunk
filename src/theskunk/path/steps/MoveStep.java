package theskunk.path.steps;


public class MoveStep extends Step {
	public enum Direction {
		Down,
		Up,
		Left,
		Right
	};
	
	private Direction _direction;
	
	public MoveStep(Direction direction) {
		this._direction = direction;
	}
	
	public Direction direction() {
		return this._direction;
	}
	
	@Override
	public String toString() {
		return String.format("Move(direction=%s)", this._direction);
	}
}
