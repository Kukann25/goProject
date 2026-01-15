package project.go.applogic;

import java.util.HashSet;
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
     * @return ChainResult class that contains set of all chains and set of all liberties
     */
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
        pointHandler.addPoints(capturedStones, side);
        //System.out.println(pointHandler.whitePoints());
        //System.out.println(pointHandler.blackPoints());
        return true;
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
                    pointHandler.addPoints(1, board.returnCurrentState()[i][j].getOpposite());
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
