package project.go.applogic;

import project.go.Config;

public class Board {

    private Color currentTurn;
    private Color[][] grid;
    private int boardSize=Config.DEFAULT_BOARD_SIZE;

    public Board(int boardSize){
        this.boardSize=boardSize;
        this.currentTurn=Color.BLACK;

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

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        if (currentTurn == Color.BLACK) {
            currentTurn = Color.WHITE;
        } else {
            currentTurn = Color.BLACK;
        }
    }

    public void setCurrentTurn(Color player){
        currentTurn = player;
    }
}
