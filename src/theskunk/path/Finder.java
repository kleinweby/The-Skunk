package theskunk.path;
import java.awt.Point;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import theskunk.GenericAStar;
import theskunk.environment.BombTileState;
import theskunk.environment.Environment;
import theskunk.environment.TileState;
import theskunk.path.assertions.Assertion;
import theskunk.path.assertions.PathBushAssertion;
import theskunk.path.assertions.PathPlayerPositionAssertion;
import theskunk.path.assertions.PathSkunkWidthAssertion;
import theskunk.path.assertions.PathSpeedAssertion;
import theskunk.path.steps.LayBombStep;
import theskunk.path.steps.MoveStep;
import theskunk.path.steps.Step;
import theskunk.path.steps.WaitStep;
import theskunk.path.steps.MoveStep.Direction;

public class Finder extends GenericAStar<Environment> {
	public enum Type {
		FindGoal,
		AvoidBomb,
		BombAway
	}
	
	private Type _type;
	private Point _startPoint;
	private Point _objPoint;
	private boolean _layBombs;
	
	private int _stepCount;
	private int _stepCountSubroutines;
	private long _timeConsumed;
	private long _timeConsumedInSubFinders;
	private Environment _startEnv;
	
	public Finder(Environment env, Type type, Point obj) {		
		this._objPoint = new Point(obj);
		this._type = type;
		this._startPoint = env.playerPosition();
		this._startEnv = env;
		
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
			
			if (n != null && n.nodeState().isPlayerAlive())
				nodes.add(n);
		}
		
		if (p.x + 1 < Environment.FIELD_WIDTH) {
			Node n = nodeFromTo(sourceNode, Direction.Right);
			
			if (n != null && n.nodeState().isPlayerAlive())
				nodes.add(n);
		}
		
		if (p.y >= 1) {
			Node n = nodeFromTo(sourceNode, Direction.Up);
			
			if (n != null && n.nodeState().isPlayerAlive())
				nodes.add(n);
		}
		
		if (p.y + 1 < Environment.FIELD_HEIGHT) {
			Node n = nodeFromTo(sourceNode, Direction.Down);
			
			if (n != null && n.nodeState().isPlayerAlive())
				nodes.add(n);
		}
		
		return nodes;
	}
	
	protected Node nodeFromTo(Node sourceNode, Direction direction) {
		assert sourceNode != null;
		Point dest = new Point(sourceNode.coordinate());
		
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
		
		Environment srcEnv = sourceNode.nodeState();
		TileState currentState = srcEnv.tileStateAt(dest.x, dest.y);
				
		// Perfect =) we can simply go there
		if (currentState.tileType() == TileState.FreeTileType) {
			Environment env = srcEnv;
			
			{
				Step step = new MoveStep(direction);
				step.addAssertion(new PathPlayerPositionAssertion(env.playerPosition()));
			
				env = new Environment(env, step);
			}
			
			return new Node(env, sourceNode, dest, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, dest));
		}
		else if (currentState.tileType() == TileState.GoodieTileType) {
			Environment env = srcEnv;
			
			// Goodies will not be handled separatly in the sense of cost, because
			// it is the path finders role to do so (A bad goodie may be good
			// for the solution)
						
			{
				Step step = new MoveStep(direction);
				step.addAssertion(new PathPlayerPositionAssertion(env.playerPosition()));
			
				env = new Environment(env, step);
			}
			
			return new Node(env, sourceNode, dest, srcEnv.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.coordinate()));
		}
		// Do not blow up bushes when we're currently running away from an bomb
		else if (currentState.tileType() == TileState.BushTileType && this._layBombs) {
			Environment env = srcEnv;
			
			{
				LayBombStep step = new LayBombStep();
				// We dont need to blow something up that is not there
				step.addAssertion(new PathBushAssertion(dest, true));
				// We have to make sure we are where we're thinking we are ;)
				step.addAssertion(new PathPlayerPositionAssertion(env.playerPosition()));
				// We have to make sure our escape is right (timing/distance)
				step.addAssertion(new PathSkunkWidthAssertion(env.skunkWidth()));
				step.addAssertion(new PathSpeedAssertion(env.miliTimeForTile()));
				// Will insert a bomb tile
				env = new Environment(env, step);
			}
			
			// Find escape route
			{	
				Finder finder = new Finder(env, Type.AvoidBomb, new Point(0,0));
				
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
					env = new Environment(env, new WaitStep(remainingBombTime));
				}
				
				// There should not be a bomb now
				assert !(env.tileStateAt(sourceNode.coordinate().x, sourceNode.coordinate().y) instanceof BombTileState);
				
				// Ok bomb is now exploded, get back to final destination
				finder = new Finder(env, Type.FindGoal, dest);
				// There is an free path, which is guranteed
				// to be least expensive. So save the computing
				// time and don't simulate bombs
				finder._layBombs = false;
				
				path = finder.solution();
				this._stepCountSubroutines += finder._stepCount;
				this._timeConsumedInSubFinders += finder._timeConsumed;

				env = path.finalState();
				// Looks like there is no solution path here =S
				// TODO: investigate this futhure. Was introduced
				// with premature bombing detection.
				if (!path.finalPlayerPosition().equals(dest)) {
					return null;
				}
			}
			
			return new Node(env, sourceNode, dest, env.currentTime() - srcEnv.currentTime() + env.miliTimeForTile(), 
					this.estimatedCost(env, sourceNode.coordinate()));
		}
		// We can not over a bomb (why?!)
		else if (currentState.tileType() == TileState.BombTileType) {
			return null;
		}
		else if (currentState.tileType() == TileState.StoneTileType) {
			// We can not go here
			return null;
		}
		
		return null;
	}
	
	protected int estimatedCost(Environment env, Point p) {
		if (this._type == Type.FindGoal)
			return (Math.abs(this._objPoint.x - p.x) + Math.abs(this._objPoint.y - p.y)) * env.miliTimeForTile();
		else if (this._type == Type.BombAway) {
			// If the target could be bombed away by the skund width
			// we've reached the goal
			if (p.y == this._objPoint.y && Math.abs(p.x - this._objPoint.x) <= env.skunkWidth())
				return 0;
			else if (p.x == this._objPoint.x && Math.abs(p.y - this._objPoint.y) <= env.skunkWidth())
				return 0;
			
			return (Math.abs(this._objPoint.x-p.x) + Math.abs(this._objPoint.y-p.y)) * env.miliTimeForTile();
		}
		else if (this._type == Type.AvoidBomb) {
			int estimatedCost = 0;
			
			for (BombTileState bomb : env.bombTiles()) {
				Point b = bomb.coordinate();
				
				// TODO: check if there is already a bush/stone
				// that would mark us save =)
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
		
		if (nodePath == null)
			return new Path(new ArrayList<Step>(), new ArrayList<Assertion>(), this._startEnv, this._startPoint, this._startPoint);
		
		Node lastNode = nodePath().get(nodePath.size() - 1);
		Environment env = lastNode.nodeState;
		List<Assertion> assertions = new ArrayList<Assertion>();
		
		if (this._type == Type.BombAway) {
			env = new Environment(env, new LayBombStep());
			
			Finder f = new Finder(env, Type.AvoidBomb, env.playerPosition());
			// Wait for it to explode
//			BombTileState bomb = (BombTileState)env.tileStateAt(env.playerPosition().x, env.playerPosition().y);
			env = f.solution().finalState();
//			env = new Environment(env, new WaitStep(bomb.timeExploded() - env.currentTime()));
		}
		
		return new Path(env.steps(), assertions, env, this._startPoint, env.playerPosition());
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
