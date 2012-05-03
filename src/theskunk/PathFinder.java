package theskunk;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	public PathFinder(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player, Type type, int objX, int objY) {
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
		
		this.setStartNode(new Node(startState, null, player.getPlayerX(), player.getPlayerY(), 0, Integer.MAX_VALUE));
	}
	
	@Override
	protected Set<Node> adjacentNodes(Node sourceNode) {
		Set<Node> nodes = new HashSet<Node>();
		
		if (sourceNode.x() >= 1) {
			Node n = nodeFromTo(sourceNode, sourceNode.x() - 1, sourceNode.y());
			
			if (n != null)
				nodes.add(n);
		}
		
		if (sourceNode.x() + 1 < EnvironmentState.FIELD_WIDTH) {
			Node n = nodeFromTo(sourceNode, sourceNode.x() + 1, sourceNode.y());
			
			if (n != null)
				nodes.add(n);
		}
		
		if (sourceNode.y() >= 1) {
			Node n = nodeFromTo(sourceNode, sourceNode.x(), sourceNode.y() - 1);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (sourceNode.y() + 1 < EnvironmentState.FIELD_HEIGHT) {
			Node n = nodeFromTo(sourceNode, sourceNode.x(), sourceNode.y() + 1);
			
			if (n != null)
				nodes.add(n);
		}
		
		return nodes;
	}
	
	protected Node nodeFromTo(Node sourceNode, int destX, int destY) {
		assert sourceNode != null;
		assert destX >= 0 && destX < EnvironmentState.FIELD_WIDTH;
		assert destY >= 0 && destY < EnvironmentState.FIELD_HEIGHT;
		
		EnvironmentState srcEnv = sourceNode.nodeState();
		TileState currentState = srcEnv.tileStateAt(destX, destY);
		
		// Perfect =) we can simply go there
		if (currentState.tileType() == TileState.FreeTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.x(), sourceNode.y(), destX, destY));
		}
		
		return null;
	}
	
	protected int estimatedCost(EnvironmentState env, int srcX, int srcY, int destX, int destY) {
		return (Math.abs(destY-srcY) + Math.abs(destX-srcX)) * env.miliTimeForTile();
	}
	
	public Path solution() {
		List<Point> points = new ArrayList<Point>();
		
		// Do the a-star thing
		while (this.doStep());
		
		for (Node node : this.nodePath()) {
			points.add(new Point(node.x(), node.y()));
		}
		
		return new Path(points);
	}
}
