package project.go;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.MoveHandler;
import project.go.applogic.SingleMove;

public class MoveHandlerTest {

    private Board board;
    private MoveHandler moveHandler;

    @Before
    public void setUp() {
        board = new Board(19);
        moveHandler = new MoveHandler(board);
    }

    @Test
public void testKoRule_BasicKoScenario() {

    board.returnCurrentState()[4][4] = Color.WHITE;
    board.returnCurrentState()[3][4] = Color.BLACK;
    board.returnCurrentState()[5][4] = Color.BLACK;
    board.returnCurrentState()[4][3] = Color.BLACK;
    board.returnCurrentState()[4][5] = Color.WHITE;
    
    board.returnCurrentState()[3][3] = Color.BLACK;
    board.returnCurrentState()[5][3] = Color.BLACK;
    board.returnCurrentState()[3][5] = Color.WHITE;
    board.returnCurrentState()[5][5] = Color.WHITE;
    
    SingleMove captureMove = new SingleMove(4, 5);
    boolean captureResult = moveHandler.makeMove(captureMove, Color.WHITE);
    assertTrue(captureResult);
    
    assertEquals(Color.WHITE, board.returnCurrentState()[4][5]);
    assertEquals(Color.NONE, board.returnCurrentState()[4][4]);
    

    SingleMove koRecapture = new SingleMove(4, 4);
    boolean koResult = moveHandler.makeMove(koRecapture, Color.BLACK);
    assertFalse("Ko rule should prevent immediate recapture", koResult);
    
    assertEquals(Color.WHITE, board.returnCurrentState()[4][5]);
    assertEquals(Color.NONE, board.returnCurrentState()[4][4]);
}

@Test
public void testKoRule_KoIsTemporary() {

    board.returnCurrentState()[3][3] = Color.BLACK;
    board.returnCurrentState()[3][4] = Color.BLACK;
    board.returnCurrentState()[4][3] = Color.BLACK;
    board.returnCurrentState()[4][5] = Color.WHITE;
    board.returnCurrentState()[5][4] = Color.WHITE;
    board.returnCurrentState()[5][5] = Color.WHITE;
    board.returnCurrentState()[4][4] = Color.WHITE;
    

    SingleMove captureMove = new SingleMove(5, 3);
    moveHandler.makeMove(captureMove, Color.WHITE);
    

    SingleMove koRecapture = new SingleMove(4, 4);
    assertFalse(moveHandler.makeMove(koRecapture, Color.BLACK));
    

    SingleMove otherMove = new SingleMove(10, 10);
    assertTrue(moveHandler.makeMove(otherMove, Color.BLACK));
    

    SingleMove otherMove2 = new SingleMove(10, 11);
    assertTrue(moveHandler.makeMove(otherMove2, Color.WHITE));
    

    assertTrue("After another move, ko should be allowed", 
               moveHandler.makeMove(koRecapture, Color.BLACK));
}

@Test
public void testKoRule_MultipleCapturesNotKo() {

    board.returnCurrentState()[0][0] = Color.WHITE;
    board.returnCurrentState()[0][1] = Color.WHITE;
    board.returnCurrentState()[1][0] = Color.BLACK;
    board.returnCurrentState()[1][1] = Color.BLACK;
    board.returnCurrentState()[2][0] = Color.BLACK;
    board.returnCurrentState()[2][1] = Color.BLACK;

    SingleMove captureMove = new SingleMove(0, 2);
    moveHandler.makeMove(captureMove, Color.BLACK);

    SingleMove recapture = new SingleMove(0, 0);
    assertTrue("Multiple stone capture should not create ko", 
               moveHandler.makeMove(recapture, Color.WHITE));
}

@Test
public void testKoRule_KoAfterPass() {
    board.returnCurrentState()[5][5] = Color.BLACK;
    board.returnCurrentState()[5][6] = Color.BLACK;
    board.returnCurrentState()[6][5] = Color.BLACK;
    board.returnCurrentState()[6][7] = Color.WHITE;
    board.returnCurrentState()[7][6] = Color.WHITE;
    board.returnCurrentState()[7][7] = Color.WHITE;
    board.returnCurrentState()[6][6] = Color.WHITE;

    SingleMove captureMove = new SingleMove(7, 5);
    moveHandler.makeMove(captureMove, Color.WHITE);

    moveHandler.pass(Color.BLACK);

    moveHandler.pass(Color.WHITE);
    
    moveHandler.resumeGame(Color.BLACK);
    
    SingleMove koRecapture = new SingleMove(6, 6);
    assertFalse("Ko should still be prevented even after resume", 
                moveHandler.makeMove(koRecapture, Color.BLACK));
    
    SingleMove otherMove = new SingleMove(10, 10);
    moveHandler.makeMove(otherMove, Color.BLACK);
    moveHandler.makeMove(new SingleMove(10, 11), Color.WHITE);
    
    assertTrue("After another move, ko recapture should be allowed",
               moveHandler.makeMove(koRecapture, Color.BLACK));
}

    @Test
    public void testMakeMove_ValidMove() {
        SingleMove move = new SingleMove(4, 4);
        boolean result = moveHandler.makeMove(move, Color.BLACK);
        assertTrue(result);
        assertEquals(Color.BLACK, board.returnCurrentState()[4][4]);
        assertEquals(Color.WHITE, board.getCurrentTurn());
    }

    @Test
    public void testMakeMove_InvalidMoveOnOccupiedCell() {
        SingleMove move1 = new SingleMove(4, 4);
        SingleMove move2 = new SingleMove(4, 4);
        moveHandler.makeMove(move1, Color.BLACK);
        boolean result = moveHandler.makeMove(move2, Color.WHITE);
        assertFalse(result);
    }

    @Test
    public void testKoRule_PreventsImmediateRecapture() {
        board.returnCurrentState()[0][1] = Color.BLACK;
        board.returnCurrentState()[1][0] = Color.WHITE;
        board.returnCurrentState()[1][1] = Color.BLACK;

        SingleMove captureMove = new SingleMove(0, 0);
        moveHandler.makeMove(captureMove, Color.WHITE);

        SingleMove forbiddenKo = new SingleMove(1, 1);
        boolean result = moveHandler.makeMove(forbiddenKo, Color.BLACK);
        assertFalse(result);
    }

    @Test
    public void testRemoveDeadGroups_IncrementsPrisoners() {
        board.returnCurrentState()[4][4] = Color.WHITE;
        board.returnCurrentState()[3][4] = Color.BLACK;
        board.returnCurrentState()[5][4] = Color.BLACK;
        board.returnCurrentState()[4][3] = Color.BLACK;
        board.returnCurrentState()[4][5] = Color.BLACK;

        assertEquals(Color.NONE, board.returnCurrentState()[4][4]);
    }

    @Test
    public void testPassAndGameStopped() {
        moveHandler.pass(Color.BLACK);
        assertFalse(moveHandler.gameStopped);
        moveHandler.pass(Color.WHITE);
        assertTrue(moveHandler.gameStopped);
    }

    @Test
    public void testResolveAfterMove_SuicideMove() {
        board.returnCurrentState()[0][1] = Color.WHITE;
        board.returnCurrentState()[1][0] = Color.WHITE;
        board.returnCurrentState()[1][1] = Color.WHITE;

        SingleMove suicideMove = new SingleMove(0, 0);
        boolean result = moveHandler.makeMove(suicideMove, Color.BLACK);

        assertFalse(result);
        assertEquals(Color.NONE, board.returnCurrentState()[0][0]);
    }

}
