package applications.googleCalendarAndTrelloSynch.database;

import database.SqlSpecification;
import network.services.trello.entities.TrelloBoard;
import org.jetbrains.annotations.NotNull;

public class DefaultListFromBoardSpecification implements SqlSpecification {
    private final @NotNull TrelloBoard board;

    public DefaultListFromBoardSpecification(@NotNull TrelloBoard board) {
        this.board = board;
    }


    @Override
    public @NotNull String toSqlClauses() {
        return "IS_DEFAULT=TRUE AND BELONGS_TO_BOARD_ID='" + board.getBoardId() + "'";
    }
}
