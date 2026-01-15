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

    public void updateStatus(int x, int y, StoneStatus status) {
        if (x >= 0 && x < stoneStatusGrid.length && y >= 0 && y < stoneStatusGrid.length) {
            stoneStatusGrid[x][y] = status;
        }
    }

    public boolean equals(StoneStatusHolder other) {
        if (other == null) return false;
        StoneStatus[][] otherGrid = other.returnStoneStatus();
        for(int i=0;i<stoneStatusGrid.length;i++){
            for(int j=0;j<stoneStatusGrid[i].length;j++){
                if (stoneStatusGrid[i][j] != otherGrid[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
