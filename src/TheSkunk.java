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

import theskunk.ExecutionState;
import theskunk.Path;
import theskunk.PathAssertion;
import theskunk.PathFinder;
import theskunk.PathLayBombStep;
import theskunk.PathMoveStep;
import theskunk.PathMoveStep.Direction;
import theskunk.PathStep;
import theskunk.PathWaitStep;
import theskunk.environment.Environment;
import theskunk.objectives.FindGoalObjective;
import theskunk.objectives.Objective;
import theskunk.objectives.StayAliveObjective;

public class TheSkunk extends ApoSkunkmanAI {
	
	private ExecutionState state;
	
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
		
		this.state = new ExecutionState();
				
		this.state.objectives.add(new FindGoalObjective());
		this.state.objectives.add(new StayAliveObjective());
	}
	
	@Override
	public void think(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		Environment env = Environment.envFromApo(level, player);
		this.state.level = level;
		this.state.player = player;
		
		for (Objective o : this.state.objectives) {
			// Evaluate the objective
			o.evaluate(env, this.state);
			
			// If the goal is satisfied we need to
			// work down it's path.
			if (!o.isSatisfied()) {
				if (o != this.state.currentObjective) {
					this.state.reset();
					this.state.currentObjective = o;
					player.addMessage(String.format("Changed objective to %s", o.toString()));
					break;
				}
				break;
			}
			else if (o == this.state.currentObjective) {
				player.addMessage("Cleared objective...");
				this.state.currentObjective = null;
			}
		}
		
		if (this.state.currentObjective != null) {
			List<PathStep> steps = this.state.currentObjective.path().steps();
			
			if (this.state.stepIndex >= steps.size()) {
				player.addMessage("Cleared objective...");
				this.state.currentObjective = null;
				return;
			}
			
			// Check if next step would violate a objective
//			for (Objective supirior : this.state.objectives) {
//				if (supirior.equals(this.state.currentObjective)) {
//					// Went down the the current objective
//					// all are satisfied, so move on with this.
//					break;
//				}
//				
//				int oldStep = this.state.stepIndex;
//				this.state.stepIndex++;
//				EnvironmentState env2 = new EnvironmentState(env, 0);
//				player.addMessage("Player pos " + env2.playerPosition());
//				env2.setStep(this.state.currentObjective.path().steps().get(this.state.stepIndex-1));
//				player.addMessage("Step " + this.state.currentObjective.path().steps().get(this.state.stepIndex-1));
//				player.addMessage("Player pos " + env2.playerPosition());
//				player.addMessage("Bombs" + env2.bombTiles());
//				env2 = new EnvironmentState(env2, env.miliTimeForTile());
//				supirior.evaluate(env2, this.state);
//				this.state.stepIndex = oldStep;
//				
//				if (!supirior.isSatisfied()) {
//					player.addMessage(String.format("Would violate"));
//					return;
//				}
//			}

			PathStep step = steps.get(this.state.stepIndex);
			
			for (PathAssertion a : step.assertions()) {
				if (!a.evaulate(level, player)) {
					player.addMessage(a + " failed. Restart thinking...");
					// Current situation does not hold the path anymore
					this.pathFailed();
					// Start new
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
				player.addMessage("Go " + move);
				this.state.stepIndex++;
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
				
				this.state.stepIndex++;
			}
			else if (step instanceof PathWaitStep) {
				if (this.state.remainingWaitTime <= 0)
					this.state.remainingWaitTime = ((PathWaitStep)step).duration();
				
				this.state.remainingWaitTime -= ApoSkunkmanAIConstants.WAIT_TIME_THINK;
				
				if (this.state.remainingWaitTime <= 0) {
					this.state.stepIndex++;
					this.state.remainingWaitTime = 0;
				}
			}
			
			this.visualizePath(this.state.currentObjective.path(),this.state.stepIndex, player);
		}
		else {
			player.addMessage("No objective");
		}
	}
	
	private void pathFailed() {
		this.state.reset();
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
