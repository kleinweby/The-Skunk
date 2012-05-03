package theskunk;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class EnvironmentStateTest {
	EnvironmentState cleanState;
	
	@Before
	public void setUp() {
		this.cleanState = new EnvironmentState(null, 0);
		
		for (int x = 0; x < EnvironmentState.FIELD_WIDTH; x++) {
			for (int y = 0; y < EnvironmentState.FIELD_HEIGHT; y++) {
				this.cleanState.updateTileState(new TileState(TileState.FreeTileType, x, y));
			}
		}
	}
	
	@Test
	public void testEnvironmentState() {
		EnvironmentState state = new EnvironmentState(null, 0);
		
		assertNotNull(state);
	}

	@Test
	public void testTileStateAt() {
		EnvironmentState parent = new EnvironmentState(this.cleanState, 10);
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
		EnvironmentState parent = new EnvironmentState(this.cleanState, 10);
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
		EnvironmentState parent = new EnvironmentState(this.cleanState, 10);
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
		EnvironmentState parent = new EnvironmentState(this.cleanState, 10);
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
		EnvironmentState parent = new EnvironmentState(this.cleanState, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		
		assertEquals(10, parent.currentTime());
				
		assertEquals(30, state.currentTime());
	}

	@Test
	public void testBombTiles() {
		EnvironmentState parent = new EnvironmentState(this.cleanState, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		BombTileState bombTile1 = new BombTileState(5, 1, 5);
		TileState bombTile2 = new BombTileState(10, 5, 5);
		TileState bombTile3 = new BombTileState(5, 1, 5);
		
		assertEquals(0, parent.bombTiles().size());
		assertEquals(0, state.bombTiles().size());

		parent.updateTileState(bombTile1);
		{
			HashSet<TileState> expected = new HashSet<TileState>();
			expected.add(bombTile1);
			parent.tileStateAt(5, 1);
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
	public void testSimulation() {
		EnvironmentState state = new EnvironmentState(this.cleanState, 20);
		EnvironmentState afterBombState;
		TileState tiles[][] = new TileState[EnvironmentState.FIELD_WIDTH][EnvironmentState.FIELD_HEIGHT];
		BombTileState bombTile = new BombTileState(3, 3, 5);
		
		for (int x = 0; x < EnvironmentState.FIELD_WIDTH; x++) {
			for (int y = 0; y < EnvironmentState.FIELD_HEIGHT; y++) {
				if (x == bombTile.x() && y == bombTile.y()) {
					tiles[x][y] = bombTile;
				}
				else {
					tiles[x][y] = new TileState(TileState.BushTileType, x, y);
				}
				state.updateTileState(tiles[x][y]);
			}
		}
		
		// Ensure the current state
		for (int x = 0; x < EnvironmentState.FIELD_WIDTH; x++) {
			for (int y = 0; y < EnvironmentState.FIELD_HEIGHT; y++) {
				assertSame(tiles[x][y], state.tileStateAt(x, y));
			}
		}
		
		// Check that the bomb is present
		{
			HashSet<TileState> expected = new HashSet<TileState>();
			expected.add(bombTile);
			assertEquals(expected, state.bombTiles());
		}
		
		afterBombState = new EnvironmentState(state, BombTileState.TimeToLive);
		
		// Ensure the bombed state with respect to the bombed
		// tiles
		for (int x = 0; x < EnvironmentState.FIELD_WIDTH; x++) {
			for (int y = 0; y < EnvironmentState.FIELD_HEIGHT; y++) {
				if ((x == 3 && y == 3) || (x == 2 && y == 3) || (x == 4 && y == 3)
						|| (x == 3 && y == 4) || (x == 3 && y == 2)) {
					TileState tile = afterBombState.tileStateAt(x, y);
					
					assertEquals(tile._tileType, TileState.FreeTileType);
				}
				else {
					assertSame(tiles[x][y], afterBombState.tileStateAt(x, y));
				}
			}
		}
	}

}
