package project.go.server.common.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * JSON representation of a player's turn
 * Returned on game start to indicate which side the player is on
 * Usage:
 * 
 * JsonFmt.fromJson(jsonString, PlayerTurn.class);
 */
public class PlayerTurn {
    private String side;

    /**
     * Initializes PlayerTurn with the given Color enum.
     */
    public PlayerTurn(boolean isBlack) {
        this.side = isBlack ? "BLACK" : "WHITE";
    }

    public PlayerTurn() {}

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    @JsonIgnore
    boolean isBlack() {
        return this.side.equals("BLACK");
    }

    /**
     * Converts the side string to a Color enum.
     * @return Corresponding Color enum value.
     */
    // public Color asColor() {
    //     switch(this.side) {
    //         case "BLACK":
    //             return Color.BLACK;
    //         case "WHITE":
    //             return Color.WHITE;
    //         default:
    //             return Color.NONE;
    //     }
    // }
}
