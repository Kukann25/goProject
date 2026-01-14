package project.go;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.GroupStatus;
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

        moveHandler.updateGroupStatus();
        
        for (Set<SingleMove> group : moveHandler.groupStatus.keySet()) {
            moveHandler.groupStatus.put(group, GroupStatus.DEAD);
        }

        moveHandler.removeDeadGroups();

        assertEquals(Color.NONE, board.returnCurrentState()[4][4]);
        assertEquals(1, moveHandler.getPrisoners(Color.BLACK));
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
