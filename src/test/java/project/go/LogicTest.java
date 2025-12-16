package project.go;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.MoveHandler;
import project.go.applogic.SingleMove;

/**
 * Tests for Board class
 */
public class LogicTest {
    @Test
    public void boardInitializationTest(){
        Board board = new Board(19);
        assertEquals(19, board.getSize());
    }

    @Test
    public void MoveHandlerTest(){
        Color black = Color.BLACK;
        Color white = Color.WHITE;
        Board board = new Board(19);
        MoveHandler moveHandler = new MoveHandler(board);
        moveHandler.makeMove(new SingleMove(1, 0), black);
        moveHandler.makeMove(new SingleMove(0, 1), black);
        assertFalse(moveHandler.makeMove(new SingleMove(0, 0), white));
        moveHandler.makeMove(new SingleMove(10, 10), black);
        moveHandler.makeMove(new SingleMove(9, 10), white);
        moveHandler.makeMove(new SingleMove(11, 10), white);
        moveHandler.makeMove(new SingleMove(10, 9), white);
        moveHandler.makeMove(new SingleMove(10, 11), white);
        assertEquals(Color.NONE, board.returnCurrentState()[10][10]);
        moveHandler.makeMove(new SingleMove(10, 0), black);
        moveHandler.makeMove(new SingleMove(11, 0), white);
        moveHandler.makeMove(new SingleMove(9, 0), white);
        moveHandler.makeMove(new SingleMove(10, 1), white);
        assertEquals(Color.NONE, board.returnCurrentState()[10][0]);
        assertFalse(moveHandler.makeMove(new SingleMove(-1, -1), white));
    }
}
