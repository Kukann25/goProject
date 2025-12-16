package project.go.applogic;

import project.go.Config;

public class Board {

    private Color[][] grid;
    private int boardSize=Config.DEFAULT_BOARD_SIZE;

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

    public Color[][] returnCurrentState(){
        return grid;
    }

}
