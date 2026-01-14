package project.go.applogic;

import project.go.Config;

public class StoneStatusHolder {

    private StoneStatus[][] stoneStatusGrid;

    public StoneStatusHolder(Board board){
        int size=Config.DEFAULT_BOARD_SIZE;
        this.stoneStatusGrid = new StoneStatus[size][size];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(board.returnCurrentState()[i][j]!=Color.NONE){
                    stoneStatusGrid[i][j]=StoneStatus.UNKNOWN;
                }
            }
        }
    }

    public StoneStatus[][] returnStoneStatus(){
        return stoneStatusGrid;
    }
}
