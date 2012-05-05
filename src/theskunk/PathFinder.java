package theskunk;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import theskunk.PathMoveStep.Direction;
import theskunk.environment.BombTileState;
import theskunk.environment.EnvironmentState;
import theskunk.environment.TileState;

import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAILevelGoodie;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathFinder extends GenericAStar<EnvironmentState> {
	public enum Type {
		FindGoal,
		AvoidBomb
	}
	
	private int _objX;
	private int _objY;
	private Type _type;
	private Point _startPoint;
	private boolean _layBombs;
	
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
					
					ApoSkunkmanAILevelGoodie goodie = level.getGoodie(y, x);
					if (goodie != null) {
						System.out.println("goodie under bush!");
					}
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
	
	public PathFinder(EnvironmentState env, Type type, Point playerPosition, int objX, int objY) {		
		this._objX = objX;
		this._objY = objY;
		this._type = type;
		this._startPoint = playerPosition;
		
		this._layBombs = true;
		
		if (this._type == Type.AvoidBomb)
			this._layBombs = false;
		
		this.setStartNode(new Node(env, null, playerPosition.x, playerPosition.y, 0, 
				this.estimatedCost(env, playerPosition.x, playerPosition.y)));
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
			
			// TODO: Goodies hiere ber�cksichtigen. Kosten werden nicht angepasst, da
			// evtl. ein schlechtes goodie trotzdem ein guten weg produzieren wuerde
			
			env.addStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.x(), sourceNode.y()));
		}
		// Do not blow up bushes when we're currently running away from an bomb
		else if (currentState.tileType() == TileState.BushTileType && this._layBombs) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			// We need to destroy the bush, so add the laybomb step and lay it in our env
			env.updateTileState(new BombTileState(sourceNode.x(), sourceNode.y(), env.skunkWidth()));
			{
				PathLayBombStep step = new PathLayBombStep();
				// We dont need to blow something up that is not there
				step.addAssertion(new PathBushAssertion(new Point(destX, destY), true));
				// We have to make sure our escape is right (timing/distance)
				step.addAssertion(new PathSkunkWidthAssertion(env.skunkWidth()));
				step.addAssertion(new PathSpeedAssertion(env.miliTimeForTile()));
				env.addStep(step);
			}
			
			// Find escape route
			{
				// Advance the time because we did a step
				// TODO: we is this not needed and actually
				// makes the wait time to short?
				//env = new EnvironmentState(env, srcEnv.miliTimeForTile());
				
				PathFinder finder = new PathFinder(env, Type.AvoidBomb, new Point(sourceNode.x(), 
						sourceNode.y()), sourceNode.x(), sourceNode.y());
				
				// Solve the escape.
				Path path = finder.solution();
				
				// the new env is this
				env = path.finalState();
				
				// Now we have to wait 'till the bomb is exploded
				int remainingBombTime = 0;
				TileState tile = env.tileStateAt(sourceNode.x(), sourceNode.y());
				
				if (tile instanceof BombTileState) {
					BombTileState bomb = (BombTileState)tile;
					remainingBombTime = bomb.timeExploded() - env.currentTime();
				}
				
				if (remainingBombTime > 0) {
					env.addStep(new PathWaitStep(remainingBombTime));
					// New env here to work with
					env = new EnvironmentState(env, remainingBombTime);
				}
				
				// There should not be a bomb now
				assert !(env.tileStateAt(sourceNode.x(), sourceNode.y()) instanceof BombTileState);
				
				// Ok bomb is now exploded, get back to that position
				finder = new PathFinder(env, Type.FindGoal, path.finalPlayerPosition(), sourceNode.x(), sourceNode.y());
				// There is an empty path, where no bombs
				// are needed, only look for those
				finder._layBombs = false;
				
				path = finder.solution();
				
				env = path.finalState();
				assert path.finalPlayerPosition().x == sourceNode.x() &&
						path.finalPlayerPosition().y == sourceNode.y();
			}
			
			// Finally step onto the tile
			env.addStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, env.currentTime() - srcEnv.currentTime() + env.miliTimeForTile(), 
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
		if (this._type == Type.FindGoal)
			return (Math.abs(this._objX-srcX) + Math.abs(this._objY-srcY)) * env.miliTimeForTile();
		else if (this._type == Type.AvoidBomb) {
			TileState tile = env.tileStateAt(this._objX, this._objY);
			
			if (tile instanceof BombTileState) {
				BombTileState bomb = (BombTileState)tile;
				if (srcY == this._objY) {
					if (Math.abs(srcX - this._objX) > bomb.width()) {
						// we're safe
						return 0;
					}
					else {
						return (Math.abs(srcX - this._objX) + 1) * env.miliTimeForTile();
					}
				}
				else if (srcX == this._objX) {
					if (Math.abs(srcY - this._objY) > bomb.width()) {
						// we're safe
						return 0;
					}
					else {
						return (Math.abs(srcY - this._objY) + 1) * env.miliTimeForTile();
					}
				}
				else
					return 0;
			}
			else {
				// No bomb at the coord we should avoid
				return 0;
			}
		}
		throw new RuntimeException("Unknown type");
	}
	
	public Path solution() {
		// Do the a-star thing
		while (this.doStep());
		
		List<Node> nodePath = this.nodePath();
		
		Node lastNode = nodePath().get(nodePath.size() - 1);

		return new Path(lastNode.nodeState().steps(), null, lastNode.nodeState, this._startPoint, new Point(lastNode.x(), lastNode.y()));
	}
}
