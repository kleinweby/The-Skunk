package theskunk;
import java.util.Set;

import apoSkunkman.ai.ApoSkunkmanAIConstants;
import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public class PathFinder extends GenericAStar<EnvironmentState> {

	public PathFinder(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player) {
		EnvironmentState startState = new EnvironmentState(null, 0);
		
		// Populate this state
		byte byteLevel[][] = level.getLevelAsByte();
		
		for (int x = 0; x < byteLevel.length; x++) {
			for (int y = 0; y < byteLevel[x].length; y++) {
				TileState tileState = null;
				
				switch (byteLevel[x][y]) {
				case ApoSkunkmanAIConstants.LEVEL_FREE:
					tileState = new TileState(TileState.FreeTileType);
					break;
				case ApoSkunkmanAIConstants.LEVEL_BUSH:
					tileState = new TileState(TileState.BushTileType);
					break;
				case ApoSkunkmanAIConstants.LEVEL_GOODIE:
					tileState = new TileState(TileState.GoodieTileType);
					break;
				}
				
				startState.setTileState(tileState, x, y);
			}
		}
		
		startState.setMaxSkunks(player.getMaxSkunkman());
		startState.setMiliTimeForTile(player.getMSForOneTile());
		startState.setSkunkWidth(player.getSkunkWidth());
		
		this.setStartNode(new Node(startState, null, 0, 0));
	}
	
	@Override
	protected Set<Node> adjacentNodes(Node sourceNode) {
		// TODO Auto-generated method stub
		return null;
	}
}
