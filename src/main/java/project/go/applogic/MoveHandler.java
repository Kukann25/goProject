package project.go.applogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import project.go.Config;

/**
 * Class MoveHandler handles every move of each player
 */

public class MoveHandler extends Handler{

    public boolean lastMoveWasPass = false;
    public boolean gameStopped = false;

    private Color[][] koGrid;
    private boolean lastMoveWasKoBeat = false;

    private int size=Config.DEFAULT_BOARD_SIZE;

    private PointHandler pointHandler;

    public Board getBoard() {
        return board;
    }

    /**
     * 
     * @param board each board has its own movehandler
     */
    public MoveHandler(Board board){
        this.board=board; 

        pointHandler = new PointHandler(board);

        this.koGrid = new Color[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                koGrid[i][j]=Color.NONE;
            }
        }
    }

    /**
     * Function copy_grid that copies board into temporary array (needed for ko check)
     * @param boardGrid temporary array to copy
     */
    private void copy_grid(Color[][] boardGrid){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                boardGrid[i][j]=this.board.returnCurrentState()[i][j];
            }
        }
    }

    /**
     * Function copy_grid that resets temporary array (needed for ko check)
     * @param boardGrid temporary array to copy
     */
    private void reset_grid(Color[][] boardGrid){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                boardGrid[i][j]=Color.NONE;
            }
        }
    }

    /**
     * Function insert_grid that inserts temporary array into board (needed for ko check)
     * @param grid grid from which we will insert
     */
    private void insert_grid(Color[][] grid){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                this.board.returnCurrentState()[i][j]=grid[i][j];
            }
        }
    }

    /**
     * grid comparator for ko checking
     * @param grid1
     * @param grid2
     * @return true if they are equal false if not
     */
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

    /**
     * FloodFill algorithm for checking chains od stones
     * @param startX
     * @param startY
     * @param grid the board grid to check on
     * @return ChainResult class that contains set of all chains and set of all liberties
     */
    private ChainResult floodFill(int startX, int startY, Color[][] grid){
        Color color = grid[startX][startY];

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

                if (grid[nx][ny] == Color.NONE) {
                    result.liberties.add(new SingleMove(nx, ny));
                } else if (grid[nx][ny] == color) {
                    stack.push(new SingleMove(nx, ny));
                }
            }
        }
        return result;
    }

    /**
     * Function resolveAfterMove that proceeds FloodFill and checks for ko.
     * @param x
     * @param y
     * @param side
     * @return whether move was correct
     */
    public boolean resolveAfterMove(int x, int y, Color side) {

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        Color[][] boardState;

        int capturedStones = 0;
        SingleMove koCandidate = null;

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
    
            ChainResult enemyChain = floodFill(nx, ny, boardState);
            checked.addAll(enemyChain.chain);
    
            if (enemyChain.liberties.isEmpty()) {
                for (SingleMove p : enemyChain.chain) {
                    boardState[p.getX()][p.getY()] = Color.NONE;
                    capturedStones++;
                    koCandidate = p;
                }
            }
        }
    
        ChainResult myChain = floodFill(x, y, boardState);
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
            if(koCandidate != null) {
                 koGrid[x][y] = Color.NONE;
                 koGrid[koCandidate.getX()][koCandidate.getY()] = opponent;
            }
        }
        else{
            lastMoveWasKoBeat=false;
            reset_grid(koGrid);
        }

        insert_grid(boardState);
        pointHandler.addPoints(capturedStones, side);
        return true;
    }
    


    /**
     * Checks if move is legal
     * @param x coordinate
     * @param y coordinate
     * @param side player color
     * @return true if move is legal
     */
    public boolean isLegal(int x, int y, Color side) {
        if (!isValid(x, y)) return false;
        if (board.returnCurrentState()[x][y] != Color.NONE) return false;
        if (side != board.getCurrentTurn()) return false;

        Color[][] testBoard = new Color[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                testBoard[i][j]=this.board.returnCurrentState()[i][j];
            }
        }
        testBoard[x][y] = side;

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        Set<SingleMove> checked = new HashSet<>();

        for (int i = 0; i < 4; i++) {
            int nx = x + DX[i];
            int ny = y + DY[i];
    
            if (!isValid(nx, ny)) continue;
            if (testBoard[nx][ny] != opponent) continue;

            SingleMove start = new SingleMove(nx, ny);
            if (checked.contains(start)) continue;
    
            ChainResult enemyChain = floodFill(nx, ny, testBoard);
            checked.addAll(enemyChain.chain);
    
            if (enemyChain.liberties.isEmpty()) {
                for (SingleMove p : enemyChain.chain) {
                    testBoard[p.getX()][p.getY()] = Color.NONE;
                }
            }
        }
    
        ChainResult myChain = floodFill(x, y, testBoard);
        if (myChain.liberties.isEmpty()) {
            return false;
        }

        if(lastMoveWasKoBeat && compare_grids(testBoard, koGrid)){
            return false;
        }

        return true;
    }


    /**
     * Generates all legal moves for a given side
     * @param side
     * @return List of SingleMove
     */
    public List<SingleMove> generateLegalMoves(Color side) {
        List<SingleMove> legalMoves = new ArrayList<>();
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
                if(isLegal(i, j, side)) {
                    legalMoves.add(new SingleMove(i, j));
                }
            }
        }
        return legalMoves;
    }

    /**
     * Evaluates a move and returns metrics
     * @param x
     * @param y
     * @param side
     * @return MoveMetrics object
     */
    public MoveMetrics evaluateMove(int x, int y, Color side) {
        MoveMetrics metrics = new MoveMetrics();

        if (!isValid(x, y)) return metrics;
        if (board.returnCurrentState()[x][y] != Color.NONE) return metrics;

        Color[][] testBoard = new Color[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                testBoard[i][j]=this.board.returnCurrentState()[i][j];
            }
        }
        testBoard[x][y] = side;

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        Set<SingleMove> checked = new HashSet<>();
        
        for (int i = 0; i < 4; i++) {
            int nx = x + DX[i];
            int ny = y + DY[i];
    
            if (!isValid(nx, ny)) continue;
            if (testBoard[nx][ny] != opponent) continue;

            SingleMove start = new SingleMove(nx, ny);
            if (checked.contains(start)) continue;
    
            ChainResult enemyChain = floodFill(nx, ny, testBoard);
            checked.addAll(enemyChain.chain);
    
            if (enemyChain.liberties.isEmpty()) {
                metrics.capturedStones += enemyChain.chain.size();
                for (SingleMove p : enemyChain.chain) {
                    testBoard[p.getX()][p.getY()] = Color.NONE;
                }
            } else {
                // Not captured, check how choked they are
                if (enemyChain.liberties.size() < metrics.minOpponentLiberties) {
                    metrics.minOpponentLiberties = enemyChain.liberties.size();
                }
            }
        }
        
        // Check self liberties
        ChainResult myChain = floodFill(x, y, testBoard);
        if (myChain.liberties.isEmpty()) {
            return metrics; // Suicide, isLegal remains false
        }
        metrics.selfLiberties = myChain.liberties.size();

        // Ko check
        if(lastMoveWasKoBeat && compare_grids(testBoard, koGrid)){
            return metrics; // Ko, isLegal remains false
        }

        metrics.isLegal = true;
        return metrics;
    }

    /**
     * Helper to get captured stones count for a move (simulation)
     * @param x
     * @param y
     * @param side
     * @return number of captured stones, or -1 if move is illegal/suicide
     */
    public int getCapturedStoneCount(int x, int y, Color side) {
        if (!isValid(x, y)) return -1;
        if (board.returnCurrentState()[x][y] != Color.NONE) return -1;
        
        Color[][] testBoard = new Color[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                testBoard[i][j]=this.board.returnCurrentState()[i][j];
            }
        }
        testBoard[x][y] = side;

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        Set<SingleMove> checked = new HashSet<>();
        int captured = 0;

        for (int i = 0; i < 4; i++) {
            int nx = x + DX[i];
            int ny = y + DY[i];
    
            if (!isValid(nx, ny)) continue;
            if (testBoard[nx][ny] != opponent) continue;

            SingleMove start = new SingleMove(nx, ny);
            if (checked.contains(start)) continue;
    
            ChainResult enemyChain = floodFill(nx, ny, testBoard);
            checked.addAll(enemyChain.chain);
    
            if (enemyChain.liberties.isEmpty()) {
                captured += enemyChain.chain.size();
                for (SingleMove p : enemyChain.chain) {
                    testBoard[p.getX()][p.getY()] = Color.NONE;
                }
            }
        }
        
        // Check suicide
        ChainResult myChain = floodFill(x, y, testBoard);
        if (myChain.liberties.isEmpty()) {
            return -1; // Suicide
        }

        return captured;
    }

    /**
     * Function makeMove that proceeds a single move from one player
     * @param move
     * @param side
     * @return
     */
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
        lastMoveWasPass=false;
        board.switchTurn();
        return true;
    }

    /**
     * Function that removes one stone from the board and adds a point to player that captured it 
     * @param side
     * @param x
     * @param y
     */
    public void removeStone(Color side, int x, int y){
        board.returnCurrentState()[x][y]=Color.NONE;
        pointHandler.addPoints(1, side);
    }

    /**
     * Function removeStones to remove stones after negotiations
     * @param statusHolder holds grid of stone statuses
     */
    public void removeStones(StoneStatusHolder statusHolder){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(statusHolder.returnStoneStatus()[i][j]==StoneStatus.DEAD){
                    pointHandler.addPoints(1, board.returnCurrentState()[i][j]);
                    board.returnCurrentState()[i][j]=Color.NONE;
                }
            }
        }
    }


    /**
     * function for passing (useful in tests)
     * @param side
     */
    public void pass(Color side) {
        if (side != board.getCurrentTurn()) return;
    
        if (lastMoveWasPass) {
            gameStopped = true;
        }
    
        lastMoveWasPass = true;
        board.switchTurn();
    }

    /**
     * Function for resuming the game
     * @param requestingPlayer
     */
    public void resumeGame(Color requestingPlayer) {
        gameStopped = false;
        lastMoveWasPass = false;
        board.setCurrentTurn(requestingPlayer);
    }

    /**
     * Funcition resolvePoints for calculating points
     */
    public void resolvePoints(){
        pointHandler.calculateTerritoryPoints();
    }
    
    /**
     * Function that returns points for given player
     * @param side
     * @return
     */
    public int returnPoints(Color side){
        if(side==Color.WHITE){
            return pointHandler.whitePoints();
        }
        else if(side==Color.BLACK){
            return pointHandler.blackPoints();
        }
        else{
            return -1;
        }
    }
    
}
