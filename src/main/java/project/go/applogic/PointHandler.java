package project.go.applogic;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class PointHandler extends Handler{
    private int whitePoints=0;
    private int blackPoints=0;

    public PointHandler(Board board){
        this.board=board;
    }

    public void addPoints(int points, Color side){
        if(side==Color.WHITE){
            whitePoints+=points;
        }
        else if(side==Color.BLACK){
            blackPoints+=points;
        }
    }

    public int whitePoints(){
        return whitePoints;
    }

    public int blackPoints(){
        return blackPoints;
    }

        /**
     * FloodFill function to calculate points at the end of the game
     * @param startX
     * @param startY
     * @param side
     * @return number of points for territory
     */
    private int territoryFloodFill(int startX, int startY, Color side){
        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        int result = 0;
        Set<SingleMove> visited = new HashSet<>();
        Stack<SingleMove> stack = new Stack<>();

        stack.push(new SingleMove(startX, startY));

        while (!stack.isEmpty()) {
            SingleMove p = stack.pop();

            if (visited.contains(p)) continue;
            visited.add(p);
            if(board.returnCurrentState()[p.getX()][p.getY()]==Color.NONE){
                result++;
            }
            for (int i = 0; i < 4; i++) {
                int nx = p.getX() + DX[i];
                int ny = p.getY() + DY[i];

                if (!isValid(nx, ny))
                    continue;

                if(board.returnCurrentState()[nx][ny]==opponent){
                    return 0;
                }
                else if(board.returnCurrentState()[nx][ny]==Color.NONE){
                    stack.push(new SingleMove(nx, ny));
                }
            }
        }
        return result;
    }

    /**
     * Function that calculates territory points for both players and adds them to scores
     */
    public void calculateTerritoryPoints() {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                visited[i][j] = false;
            }
        }
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.returnCurrentState()[x][y] == Color.NONE && !visited[x][y]) {
                    int whiteTerritory = territoryFloodFill(x, y, Color.WHITE);
                    
                    if (whiteTerritory > 0) {
                        whitePoints += whiteTerritory;
                        markVisitedTerritory(x, y, visited);
                    } else {
                        int blackTerritory = territoryFloodFill(x, y, Color.BLACK);
                        
                        if (blackTerritory > 0) {
                            blackPoints += blackTerritory;
                            markVisitedTerritory(x, y, visited);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper function to mark territory as visited
     * @param startX
     * @param startY
     * @param visited visited array
     * @param side owner of the territory
     */
    private void markVisitedTerritory(int startX, int startY, boolean[][] visited) {
        Stack<SingleMove> stack = new Stack<>();
        
        stack.push(new SingleMove(startX, startY));
        
        while (!stack.isEmpty()) {
            SingleMove p = stack.pop();
            
            if (visited[p.getX()][p.getY()]) continue;
            visited[p.getX()][p.getY()] = true;
            
            for (int i = 0; i < 4; i++) {
                int nx = p.getX() + DX[i];
                int ny = p.getY() + DY[i];
                
                if (!isValid(nx, ny)) continue;
                
                if (board.returnCurrentState()[nx][ny] == Color.NONE && !visited[nx][ny]) {
                    stack.push(new SingleMove(nx, ny));
                }
            }
        }
    }
}
