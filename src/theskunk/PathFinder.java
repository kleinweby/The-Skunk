package theskunk;
import java.awt.Point;
import java.sql.Time;
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
	
	private int _stepCount;
	private int _stepCountSubroutines;
	private long _timeConsumed;
	private long _timeConsumedInSubFinders;
	
	public static EnvironmentState environmentFromApo(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		EnvironmentState startState = new EnvironmentState(null, level.getStartTime() - level.getTime());
		
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
		startState.setPlayerPosition(new Point(player.getPlayerX(), player.getPlayerY()));
		
		return startState;
	}
	
	public PathFinder(EnvironmentState env, Type type, int objX, int objY) {		
		this._objX = objX;
		this._objY = objY;
		this._type = type;
		this._startPoint = env.playerPosition();
		
		this._layBombs = true;
		
		if (this._type == Type.AvoidBomb)
			this._layBombs = false;
		
		this.setStartNode(new Node(env, null, this._startPoint.x, this._startPoint.y, 0, 
				this.estimatedCost(env, this._startPoint.x, this._startPoint.y)));
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
		// TODO: The timing here is somwhat wrong
		// we're advancing the environment for the time the steps takes
		// and do then the step. Should be the other way around, but
		// quick try made the timing even worse xD
		
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
		
		if (sourceNode.prevNode() != null && 
				sourceNode.prevNode().x() == destX &&
				sourceNode.prevNode().y() == destY) {
			// Already been there
			return null;
		}
		
		EnvironmentState srcEnv = sourceNode.nodeState();
		TileState currentState = srcEnv.tileStateAt(destX, destY);
		
		// Perfect =) we can simply go there
		if (currentState.tileType() == TileState.FreeTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			env.setStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, destX, destY));
		}
		else if (currentState.tileType() == TileState.GoodieTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			// Goodies will not be handled separatly in the sense of cost, because
			// it is the path finders role to do so (A bad goodie may be good
			// for the solution)
			
			// TODO: change env to reflect goodie
			
			env.setStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, destX, destY, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.x(), sourceNode.y()));
		}
		// Do not blow up bushes when we're currently running away from an bomb
		else if (currentState.tileType() == TileState.BushTileType && this._layBombs) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			{
				PathLayBombStep step = new PathLayBombStep();
				// We dont need to blow something up that is not there
				step.addAssertion(new PathBushAssertion(new Point(destX, destY), true));
				// We have to make sure we are where we're thinking we are ;)
				step.addAssertion(new PathPlayerPositionAssertion(env.playerPosition()));
				// We have to make sure our escape is right (timing/distance)
				step.addAssertion(new PathSkunkWidthAssertion(env.skunkWidth()));
				step.addAssertion(new PathSpeedAssertion(env.miliTimeForTile()));
				// Will insert a bomb tile
				env.setStep(step);
			}
			
			// Find escape route
			{
				// Advance the time because we did a step
				// TODO: we is this not needed and actually
				// makes the wait time to short?
				//env = new EnvironmentState(env, srcEnv.miliTimeForTile());
				
				PathFinder finder = new PathFinder(env, Type.AvoidBomb, sourceNode.x(), sourceNode.y());
				
				// Solve the escape.
				Path path = finder.solution();
				this._stepCountSubroutines += finder._stepCount;
				this._timeConsumedInSubFinders += finder._timeConsumed;
				
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
					// TODO: because of the wrong timing mentioned above
					// the last env of an path already contains an step
					// so we need an env shadow copy here, with
					// no time advance
					env = new EnvironmentState(env, 0);
					env.setStep(new PathWaitStep(remainingBombTime));
					// New env here to work with
					env = new EnvironmentState(env, remainingBombTime);
				}
				
				// There should not be a bomb now
				assert !(env.tileStateAt(sourceNode.x(), sourceNode.y()) instanceof BombTileState);
				
				// Ok bomb is now exploded, get back to final destination
				finder = new PathFinder(env, Type.FindGoal, destX, destY);
				// There is an free path, which is guranteed
				// to be least expensive. So save the computing
				// time and don't simulate bombs
				finder._layBombs = false;
				
				path = finder.solution();
				this._stepCountSubroutines += finder._stepCount;
				this._timeConsumedInSubFinders += finder._timeConsumed;

				env = path.finalState();
				assert path.finalPlayerPosition().x == destX &&
						path.finalPlayerPosition().y == destY;
			}
			
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
		this._stepCount = 0;
		long startTime = System.currentTimeMillis();

		while (this.doStep()) this._stepCount++;
		
		this._timeConsumed = System.currentTimeMillis() - startTime;
		
		List<Node> nodePath = this.nodePath();
		
		Node lastNode = nodePath().get(nodePath.size() - 1);

		return new Path(lastNode.nodeState().steps(), null, lastNode.nodeState, this._startPoint, new Point(lastNode.x(), lastNode.y()));
	}
	
	public int usedSteps() {
		return this._stepCount + this._stepCountSubroutines;
	}
	
	public int usedStepsInSubroutines() {
		return this._stepCountSubroutines;
	}
	
	public Time usedTime() {
		return new Time(this._timeConsumed);
	}
	
	public Time usedTimeInSubroutines() {
		return new Time(this._timeConsumedInSubFinders);
	}
}
