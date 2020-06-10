package applications.googleCalendarAndTrelloSynch.database;

import database.SqlSpecification;
import org.jetbrains.annotations.NotNull;

public class TrelloListByBoardIdSpecification implements SqlSpecification {
    private final @NotNull String boardId;

    public TrelloListByBoardIdSpecification(@NotNull String boardId) {
        this.boardId = boardId;
    }

    @Override
    public @NotNull String toSqlClauses() {
        return "BELONGS_TO_BOARD_ID='" + boardId + "'";
    }
}
