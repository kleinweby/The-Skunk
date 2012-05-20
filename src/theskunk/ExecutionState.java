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
	
	public void changeObjective(Objective o)
	{
		if (this.currentObjective != null) {
			this.currentObjective.resigns();
			System.out.println("Objective " + currentObjective + "resigns.");
		}
		if (o != null) {
			o.becomesActive();
			System.out.println("Objective " + o + "becomes active.");
		}
		this.currentObjective = o;
	}
	
	public void reset() {
		this.stepIndex = 0;
		this.remainingWaitTime = 0;
		this.changeObjective(null);
	}
}
