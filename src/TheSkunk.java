import java.awt.Color;
import java.awt.Point;
import java.util.List;

import theskunk.ExecutionState;
import theskunk.environment.Environment;
import theskunk.objectives.FindGoalObjective;
import theskunk.objectives.KillObjective;
import theskunk.objectives.Objective;
import theskunk.objectives.StayAliveObjective;
import theskunk.path.Path;
import theskunk.path.assertions.Assertable;
import theskunk.path.assertions.Assertion;
import theskunk.path.steps.InvalidStepException;
import theskunk.path.steps.LayBombStep;
import theskunk.path.steps.MoveStep;
import theskunk.path.steps.Step;
import theskunk.path.steps.WaitStep;
import apoSkunkman.ai.ApoSkunkmanAI;
import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class TheSkunk extends ApoSkunkmanAI {
	
	private ExecutionState state;
	private int recursiveThinkCounter;
	
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
		this.state.objectives.add(new KillObjective());
	}
	
	@Override
	public void think(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		this.recursiveThinkCounter = 0;
		this.doThinking(level, player);
	}
	
	private void doThinking(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		this.recursiveThinkCounter++;
		
		if (this.recursiveThinkCounter > 10) {
			player.addMessage("Abort thinking after 10 restarts. We don't seem to find a solution");
			return;
		}
		
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
					this.state.changeObjective(o);
					break;
				}
				break;
			}
			else if (o == this.state.currentObjective) {
				this.state.changeObjective(o);
			}
		}
		
		if (this.state.currentObjective != null) {
			List<Step> steps = this.state.currentObjective.path().steps();
			
			if (this.state.stepIndex >= steps.size()) {
				this.state.changeObjective(null);
				return;
			}

			Step step = steps.get(this.state.stepIndex);
			
			for (Assertion a : step.assertions()) {
				if (!a.evaulate(level, player)) {
					player.addMessage(a + " failed. Restart thinking...");
					// Current situation does not hold the path anymore
					this.pathFailed();
					// Start new
					this.doThinking(level, player);
					return;
				}
			}
			
			// Check if next step would violate a objective
			{
				boolean wouldViolate = false;
				try {
					int oldStep = this.state.stepIndex;
					Environment env2 = new Environment(env, step);
					this.state.stepIndex++;

					for (Objective supirior : this.state.objectives) {
						if (supirior.equals(this.state.currentObjective)) {
							// Went down the the current objective
							// all are satisfied, so move on with this.
							break;
						}

						supirior.evaluate(env2, this.state);

						if (!supirior.isSatisfied()) {
							wouldViolate = true;
							player.addMessage(String.format("Would violate" + supirior));
							break;
						}
					}

					this.state.stepIndex = oldStep;
				}
				catch (InvalidStepException e)
				{
					player.addMessage(String.format("Would violate" + e));
					wouldViolate = true;
				}
				
				if (wouldViolate) {
					player.addMessage(String.format("Would violate"));
					// Current situation does not hold the path anymore
					this.pathFailed();
					
					return;
				}
			}
			
			if (step instanceof MoveStep) {
				MoveStep move = (MoveStep)step;
				
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
			else if (step instanceof LayBombStep) {
				if (!player.canPlayerLayDownSkunkman()) {
					// Current situation does not hold the path anymore
					this.pathFailed();
					// Start new
					player.addMessage("Planned laying down skunk. Not able to. Restart thinking...");
					this.doThinking(level, player);
					return;
				}
				player.laySkunkman();
				
				this.state.stepIndex++;
			}
			else if (step instanceof WaitStep) {
				if (this.state.remainingWaitTime <= 0)
					this.state.remainingWaitTime = ((WaitStep)step).duration();
				
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
		
		for (Assertable step : path.steps()) {
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
			
			if (step instanceof MoveStep) {
				MoveStep move = (MoveStep)step;
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
			else if (step instanceof LayBombStep) {
				player.drawCircle(lastPoint.x + 0.5f, lastPoint.y + 0.5f, 0.15f, true, 300, color);
			}
			else if (step instanceof WaitStep) {
				player.drawRect(lastPoint.x + 0.5f - 0.15f, lastPoint.y + 0.5f - 0.15f, 0.3f, 0.3f, false, 300, color);
			}
			
			x++;
		}
	}
}
