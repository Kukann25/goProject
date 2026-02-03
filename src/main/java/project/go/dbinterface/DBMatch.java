package project.go.dbinterface;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "matches")
public class DBMatch {

    @Id
    private String id;
    
    private String playerBlack;
    private String playerWhite;
    private List<DBMove> moves;

    public DBMatch(){}

    public String getId() {
        return id;
    }

    public String getPlayerBlack() {
        return playerBlack;
    }

    public void setPlayerBlack(String playerBlack) {
        this.playerBlack = playerBlack;
    }

    public String getPlayerWhite() {
        return playerWhite;
    }

    public void setPlayerWhite(String playerWhite) {
        this.playerWhite = playerWhite;
    }

    public List<DBMove> getMoves(){
        return moves;
    }

    public void setMoves(List<DBMove> moves){
        this.moves=moves;
    }
}