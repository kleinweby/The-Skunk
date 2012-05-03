import java.awt.Point;

import apoSkunkman.ai.ApoSkunkmanAI;
import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

import theskunk.Path;
import theskunk.PathFinder;
import theskunk.PathMoveStep;
import theskunk.PathMoveStep.Direction;
import theskunk.PathStep;

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
		PathFinder p = new PathFinder(PathFinder.environmentFromApo(level, player), PathFinder.Type.FindGoal, 
				player.getPlayerX(), player.getPlayerY(),
				level.getGoalXPoint().x, level.getGoalXPoint().y);

		Path path = p.solution();

		Point lastPoint = new Point(player.getPlayerX(), player.getPlayerY());
		for (PathStep step : path.steps()) {
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
				
				player.drawLine(lastPoint.x + 0.5f, lastPoint.y + 0.5f, nextPoint.x + 0.5f, nextPoint.y + 0.5f, 300);
				lastPoint = nextPoint;
			}
		}
		
		{
			PathMoveStep move = (PathMoveStep) path.steps().get(0);
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
		}
	}
}
