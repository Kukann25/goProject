package project.go.applogic;

public class MoveHandler{

    private Board board;

    public MoveHandler(Board board){
        this.board=board;
    }

    public boolean hasBreaths(int x, int y, Color side) {
        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        
        if (isValid(x-1, y)&&board.grid[x-1][y]!=opponent) return true;
        if (isValid(x+1, y)&&board.grid[x+1][y]!=opponent) return true;
        if (isValid(x, y-1)&&board.grid[x][y-1]!=opponent) return true;
        if (isValid(x, y+1)&&board.grid[x][y+1]!=opponent) return true;
        
        return false;
    }
    
    private boolean isValid(int x, int y) {
        if (x < 0 || x >= board.getSize() || y < 0 || y >= board.getSize()) {
            return false;
        }
        return true;
    }

    public boolean makeMove(int x, int y, Color side){
        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        if(isValid(x, y)){
            if(board.grid[x][y]==Color.NONE){
                if(hasBreaths(x, y, side)){
                    board.grid[x][y]=side;
                    if(!hasBreaths(x+1, y, opponent)&&isValid(x+1, y)){
                        board.grid[x+1][y]=Color.NONE;
                    }
                    if(!hasBreaths(x-1, y, opponent)&&isValid(x-1, y)){
                        board.grid[x-1][y]=Color.NONE;
                    }
                    if(!hasBreaths(x, y+1, opponent)&&isValid(x, y+1)){
                        board.grid[x][y+1]=Color.NONE;
                    }
                    if(!hasBreaths(x, y-1, opponent)&&isValid(x, y-1)){
                        board.grid[x][y-1]=Color.NONE;
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
