package theskunk;
import java.awt.Point;
import java.sql.Time;
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
				Point p = new Point(x,y);
				
				switch (byteLevel[y][x]) {
				case ApoSkunkmanAIConstants.LEVEL_FREE:
				case ApoSkunkmanAIConstants.LEVEL_SKUNKMAN:
					tileState = new TileState(TileState.FreeTileType, p);
					break;
				case ApoSkunkmanAIConstants.LEVEL_BUSH:
					tileState = new TileState(TileState.BushTileType, p);
					break;
				case ApoSkunkmanAIConstants.LEVEL_GOODIE:
					tileState = new TileState(TileState.GoodieTileType, p);
					break;
				case ApoSkunkmanAIConstants.LEVEL_STONE:
					tileState = new TileState(TileState.StoneTileType, p);
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
		
		this.setStartNode(new Node(env, null, this._startPoint, 0, 
				this.estimatedCost(env, this._startPoint)));
	}
	
	@Override
	protected Set<Node> adjacentNodes(Node sourceNode) {
		Set<Node> nodes = new HashSet<Node>();
		Point p = sourceNode.coordinate();
		
		if (p.x >= 1) {
			Node n = nodeFromTo(sourceNode, Direction.Left);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (p.x + 1 < EnvironmentState.FIELD_WIDTH) {
			Node n = nodeFromTo(sourceNode, Direction.Right);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (p.y >= 1) {
			Node n = nodeFromTo(sourceNode, Direction.Up);
			
			if (n != null)
				nodes.add(n);
		}
		
		if (p.y + 1 < EnvironmentState.FIELD_HEIGHT) {
			Node n = nodeFromTo(sourceNode, Direction.Down);
			
			if (n != null)
				nodes.add(n);
		}
		
		return nodes;
	}
	
	protected Node nodeFromTo(Node sourceNode, Direction direction) {
		assert sourceNode != null;
		Point dest = new Point(sourceNode.coordinate());
		// TODO: The timing here is somwhat wrong
		// we're advancing the environment for the time the steps takes
		// and do then the step. Should be the other way around, but
		// quick try made the timing even worse xD
		
		switch (direction) {
		case Down:
			dest.y++;
			break;
		case Up:
			dest.y--;
			break;
		case Left:
			dest.x--;
			break;
		case Right:
			dest.x++;
			break;
		}
		
		if (sourceNode.prevNode() != null && 
				sourceNode.coordinate().equals(dest)) {
			// Already been there
			return null;
		}
		
		EnvironmentState srcEnv = sourceNode.nodeState();
		TileState currentState = srcEnv.tileStateAt(dest.x, dest.y);
		
		// Perfect =) we can simply go there
		if (currentState.tileType() == TileState.FreeTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			env.setStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, dest, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, dest));
		}
		else if (currentState.tileType() == TileState.GoodieTileType) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			// Goodies will not be handled separatly in the sense of cost, because
			// it is the path finders role to do so (A bad goodie may be good
			// for the solution)
			
			// TODO: change env to reflect goodie
			
			env.setStep(new PathMoveStep(direction));
			
			return new Node(env, sourceNode, dest, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.coordinate()));
		}
		// Do not blow up bushes when we're currently running away from an bomb
		else if (currentState.tileType() == TileState.BushTileType && this._layBombs) {
			EnvironmentState env = new EnvironmentState(srcEnv, srcEnv.miliTimeForTile());
			
			{
				PathLayBombStep step = new PathLayBombStep();
				// We dont need to blow something up that is not there
				step.addAssertion(new PathBushAssertion(dest, true));
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
				
				PathFinder finder = new PathFinder(env, Type.AvoidBomb, sourceNode.coordinate().x, sourceNode.coordinate().y);
				
				// Solve the escape.
				Path path = finder.solution();
				this._stepCountSubroutines += finder._stepCount;
				this._timeConsumedInSubFinders += finder._timeConsumed;
				
				// the new env is this
				env = path.finalState();
				
				// Now we have to wait 'till the bomb is exploded
				int remainingBombTime = 0;
				TileState tile = env.tileStateAt(sourceNode.coordinate().x, sourceNode.coordinate().y);
				
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
				assert !(env.tileStateAt(sourceNode.coordinate().x, sourceNode.coordinate().y) instanceof BombTileState);
				
				// Ok bomb is now exploded, get back to final destination
				finder = new PathFinder(env, Type.FindGoal, dest.x, dest.y);
				// There is an free path, which is guranteed
				// to be least expensive. So save the computing
				// time and don't simulate bombs
				finder._layBombs = false;
				
				path = finder.solution();
				this._stepCountSubroutines += finder._stepCount;
				this._timeConsumedInSubFinders += finder._timeConsumed;

				env = path.finalState();
				assert path.finalPlayerPosition().equals(dest);
			}
			
			return new Node(env, sourceNode, dest, env.currentTime() - srcEnv.currentTime() + env.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.coordinate()));
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
	
	protected int estimatedCost(EnvironmentState env, Point p) {
		if (this._type == Type.FindGoal)
			return (Math.abs(this._objX-p.x) + Math.abs(this._objY-p.y)) * env.miliTimeForTile();
		else if (this._type == Type.AvoidBomb) {
			int estimatedCost = 0;
			
			for (BombTileState bomb : env.bombTiles()) {
				Point b = bomb.coordinate();
				
				if (p.y == b.y) {
					if (Math.abs(p.x - b.x) <= bomb.width()) {
						estimatedCost = Math.max((Math.abs(p.x - b.x) + 1) * env.miliTimeForTile(),
								estimatedCost);
					}
				}
				else if (p.x == b.x) {
					if (Math.abs(p.y - b.y) <= bomb.width()) {
						estimatedCost = Math.max((Math.abs(p.y - b.y) + 1) * env.miliTimeForTile(),
								estimatedCost);
					}
				}
			}
			
			return estimatedCost;
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
		
		return new Path(lastNode.nodeState().steps(), new ArrayList<PathAssertion>(), lastNode.nodeState, this._startPoint, lastNode.coordinate());
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
