package network.services.trello.entities;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class TrelloList extends GenericJson {
    @Key("id")
    private String id;

    @Key("name")
    private String name;

    @Key("idBoard")
    private String idBoard;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdBoard() {
        return idBoard;
    }

    public void setIdBoard(String idBoard) {
        this.idBoard = idBoard;
    }
}
