package project.go.applogic;

public class Board {

    public Color[][] grid;
    private int boardSize=19;

    public Board(int boardSize){
        this.boardSize=boardSize;

        this.grid=new Color[boardSize][boardSize];
        for(int i=0;i<boardSize;i++){
            for(int j=0;j<boardSize;j++){
                grid[i][j]=Color.NONE;
            }
        }
    }

    public int getSize(){
        return boardSize;
    }

}
