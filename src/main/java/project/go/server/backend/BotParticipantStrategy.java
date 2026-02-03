package project.go.server.backend;

import project.go.applogic.Color;
import project.go.applogic.MoveConverter;
import project.go.applogic.SingleMove;
import project.go.bot.Engine;

public class BotParticipantStrategy implements MatchParticipantStrategy {
    private final Engine engine;
    private final Color botColor;
    private final SharedMatchLogicState sharedState;
    // Simple delay simulation or check
    private long lastMoveTime = 0;

    public BotParticipantStrategy(Color botColor, SharedMatchLogicState sharedState) {
        this.engine = new Engine();
        this.botColor = botColor;
        this.sharedState = sharedState;
    }

    @Override
    public void handleTurn(Match match) throws Exception {
        // Check if it is bot's turn
        if (sharedState.getMatchState().isOngoing() && 
            sharedState.getMoveHandler().getBoard().getCurrentTurn() == botColor) {
            
            // Add a small delay for realism
            if (System.currentTimeMillis() - lastMoveTime < 1000) return;

            // Generate move using Engine
            SingleMove bestMove = engine.returnBestMove(sharedState.getMoveHandler(), botColor);
            
            if (bestMove == null) {
                // Engine decided to pass
                sharedState.passTurn(botColor);
                Logger.getInstance().log("BotStrategy", "Bot passed.");
            } else {
                String moveStr = MoveConverter.toJSON(bestMove);
                // Directly modify shared state as if a command came in
                try {
                    sharedState.makeMove(moveStr, botColor);
                    Logger.getInstance().log("BotStrategy", "Bot played: " + moveStr);
                } catch (IllegalArgumentException e) {
                    // Should not happen if Engine uses generateLegalMoves, but safe fallback
                    Logger.getInstance().log("BotStrategy", "Bot tried illegal: " + e.getMessage());
                    sharedState.passTurn(botColor);
                }
            }
            lastMoveTime = System.currentTimeMillis();
        } else {
             // If opponent passed and we are in negotiation logic? 
             // Bots always agree to end game if both passed, or standard logic handles it.
             lastMoveTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public boolean supportsNegotiation() {
        // Bots currently do not negotiate.
        // They will rely on auto-scoring or simply agreeing to whatever the server calculates,
        // but for now we disable negotiation in UI.
        return false;
    }
}
