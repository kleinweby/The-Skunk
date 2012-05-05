package theskunk;

import java.awt.Point;
import java.util.List;

import theskunk.environment.EnvironmentState;

public class Path {
	private List<PathStep> _steps;
	private List<PathAssertion> _assertions;
	private EnvironmentState _finalState;
	private Point _finalPlayerPosition;
	private Point _startPlayerPosition;
	
	protected Path(List<PathStep> steps, List<PathAssertion> assertions, EnvironmentState finalState, Point startPlayerPosition, Point finalPlayerPosition) {
		this._steps = steps;
		this._finalState = finalState;
		this._finalPlayerPosition = finalPlayerPosition;
		this._startPlayerPosition = startPlayerPosition;
		this._assertions = assertions;
	}
	
	public List<PathStep> steps() {
		return this._steps;
	}
	
	public EnvironmentState finalState() {
		return this._finalState;
	}
	
	public Point finalPlayerPosition() {
		return this._finalPlayerPosition;
	}
	
	public Point startPlayerPosition() {
		return this._startPlayerPosition;
	}
	
	public List<PathAssertion> assertions() {
		return this._assertions;
	}
}
