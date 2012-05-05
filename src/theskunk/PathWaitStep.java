package theskunk;

public class PathWaitStep extends PathStep {
	private int _duration;
	
	public PathWaitStep(int duration) {
		this._duration = duration;
	}
	
	public int duration() {
		return this._duration;
	}
}
