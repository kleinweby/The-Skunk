import apoSkunkman.ai.ApoSkunkmanAI;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.PathFinder;

public class TheSkunk extends ApoSkunkmanAI {

	@Override
	public String getPlayerName() {
		return "The Skunk";
	}

	@Override
	public String getAuthor() {
		return "Christian Speich";
	}

	@Override
	public void think(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		// TODO Auto-generated method stub
		PathFinder p = new PathFinder(level, player, PathFinder.Type.FindGoal, level.getGoalXPoint().x, level.getGoalXPoint().y);
	}

}
