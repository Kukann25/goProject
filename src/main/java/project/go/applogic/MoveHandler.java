package project.go.applogic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import project.go.Config;

public class MoveHandler{

    private Board board;

    private static final int[] DX = {-1, 1, 0, 0};
    private static final int[] DY = {0, 0, -1, 1};

    public boolean lastMoveWasPass = false;
    public boolean gameStopped = false;


    private Color[][] koGrid;
    private boolean lastMoveWasKoBeat = false;

    private int size=Config.DEFAULT_BOARD_SIZE;


    public MoveHandler(Board board){
        this.board=board; 

        this.koGrid = new Color[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                koGrid[i][j]=Color.NONE;
            }
        }
    }

    private void copy_grid(Color[][] boardGrid){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                boardGrid[i][j]=this.board.returnCurrentState()[i][j];
            }
        }
    }

    private void reset_grid(Color[][] boardGrid){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                boardGrid[i][j]=Color.NONE;
            }
        }
    }

    private void insert_grid(Color[][] grid){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                this.board.returnCurrentState()[i][j]=grid[i][j];
            }
        }
    }

    private boolean compare_grids(Color[][] grid1, Color[][] grid2){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(grid1[i][j]!=grid2[i][j]){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int x, int y) {
        if (x < 0 || x >= board.getSize() || y < 0 || y >= board.getSize()) {
            return false;
        }
        return true;
    }


    private ChainResult floodFill(int startX, int startY){
        Color color = board.returnCurrentState()[startX][startY];

        ChainResult result = new ChainResult();
        Set<SingleMove> visited = new HashSet<>();
        Stack<SingleMove> stack = new Stack<>();

        stack.push(new SingleMove(startX, startY));

        while (!stack.isEmpty()) {
            SingleMove p = stack.pop();

            if (visited.contains(p)) continue;
            visited.add(p);
            result.chain.add(p);

            for (int i = 0; i < 4; i++) {
                int nx = p.getX() + DX[i];
                int ny = p.getY() + DY[i];

                if (!isValid(nx, ny))
                    continue;

                if (board.returnCurrentState()[nx][ny] == Color.NONE) {
                    result.liberties.add(new SingleMove(nx, ny));
                } else if (board.returnCurrentState()[nx][ny] == color) {
                    stack.push(new SingleMove(nx, ny));
                }
            }
        }
        return result;
    }

    public boolean resolveAfterMove(int x, int y, Color side) {

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        Color[][] boardState;

        int capturedStones = 0;

        Set<SingleMove> checked = new HashSet<>();

        if(lastMoveWasKoBeat==true){
            boardState = new Color[size][size];
            copy_grid(boardState);
        }else{
            boardState=board.returnCurrentState();
        }
    
        for (int i = 0; i < 4; i++) {
            int nx = x + DX[i];
            int ny = y + DY[i];
    
            if (!isValid(nx, ny)) continue;
            if (boardState[nx][ny] != opponent) continue;

            SingleMove start = new SingleMove(nx, ny);
            if (checked.contains(start)) continue;
    
            ChainResult enemyChain = floodFill(nx, ny);
            checked.addAll(enemyChain.chain);
    
            if (enemyChain.liberties.isEmpty()) {
                for (SingleMove p : enemyChain.chain) {
                    boardState[p.getX()][p.getY()] = Color.NONE;
                    capturedStones++;
                }
            }
        }
    
        ChainResult myChain = floodFill(x, y);
        if (myChain.liberties.isEmpty()) {
            for (SingleMove p : myChain.chain) {
                boardState[p.getX()][p.getY()] = Color.NONE;
            }
            return false;
        }

        if(lastMoveWasKoBeat&&compare_grids(boardState, koGrid)==true){
            return false;
        }
        
        if (capturedStones == 1) {
            lastMoveWasKoBeat=true;
            copy_grid(koGrid);
        }
        else{
            lastMoveWasKoBeat=false;
            reset_grid(koGrid);
        }

        insert_grid(boardState);
        return true;
    }
    

    public boolean makeMove(SingleMove move, Color side) {

        int x = move.getX();
        int y = move.getY();
    
        if (!isValid(x, y)) return false;
        if (board.returnCurrentState()[x][y] != Color.NONE) return false;
        if (side != board.getCurrentTurn()) return false;
    
        board.returnCurrentState()[x][y] = side;
    
        if (!resolveAfterMove(x, y, side)) {
            board.returnCurrentState()[x][y] = Color.NONE;
            return false;
        }

        board.switchTurn();
        return true;
    }

    public void pass(Color side) {
        if (side != board.getCurrentTurn()) return;
    
        if (lastMoveWasPass) {
            gameStopped = true;
        }
    
        lastMoveWasPass = true;
        board.switchTurn();
    }

    public void resumeGame(Color requestingPlayer) {
        gameStopped = false;
        lastMoveWasPass = false;
        board.setCurrentTurn(requestingPlayer);
    }
    
    
}
