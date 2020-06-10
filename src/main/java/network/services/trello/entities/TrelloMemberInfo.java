package network.services.trello.entities;

import com.google.api.client.util.Key;

import java.util.List;

public class TrelloMemberInfo {
    @Key("idBoards")
    private List<String> userBoardIds;

    public List<String> getUserBoardIds() {
        return userBoardIds;
    }

    public void setUserBoardIds(List<String> userBoardIds) {
        this.userBoardIds = userBoardIds;
    }
}
