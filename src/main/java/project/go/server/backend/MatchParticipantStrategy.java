package project.go.server.backend;

/**
 * Strategy interface for how a participant (Human or Bot) behaves in a match.
 */
public interface MatchParticipantStrategy {
    /**
     * Called every loop cycle of the match thread.
     * @param match The current match context.
     * @throws Exception
     */
    void handleTurn(Match match) throws Exception;

    /**
     * Determines if the participant is a bot.
     */
    boolean isBot();

    /**
     * Determine if the participant supports manual negotiation.
     */
    boolean supportsNegotiation();
}
