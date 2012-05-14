package theskunk.path.steps;


public class WaitStep extends Step {
	private int _duration;
	
	public WaitStep(int duration) {
		this._duration = duration;
	}
	
	public int duration() {
		return this._duration;
	}
	
	@Override
	public String toString() {
		return String.format("Wait(duration=%d)", this._duration);
	}
}
