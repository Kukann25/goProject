package project.go.applogic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import project.go.Config;

public class MoveHandler{

    private Board board;

    private static final int[] DX = {-1, 1, 0, 0};
    private static final int[] DY = {0, 0, -1, 1};

    private SingleMove koPoint = null;

    public boolean lastMoveWasPass = false;
    public boolean gameStopped = false;

    private EnumMap<Color, Integer> prisoners;
    public Map<Set<SingleMove>, GroupStatus> groupStatus;


    public MoveHandler(Board board){
        this.board=board;
        prisoners = new EnumMap<>(Color.class);
        prisoners.put(Color.BLACK, 0);
        prisoners.put(Color.WHITE, 0);
        groupStatus = new HashMap<>(); 
    }
    
    private boolean isValid(int x, int y) {
        if (x < 0 || x >= board.getSize() || y < 0 || y >= board.getSize()) {
            return false;
        }
        return true;
    }

    private List<Set<SingleMove>> findAllGroups(Color color) {
        int size = Config.DEFAULT_BOARD_SIZE;
        boolean[][] visited = new boolean[size][size];
        List<Set<SingleMove>> groups = new ArrayList<>();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (!visited[x][y] && board.returnCurrentState()[x][y] == color) {
                    ChainResult cr = floodFill(x, y);
                    groups.add(cr.chain);
                    for (SingleMove p : cr.chain)
                        visited[p.getX()][p.getY()] = true;
                }
            }
        }
        return groups;
}


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

    public boolean resolveAfterMove(int x, int y, Color side) {

        Color opponent = (side == Color.BLACK) ? Color.WHITE : Color.BLACK;
        Color[][] boardState = board.returnCurrentState();

        int capturedStones = 0;
        SingleMove potentialKo = null;

        Set<SingleMove> checked = new HashSet<>();
    
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
                    potentialKo = p;
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

        if (capturedStones == 1) {
            koPoint = potentialKo;
        } else {
            koPoint = null;
        }
    
        return true;
    }

    public void updateGroupStatus() {
        groupStatus.clear();
        for (Color color : Color.values()) {
            if (color == Color.NONE) continue;
            List<Set<SingleMove>> groups = findAllGroups(color);
            for (Set<SingleMove> group : groups) {
                groupStatus.put(group, GroupStatus.UNKNOWN);
            }
        }
    }

    public void removeDeadGroups() {
        Color[][] boardState = board.returnCurrentState();

        for (Map.Entry<Set<SingleMove>, GroupStatus> entry : groupStatus.entrySet()) {
            Set<SingleMove> group = entry.getKey();
            GroupStatus status = entry.getValue();

            if (status != GroupStatus.DEAD) continue;

            SingleMove any = group.iterator().next();
            Color deadColor = boardState[any.getX()][any.getY()];

            Color captor = (deadColor == Color.BLACK) ? Color.WHITE : Color.BLACK;

            for (SingleMove p : group) {
                boardState[p.getX()][p.getY()] = Color.NONE;
                prisoners.put(captor, prisoners.get(captor) + 1);
            }
        }
    }

    public int getPrisoners(Color color) {
        return prisoners.get(color);
    }

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

        if (koPoint != null &&
            koPoint.getX() == x &&
            koPoint.getY() == y) {
            return false;
        }

        updateGroupStatus();
        removeDeadGroups();

        board.switchTurn();
        return true;
    }

    public void pass(Color side) {
        if (side != board.getCurrentTurn()) return;
    
        if (lastMoveWasPass) {
            gameStopped = true;
        }
    
        lastMoveWasPass = true;
        board.switchTurn();
    }

    public void resumeGame(Color requestingPlayer) {
        gameStopped = false;
        lastMoveWasPass = false;
        board.setCurrentTurn(requestingPlayer);
    }
    
    
}
