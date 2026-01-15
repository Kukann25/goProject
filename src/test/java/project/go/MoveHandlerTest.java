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

    moveHandler.makeMove(new SingleMove(4, 4), Color.BLACK);
    moveHandler.makeMove(new SingleMove(5, 4), Color.WHITE);
    moveHandler.makeMove(new SingleMove(3, 3), Color.BLACK);
    moveHandler.makeMove(new SingleMove(6, 3), Color.WHITE);
    moveHandler.makeMove(new SingleMove(4, 2), Color.BLACK);
    moveHandler.makeMove(new SingleMove(5, 2), Color.WHITE);
    moveHandler.makeMove(new SingleMove(5, 3), Color.BLACK);

    SingleMove captureMove = new SingleMove(4, 3);
    boolean captureResult = moveHandler.makeMove(captureMove, Color.WHITE);
    assertTrue(captureResult);
    
    assertEquals(Color.WHITE, board.returnCurrentState()[4][3]);
    assertEquals(Color.NONE, board.returnCurrentState()[5][3]);
    

    SingleMove koRecapture = new SingleMove(5, 3);
    boolean koResult = moveHandler.makeMove(koRecapture, Color.BLACK);
    assertFalse("Ko rule should prevent immediate recapture", koResult);
    
    assertEquals(Color.WHITE, board.returnCurrentState()[4][3]);
    assertEquals(Color.NONE, board.returnCurrentState()[5][3]);
}

@Test
public void testKoRule_KoIsTemporary() {

    moveHandler.makeMove(new SingleMove(4, 4), Color.BLACK);
    moveHandler.makeMove(new SingleMove(5, 4), Color.WHITE);
    moveHandler.makeMove(new SingleMove(3, 3), Color.BLACK);
    moveHandler.makeMove(new SingleMove(6, 3), Color.WHITE);
    moveHandler.makeMove(new SingleMove(4, 2), Color.BLACK);
    moveHandler.makeMove(new SingleMove(5, 2), Color.WHITE);
    moveHandler.makeMove(new SingleMove(5, 3), Color.BLACK);
    

    SingleMove captureMove = new SingleMove(4, 3);
    moveHandler.makeMove(captureMove, Color.WHITE);
    

    SingleMove koRecapture = new SingleMove(5, 3);
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

    moveHandler.makeMove(new SingleMove(4, 4), Color.BLACK);
    moveHandler.makeMove(new SingleMove(5, 4), Color.WHITE);
    moveHandler.makeMove(new SingleMove(3, 4), Color.BLACK);
    moveHandler.makeMove(new SingleMove(6, 3), Color.WHITE);
    moveHandler.makeMove(new SingleMove(2, 3), Color.BLACK);
    moveHandler.makeMove(new SingleMove(5, 2), Color.WHITE);
    moveHandler.makeMove(new SingleMove(3, 2), Color.BLACK);
    moveHandler.makeMove(new SingleMove(4, 3), Color.WHITE);
    moveHandler.makeMove(new SingleMove(4, 2), Color.BLACK);
    moveHandler.makeMove(new SingleMove(3, 3), Color.WHITE);

    SingleMove captureMove = new SingleMove(5, 3);
    moveHandler.makeMove(captureMove, Color.BLACK);

    SingleMove recapture = new SingleMove(4, 3);
    assertTrue("Multiple stone capture should not create ko", 
               moveHandler.makeMove(recapture, Color.WHITE));
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
