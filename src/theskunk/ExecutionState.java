package theskunk;

import java.util.PriorityQueue;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.objectives.Objective;

public class ExecutionState {
	public int stepIndex;
	public int remainingWaitTime;
	public PriorityQueue<Objective> objectives;
	public Objective currentObjective;
	public ApoSkunkmanAILevel level;
	public ApoSkunkmanAIPlayer player;
	
	public ExecutionState() {
		this.objectives = new PriorityQueue<Objective>();
	}
	
	public void reset() {
		this.stepIndex = 0;
		this.remainingWaitTime = 0;
		if (this.currentObjective != null)
			this.currentObjective.pathFailed();
		this.currentObjective = null;
	}
}
