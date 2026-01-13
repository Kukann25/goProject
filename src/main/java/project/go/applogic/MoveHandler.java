package project.go.applogic;

public class MoveHandler{

    private Board board;

    public MoveHandler(Board board){
        this.board=board;
    }

    public boolean hasBreaths(int x, int y, Color side) {
        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        
        if (isValid(x-1, y)&&board.returnCurrentState()[x-1][y]!=opponent) return true;
        if (isValid(x+1, y)&&board.returnCurrentState()[x+1][y]!=opponent) return true;
        if (isValid(x, y-1)&&board.returnCurrentState()[x][y-1]!=opponent) return true;
        if (isValid(x, y+1)&&board.returnCurrentState()[x][y+1]!=opponent) return true;
        
        return false;
    }
    
    private boolean isValid(int x, int y) {
        if (x < 0 || x >= board.getSize() || y < 0 || y >= board.getSize()) {
            return false;
        }
        return true;
    }

    public boolean makeMove(SingleMove singleMove, Color side){
        if (side != board.getCurrentTurn()) { return false; }

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        int x=singleMove.getX();
        int y=singleMove.getY();
        if(isValid(x, y)){
            if(board.returnCurrentState()[x][y]==Color.NONE){
                if(hasBreaths(x, y, side)){
                    board.returnCurrentState()[x][y]=side;
                    if(isValid(x+1, y)&&!hasBreaths(x+1, y, opponent)&&board.returnCurrentState()[x+1][y]==opponent){
                        board.returnCurrentState()[x+1][y]=Color.NONE;
                    }
                    if(isValid(x-1, y)&&!hasBreaths(x-1, y, opponent)&&board.returnCurrentState()[x-1][y]==opponent){
                        board.returnCurrentState()[x-1][y]=Color.NONE;
                    }
                    if(isValid(x, y+1)&&!hasBreaths(x, y+1, opponent)&&board.returnCurrentState()[x][y+1]==opponent){
                        board.returnCurrentState()[x][y+1]=Color.NONE;
                    }
                    if(isValid(x, y-1)&&!hasBreaths(x, y-1, opponent)&&board.returnCurrentState()[x][y-1]==opponent){
                        board.returnCurrentState()[x][y-1]=Color.NONE;
                    }
                    board.switchTurn();
                    return true;
                }
            }
        }
        return false;
    }
}
