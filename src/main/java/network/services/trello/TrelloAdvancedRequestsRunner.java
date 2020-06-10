package network.services.trello;

import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloList;
import network.services.trello.entities.TrelloMemberInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TrelloAdvancedRequestsRunner extends TrelloApiWrapper {
    public TrelloAdvancedRequestsRunner(@NotNull OAuth1Tokens myCredentials) {
        super(myCredentials);
    }

    @NotNull
    public List<TrelloBoard> getAllTrelloBoards() throws IOException {
        ArrayList<TrelloBoard> boards = new ArrayList<>();
        TrelloMemberInfo info = getTrelloMembersInfo();
        List<String> ids = info.getUserBoardIds();
        for (String id : ids) {
            TrelloBoard board = getTrelloBoardById(id);
            assert board.getBoardId().equals(id);
            boards.add(board);
        }
        return boards;
    }

    @Nullable
    public TrelloBoard getBoardByName(String targetBoardName) throws IOException {
        List<TrelloBoard> trelloBoards = getAllTrelloBoards();
        for (TrelloBoard board : trelloBoards) {
            if (board.getBoardName().equals(targetBoardName)) {
                return board;
            }
        }
        return null;
    }

    @Nullable
    public TrelloList getListFromBoardByName(TrelloBoard board, String listName) throws IOException {
        if (board == null) {
            return null;
        }
        List<TrelloList> lists = getAllListsFromBoard(board);
        for (TrelloList list : lists) {
            if (list.getName().equals(listName)) {
                return list;
            }
        }
        return null;
    }
}
