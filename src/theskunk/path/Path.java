package theskunk.path;

import java.awt.Point;
import java.util.List;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.environment.Environment;
import theskunk.path.assertions.Assertion;
import theskunk.path.steps.Step;

public class Path {
	private List<Step> _steps;
	private List<Assertion> _assertions;
	private Environment _finalState;
	private Point _finalPlayerPosition;
	private Point _startPlayerPosition;
	
	public Path(List<Step> steps, List<Assertion> assertions, Environment finalState, Point startPlayerPosition, Point finalPlayerPosition) {
		this._steps = steps;
		this._finalState = finalState;
		this._finalPlayerPosition = finalPlayerPosition;
		this._startPlayerPosition = startPlayerPosition;
		this._assertions = assertions;
	}
	
	public List<Step> steps() {
		return this._steps;
	}
	
	public Environment finalState() {
		return this._finalState;
	}
	
	public Point finalPlayerPosition() {
		return this._finalPlayerPosition;
	}
	
	public Point startPlayerPosition() {
		return this._startPlayerPosition;
	}
	
	public List<Assertion> assertions() {
		return this._assertions;
	}

	public boolean assertAgainstApo(ApoSkunkmanAILevel level,
			ApoSkunkmanAIPlayer player) {
		for (Assertion a : this._assertions) {
			if (!a.evaulate(level, player)) {
				player.addMessage(a + "failed.");
				return false;
			}
		}
		return true;
	}
}
