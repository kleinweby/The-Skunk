package theskunk;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class EnvironmentStateTest {
	
	@Test
	public void testEnvironmentState() {
		EnvironmentState state = new EnvironmentState(null, 0);
		
		assertNotNull(state);
	}

	@Test
	public void testTileStateAt() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		TileState tile1 = new TileState(TileState.BombTileTYpe, 10, 5);
		TileState tile2 = new TileState(TileState.BushTileType, 10, 5);
		
		parent.updateTileState(tile1);
		assertSame(tile1, parent.tileStateAt(10, 5));
		// Fall through
		assertSame(tile1, state.tileStateAt(10, 5));
		
		state.updateTileState(tile2);
		assertSame(tile2, state.tileStateAt(10, 5));
		// Not changed
		assertSame(tile1, parent.tileStateAt(10, 5));
	}
	
	@Test
	public void testMiliTimeForTile() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		
		parent.setMiliTimeForTile(10);
		
		assertEquals(10, parent.miliTimeForTile());
		assertEquals(10, state.miliTimeForTile());
		
		state.setMiliTimeForTile(30);
		assertEquals(10, parent.miliTimeForTile());
		assertEquals(30, state.miliTimeForTile());
	}

	@Test
	public void testSkunkWidth() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		
		parent.setSkunkWidth(10);
		assertEquals(10, parent.skunkWidth());
		
		// Value should fall through
		assertEquals(10, state.skunkWidth());
		
		state.setSkunkWidth(20);
		assertEquals(20, state.skunkWidth());
		assertEquals(10, parent.skunkWidth()); // Should not change
	}

	@Test
	public void testMaxSkunks() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		
		parent.setMaxSkunks(10);
		assertEquals(10, parent.maxSkunks());
		
		// Value should fall through
		assertEquals(10, state.maxSkunks());
		
		state.setMaxSkunks(20);
		assertEquals(20, state.maxSkunks());
		assertEquals(10, parent.maxSkunks()); // Should not change
	}

	@Test
	public void testCurrentTime() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		
		assertEquals(10, parent.currentTime());
				
		assertEquals(30, state.currentTime());
	}

	@Test
	public void testBombTiles() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		TileState bombTile1 = new TileState(TileState.BombTileTYpe, 5, 1);
		TileState bombTile2 = new TileState(TileState.BombTileTYpe, 10, 5);
		TileState bombTile3 = new TileState(TileState.BombTileTYpe, 5,1 );
		
		assertEquals(0, parent.bombTiles().size());
		assertEquals(0, state.bombTiles().size());

		parent.updateTileState(bombTile1);
		{
			HashSet<TileState> expected = new HashSet<TileState>();
			expected.add(bombTile1);
			assertEquals(expected, parent.bombTiles());
			assertEquals(expected, state.bombTiles());
		}
		
		state.updateTileState(bombTile2);
		{
			HashSet<TileState> expected = new HashSet<TileState>();
			expected.add(bombTile1);
			assertEquals(expected, parent.bombTiles());
			expected.add(bombTile2);
			assertEquals(expected, state.bombTiles());
		}
		
		state.updateTileState(bombTile3);
		{
			HashSet<TileState> expected = new HashSet<TileState>();
			expected.add(bombTile1);
			assertEquals(expected, parent.bombTiles());
			expected = new HashSet<TileState>();
			expected.add(bombTile2);
			expected.add(bombTile3);
			assertEquals(expected, state.bombTiles());
		}
	}

	@Test
	public void testAdvanceTime() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		TileState bushes[] = {
				new TileState(TileState.BushTileType, 2, 2),
				new TileState(TileState.BushTileType, 3, 2),
				new TileState(TileState.BushTileType, 4, 2),
				new TileState(TileState.BushTileType, 4, 3),
				new TileState(TileState.BushTileType, 4, 4),
				new TileState(TileState.BushTileType, 3, 4),
				new TileState(TileState.BushTileType, 2, 4),
				new TileState(TileState.BushTileType, 2, 3)
		};
		BombTileState bombTile = new BombTileState(3, 3, 50, 5);
		
		for (TileState s : bushes)
			parent.updateTileState(s);
		
		state.updateTileState(bombTile);
		
		for (TileState s : bushes) {
			assertEquals(s, state.tileStateAt(s.x(), s.y()));
		}
		
		{
			HashSet<TileState> expected = new HashSet<TileState>();
			assertEquals(expected, state.bombTiles());
		}
		
		state.advanceTime(50);
	}

}
