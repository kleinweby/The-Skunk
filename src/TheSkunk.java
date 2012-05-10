import java.awt.Color;
import java.awt.Point;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import apoSkunkman.ai.ApoSkunkmanAI;
import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAIEnemy;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAILevelSkunkman;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.Path;
import theskunk.PathAssertion;
import theskunk.PathFinder;
import theskunk.PathLayBombStep;
import theskunk.PathMoveStep;
import theskunk.PathMoveStep.Direction;
import theskunk.PathStep;
import theskunk.PathWaitStep;
import theskunk.objectives.FindGoalObjective;
import theskunk.objectives.Objective;

public class TheSkunk extends ApoSkunkmanAI {
	
	private int stepIndex;
	private int remainingWaitTime;
	private PriorityQueue<Objective> _objectives;
	private Objective _currentObjective;
	
	@Override
	public String getPlayerName() {
		return "The Skunk";
	}

	@Override
	public String getAuthor() {
		return "Christian Speich";
	}

	@Override
	public void load(String path) {
		super.load(path);
		
		this.resetState();
		
		this._objectives = new PriorityQueue<Objective>();
		
		this._objectives.add(new FindGoalObjective());
	}
	
	@Override
	public void think(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		for (Objective o : this._objectives) {
			// Evaluate the objective
			o.evaluate(level, player);
			
			// If the goal is satisfied we need to
			// work down it's path.
			if (!o.isSatisfied()) {
				if (o != this._currentObjective) {
					this.resetState();
					this._currentObjective = o;
					break;
				}
			}
		}
		
		{
			List<PathStep> steps = this._currentObjective.path().steps();
			
			if (this.stepIndex >= steps.size()) {
				this._currentObjective = null;
				return;
			}
			
			PathStep step = steps.get(this.stepIndex);
			
			for (PathAssertion a : step.assertions()) {
				if (!a.evaulate(level, player)) {
					// Current situation does not hold the path anymore
					this.pathFailed();
					// Start new
					player.addMessage(a + " failed. Restart thinking...");
					this.think(level, player);
					return;
				}
			}
			
			if (step instanceof PathMoveStep) {
				PathMoveStep move = (PathMoveStep)step;
				
				switch (move.direction()) {
				case Down:
					player.movePlayerDown();
					break;
				case Up:
					player.movePlayerUp();
					break;
				case Right:
					player.movePlayerRight();
					break;
				case Left:
					player.movePlayerLeft();
					break;
				}
				
				this.stepIndex++;
			}
			else if (step instanceof PathLayBombStep) {
				if (!player.canPlayerLayDownSkunkman()) {
					// Current situation does not hold the path anymore
					this.pathFailed();
					// Start new
					player.addMessage("Planned laying down skunk. Not able to. Restart thinking...");
					this.think(level, player);
				}
				
				player.laySkunkman();
				
				this.stepIndex++;
			}
			else if (step instanceof PathWaitStep) {
				if (this.remainingWaitTime <= 0)
					this.remainingWaitTime = ((PathWaitStep)step).duration();
				
				this.remainingWaitTime -= ApoSkunkmanAIConstants.WAIT_TIME_THINK;
				
				if (this.remainingWaitTime <= 0) {
					this.stepIndex++;
					this.remainingWaitTime = 0;
				}
			}
		}
		
		this.visualizePath(this._currentObjective.path(),this.stepIndex, player);
	}
	
	private void resetState() {
		this._currentObjective = null;
		this.stepIndex = 0;
		this.remainingWaitTime = 0;
	}
	
	private void pathFailed() {
		this._currentObjective.pathFailed();
		this.resetState();
	}
	
	private void visualizePath(Path path, int currentStep, ApoSkunkmanAIPlayer player)
	{
		Point lastPoint = path.startPlayerPosition();
		int x = 0;
		
		for (PathStep step : path.steps()) {
			Color color;
			
			if (x < currentStep) {
				color = new Color(0, 255, 0);
			}
			else if (x == currentStep) {
				color = new Color(255, 255, 0);
			}
			else {
				color = new Color(255,0,0);
			}
			
			if (step instanceof PathMoveStep) {
				PathMoveStep move = (PathMoveStep)step;
				Point nextPoint = null;
				
				switch (move.direction()) {
				case Down:
					nextPoint = new Point(lastPoint.x, lastPoint.y + 1);
					break;
				case Up:
					nextPoint = new Point(lastPoint.x, lastPoint.y - 1);
					break;
				case Right:
					nextPoint = new Point(lastPoint.x + 1, lastPoint.y);
					break;
				case Left:
					nextPoint = new Point(lastPoint.x - 1, lastPoint.y);
					break;
				}
				
				player.drawLine(lastPoint.x + 0.5f, lastPoint.y + 0.5f, nextPoint.x + 0.5f, nextPoint.y + 0.5f, 300, color);
				lastPoint = nextPoint;
			}
			else if (step instanceof PathLayBombStep) {
				player.drawCircle(lastPoint.x + 0.5f, lastPoint.y + 0.5f, 0.15f, true, 300, color);
			}
			else if (step instanceof PathWaitStep) {
				player.drawRect(lastPoint.x + 0.5f - 0.15f, lastPoint.y + 0.5f - 0.15f, 0.3f, 0.3f, false, 300, color);
			}
			
			x++;
		}
	}
}
