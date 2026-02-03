package project.go.server.backend;

import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

public class HumanParticipantStrategy implements MatchParticipantStrategy {
    private final MatchLogic logic;
    private final Match.ClientData clientData;

    public HumanParticipantStrategy(Match.ClientData clientData, SharedMatchLogicState sharedState) throws IOException {
        this.clientData = clientData;
        this.logic = new MatchLogic(clientData, sharedState);
    }

    @Override
    public void handleTurn(Match match) throws Exception {
        try {
            logic.handleInput();
        } catch (NoSuchElementException e) {
            // Re-throw to handle disconnection in Match.run()
            throw e;
        }
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public boolean supportsNegotiation() {
        return true;
    }
}
