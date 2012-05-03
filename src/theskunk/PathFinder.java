package theskunk;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import theskunk.PathMoveStep.Direction;
import theskunk.environment.EnvironmentState;
import theskunk.environment.TileState;

import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathFinder extends GenericAStar<EnvironmentState> {
	public enum Type {
		FindGoal,
		AvoidBomb
	}
	
	private int _objX;
	private int _objY;
	
	public static EnvironmentState environmentFromApo(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		EnvironmentState startState = new EnvironmentState(null, 0);
		
		// Populate this state
		byte byteLevel[][] = level.getLevelAsByte();
		
		for (int x = 0; x < byteLevel.length; x++) {
			for (int y = 0; y < byteLevel[x].length; y++) {
				TileState tileState = null;
				
				switch (byteLevel[y][x]) {
				case ApoSkunkmanAIConstants.LEVEL_FREE:
				case ApoSkunkmanAIConstants.LEVEL_SKUNKMAN:
					tileState = new TileState(TileState.FreeTileType, x, y);
					break;
				case ApoSkunkmanAIConstants.LEVEL_BUSH:
					tileState = new TileState(TileState.BushTileType, x, y);
					break;
				case ApoSkunkmanAIConstants.LEVEL_GOODIE:
					tileState = new TileState(TileState.GoodieTileType, x, y);
					break;
				case ApoSkunkmanAIConstants.LEVEL_STONE:
					tileState = new TileState(TileState.StoneTileType, x, y);
					break;
				default:
					continue;
				}
				
				startState.updateTileState(tileState);
			}
		}
		
		startState.setMaxSkunks(player.getMaxSkunkman());
		startState.setMiliTimeForTile(player.getMSForOneTile());
		startState.setSkunkWidth(player.getSkunkWidth());
		
		return startState;
	}
	
	public PathFinder(EnvironmentState env, Type type, int curX, int curY, int objX, int objY) {
		this.setStartNode(new Node(env, null, curX, curY, 0, Integer.MAX_VALUE));
		
		this._objX = objX;
		this._objY = objY;
	}
	
	@Override
	protected Set<Node> adjacentNodes(Node sourceNode) {
		Set<Node> nodes = new HashSet<Node>();
		
		if (sourceNode.x() >= 1) {
			Node n = nodeFromTo(sourceNode, Direction.Left);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (sourceNode.x() + 1 < EnvironmentState.FIELD_WIDTH) {
			Node n = nodeFromTo(sourceNode, Direction.Right);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (sourceNode.y() >= 1) {
			Node n = nodeFromTo(sourceNode, Direction.Up);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (sourceNode.y() + 1 < EnvironmentState.FIELD_HEIGHT) {
			Node n = nodeFromTo(sourceNode, Direction.Down);
			
			if (n != null)
				nodes.add(n);
		}
		
		return nodes;
	}
	
	protected Node nodeFromTo(Node sourceNode, Direction direction) {
		assert sourceNode != null;
		int destX = sourceNode.x();
		int destY = sourceNode.y();
		
		switch (direction) {
		case Down:
			destY++;
			break;
		case Up:
			destY--;
			break;
		case Left:
			destX--;
			break;
		case Right:
			destX++;
			break;
		}
		
		EnvironmentState srcEnv = sourceNode.nodeState();
		TileState currentState = srcEnv.tileStateAt(destX, destY);
		
		// Perfect =) we can simply go there
		if (currentState.tileType() == TileState.FreeTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			env.addStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, destX, destY));
		}
		else if (currentState.tileType() == TileState.GoodieTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			// TODO: Goodies hiere berŸcksichtigen. Kosten werden nicht angepasst, da
			// evtl. ein schlechtes goodie trotzdem ein guten weg produzieren wuerde
			
			env.addStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.x(), sourceNode.y()));
		}
		else if (currentState.tileType() == TileState.BushTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			env.addStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile() * 3, 
					this.estimatedCost(env, sourceNode.x(), sourceNode.y()));
		}
		else if (currentState.tileType() == TileState.BombTileType) {
			// Well better not go here
			return null;
		}
		else if (currentState.tileType() == TileState.StoneTileType) {
			// We can not go here
			return null;
		}
		
		return null;
	}
	
	protected int estimatedCost(EnvironmentState env, int srcX, int srcY) {
		return (Math.abs(this._objX-srcX) + Math.abs(this._objY-srcY)) * env.miliTimeForTile();
	}
	
	public Path solution() {
		List<PathStep> steps = new ArrayList<PathStep>();

		// Do the a-star thing
		while (this.doStep());
		
		for (Node node : this.nodePath()) {
			steps.addAll(node.nodeState().steps());
		}

		return new Path(steps);
	}
}
