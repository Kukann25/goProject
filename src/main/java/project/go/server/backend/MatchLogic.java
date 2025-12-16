package project.go.server.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.PlayerTurn;

public class MatchLogic {
    private Scanner in;
    private PrintWriter out;
    private Match.Data client;
    private SharedMatchLogicState sharedState;
    private Match.Color color;

    public MatchLogic(Match.Data client, Match.Color color, SharedMatchLogicState state) throws IOException {
        this.client = client;
        this.in = new Scanner(client.getSocket().getInputStream());
        this.out = new PrintWriter(client.getSocket().getOutputStream(), true);
        this.color = color;
        this.sharedState = state;

        // Notify player of their color
        out.println(JsonFmt.toJson(new PlayerTurn(color == Match.Color.BLACK)));
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

        // Notify player of opponent's move if available
        String otherPlayerMove = sharedState.popEnemyMove(color);
        if (otherPlayerMove != null) {
            log("Notifying player " + client.data().getClientId() + " of opponent's move: " + otherPlayerMove);
            out.println(JsonFmt.toJson(new GameCommand.PayloadMakeMove(otherPlayerMove)));
        }

        if (!in.hasNextLine()) {
            return;
        }

        // Should be either move or resign command
        String line = in.nextLine();
        GameCommand<?> 
            command = JsonFmt.fromJson(line, GameCommand.class);
        Object payload = command.getPayload();

        if (payload instanceof GameCommand.PayloadMakeMove) {
            out.println(JsonFmt.toJson(processMove((GameCommand.PayloadMakeMove) payload)));
        }

        log("Unknown command from player " + client.data().getClientId() + ": " + command.getCommand());
        out.println(JsonFmt.toJson(new GameResponse(GameResponse.STATUS_ERROR, GameResponse.MESSAGE_UNKNOWN_COMMAND)));
    }

    /**
     * Processes a move command from a player.
     * @param client
     * @param payload
     * @return GameResponse indicating success or failure of the move.
     */
    private GameResponse processMove(GameCommand.PayloadMakeMove payload) {
        try {
            // Validate and apply the move using shared state
            sharedState.setMove(payload.getMove(), color);
            log("Player " + client.data().getClientId() + " played move: " + payload.getMove());
            return new GameResponse(GameResponse.STATUS_OK, GameResponse.MESSAGE_MOVE_OK);
        }  catch (IllegalArgumentException e) {
            log("Illegal move from player " + client.data().getClientId() + ": " + e.getMessage());
            return new GameResponse(GameResponse.STATUS_ERROR, GameResponse.MESSAGE_INVALID_MOVE);
        }
    }
}
