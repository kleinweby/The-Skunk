package theskunk;

import static org.junit.Assert.*;

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
		TileState tile1 = new TileState(TileState.BombTileTYpe);
		TileState tile2 = new TileState(TileState.BushTileType);
		
		parent.setTileState(tile1, 10, 5);
		assertSame(tile1, parent.tileStateAt(10, 5));
		// Fall through
		assertSame(tile1, state.tileStateAt(10, 5));
		
		state.setTileState(tile2, 10, 5);
		assertSame(tile2, state.tileStateAt(10, 5));
		// Not changed
		assertSame(tile1, parent.tileStateAt(10, 5));
	}

	@Test
	public void testMiliTimeForTileConstructor() {
		EnvironmentState state = new EnvironmentState(null, 10);
		
		assertEquals(10, state.currentTime());
		
		state = new EnvironmentState(null, 0);
		
		assertEquals(0, state.currentTime());
	}
	
	@Test
	public void testMiliTimeForTileParent() {
		EnvironmentState parent = new EnvironmentState(null, 10);
		EnvironmentState state = new EnvironmentState(parent, 20);
		
		assertEquals(10, parent.currentTime());
				
		assertEquals(30, state.currentTime());
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
		fail("Not yet implemented");
	}

	@Test
	public void testBombTiles() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTileState() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMiliTimeForTile() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSkunkWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMaxSkunks() {
		fail("Not yet implemented");
	}

	@Test
	public void testAdvanceTime() {
		fail("Not yet implemented");
	}

}
