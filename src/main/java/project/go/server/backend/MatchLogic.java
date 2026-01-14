package project.go.server.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;

public class MatchLogic {
    private Scanner in;
    private PrintWriter out;
    private Match.ClientData client;
    private SharedMatchLogicState sharedState;
    private MatchRequestDispatcher dispatcher;

    public MatchLogic(Match.ClientData client, SharedMatchLogicState state) throws IOException {
        this.client = client;
        this.sharedState = state;

        this.dispatcher = new MatchRequestDispatcher(sharedState, client);
        this.in = new Scanner(client.getSocket().getInputStream());
        this.out = new PrintWriter(client.getSocket().getOutputStream(), true);

        // Notify player of their color
        sendPlayerTurn();
    }

    private void sendPlayerTurn() throws IOException {
        log(client.getClientId() + " got turn: " + client.getSide());
        out.println(JsonFmt.toJson(
            new GameResponse<GameResponse.PlayerTurn>(
                GameResponse.STATUS_OK,
                GameResponse.TYPE_PLAYER_TURN, 
                "Assigned player color", 
                new GameResponse.PlayerTurn(client.getSide()))));
    }

    private void log(String msg) {
        Logger.getInstance().log("MatchLogic-" + client.getMatchId(), msg);
    }

    /**
     * Handles input from a player client, it's non-blocking.
     * @return GameResponse indicating the result of the command.
     * @throws Exception Internal error processing the command.
     * @throws NoSuchElementException If the input is closed.
     */
    public void handleInput() throws Exception, NoSuchElementException {

        if (client.getSocket().isClosed()) {
            throw new NoSuchElementException("Socket is closed");
        }

        tryNotifyOpponent();

        // Non-blocking check for input
        if (client.getSocket().getInputStream().available() == 0) {
            return;
        }

        // Dispatch the command
        String line = in.nextLine();
        out.println(JsonFmt.toJson(
            dispatcher.dispatch(JsonFmt.fromJson(line, GameCommand.class))));
    }

    private void tryNotifyOpponent() throws JsonProcessingException {
        // Notify player of opponent's move if available
        String otherPlayerMove = sharedState.popEnemyMove(client.getSide());
        if (otherPlayerMove != null) {
            log("Notifying player " + client.data().getClientId() + " of opponent's move: " + otherPlayerMove);
            out.println(JsonFmt.toJson(new GameResponse<GameResponse.BoardUpdate>(
                GameResponse.STATUS_OK,
                GameResponse.TYPE_BOARD_UPDATE,
                "Opponent played a move",
                new GameResponse.BoardUpdate(otherPlayerMove)
            )));
        }
    }
}
