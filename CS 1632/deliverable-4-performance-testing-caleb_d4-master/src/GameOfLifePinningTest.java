import org.junit.*;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameOfLifePinningTest {
	/*
	 * READ ME: You may need to write pinning tests for methods from multiple
	 * classes, if you decide to refactor methods from multiple classes.
	 * 
	 * In general, a pinning test doesn't necessarily have to be a unit test; it can
	 * be an end-to-end test that spans multiple classes that you slap on quickly
	 * for the purposes of refactoring. The end-to-end pinning test is gradually
	 * refined into more high quality unit tests. Sometimes this is necessary
	 * because writing unit tests itself requires refactoring to make the code more
	 * testable (e.g. dependency injection), and you need a temporary end-to-end
	 * pinning test to protect the code base meanwhile.
	 * 
	 * For this deliverable, there is no reason you cannot write unit tests for
	 * pinning tests as the dependency injection(s) has already been done for you.
	 * You are required to localize each pinning unit test within the tested class
	 * as we did for Deliverable 2 (meaning it should not exercise any code from
	 * external classes). You will have to use Mockito mock objects to achieve this.
	 * 
	 * Also, you may have to use behavior verification instead of state verification
	 * to test some methods because the state change happens within a mocked
	 * external object. Remember that you can use behavior verification only on
	 * mocked objects (technically, you can use Mockito.verify on real objects too
	 * using something called a Spy, but you wouldn't need to go to that length for
	 * this deliverable).
	 */

	/* TODO: Declare all variables required for the test fixture. */
	Cell[][] cells;
	MainPanel panel;
	Cell cell;
	
	@Before
	public void setUp() {
		/*
		 * TODO: initialize the text fixture. For the initial pattern, use the "blinker"
		 * pattern shown in:
		 * https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life#Examples_of_patterns
		 * The actual pattern GIF is at:
		 * https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life#/media/File:Game_of_life_blinker.gif
		 * Start from the vertical bar on a 5X5 matrix as shown in the GIF.
		 */
		cells = new Cell[5][5];
		
		for (int j = 0; j < 5; j++) {
			for (int k = 0; k < 5; k++) {
				cell = Mockito.mock(Cell.class);
				cells[j][k] = cell;
			}
		}
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				Mockito.when(cells[i][j].getAlive()).thenReturn(false);
				Mockito.when(cells[i][j].toString()).thenReturn(".");
			}
		}
		
		Mockito.when(cells[1][2].getAlive()).thenReturn(true);
		Mockito.when(cells[1][2].toString()).thenReturn("X");
		Mockito.when(cells[2][2].getAlive()).thenReturn(true);
		Mockito.when(cells[2][2].toString()).thenReturn("X");
		Mockito.when(cells[3][2].getAlive()).thenReturn(true);
		Mockito.when(cells[3][2].toString()).thenReturn("X");
		
		panel = new MainPanel(cells);
	}

	/* TODO: Write the three pinning unit tests for the three optimized methods */
	//
	@Test
	public void testIterateCells() {
		for(int i = 0; i < 5; i++) {
			assertFalse(panel.iterateCell(0, i));
		}
		for(int i = 0; i < 5; i++) {
			assertFalse(panel.iterateCell(1, i));
		}
		
		assertFalse(panel.iterateCell(2, 0));
		assertTrue(panel.iterateCell(2, 1));
		assertTrue(panel.iterateCell(2, 2));
		assertTrue(panel.iterateCell(2, 3));
		assertFalse(panel.iterateCell(2, 4));

		for(int i = 0; i < 5; i++) {
			assertFalse(panel.iterateCell(3, i));
		}
		
		for(int i = 0; i < 5; i++) {
			assertFalse(panel.iterateCell(4, i));
		}
		
	}
	
	@Test
	public void testCalculateNextIteration() {
		//Add this test
	
		panel.calculateNextIteration();
		Cell[][] cells = panel.getCells();
		
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 5; j++) {
				Mockito.verify(cells[i][j], Mockito.times(1)).setAlive(false);
			}
		}

		Mockito.verify(cells[2][0], Mockito.times(1)).setAlive(false);
		Mockito.verify(cells[2][1], Mockito.times(1)).setAlive(true);
		Mockito.verify(cells[2][2], Mockito.times(1)).setAlive(true);
		Mockito.verify(cells[2][3], Mockito.times(1)).setAlive(true);
		Mockito.verify(cells[2][4], Mockito.times(1)).setAlive(false);

		for(int i = 3; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				Mockito.verify(cells[i][j], Mockito.times(1)).setAlive(false);
			}
		}
	}
	
	@Test
	public void testCellToString() {
		//Add this test		
		panel.toString();
		String result = "";
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				Mockito.verify(cells[i][j], Mockito.times(1)).getAlive();
				result += cells[i][j];
			}
			result += "\n";
		} 
		
		assertEquals(".....\n..X..\n..X..\n..X..\n.....\n", result);
	}
}
