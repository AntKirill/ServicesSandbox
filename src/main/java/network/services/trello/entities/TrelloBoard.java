package network.services.trello.entities;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class TrelloBoard extends GenericJson {
    @Key("id")
    private String boardId;

    @Key("name")
    private String boardName;

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }
}
