import java.awt.Point;

import apoSkunkman.ai.ApoSkunkmanAI;
import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.Path;
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
		Path path = p.solution();
		
		Point lastPoint = null;
		for (Point point : path.points()) {
			if (lastPoint != null)
				player.drawLine(lastPoint.x + 0.5f, lastPoint.y + 0.5f, point.x + 0.5f, point.y + 0.5f, 1000);
			
			lastPoint = point;
		}		
	}

}
