package applications.googleCalendarAndTrelloSynch.database.dao;

import network.services.trello.entities.TrelloBoard;
import org.jetbrains.annotations.NotNull;

public class TrelloBoardDaoImpl extends InDbWithJsonPresentationDao<TrelloBoard> {

    protected final static String TABLE_NAME = "TRELLO_BOARDS";

    protected TrelloBoardDaoImpl(@NotNull String dataBaseName) {
        super(dataBaseName, TrelloBoard.class);
    }

    @NotNull
    public static TrelloBoardDaoImpl createTrelloBoardDao(@NotNull String databaseName) {
        TrelloBoardDaoImpl trelloBoardDaoImpl = new TrelloBoardDaoImpl(databaseName);
        trelloBoardDaoImpl.runUpdateNoThrow(trelloBoardDaoImpl.getCreateTableRequest());
        return trelloBoardDaoImpl;
    }

    protected String getCreateTableRequest() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "\n" +
                "(\n" +
                "    BOARD_ID       TEXT PRIMARY KEY,\n" +
                "    JSON_PRESENTATION     TEXT,\n" +
                "    IS_DEFAULT          BOOLEAN DEFAULT FALSE" +
                ")";
    }

    @Override
    public void add(TrelloBoard entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(TrelloBoard entity) {
        runUpdateNoThrow("UPDATE " + getTableName() + "\n" +
                "SET IS_DEFAULT = " + entity.get("IS_DEFAULT") + ", JSON_PRESENTATION = '" + entity.toString() + "'\n" +
                "WHERE BOARD_ID = '" + entity.getBoardId() + "'");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected void appendToStringBuilder(Object entity, StringBuilder sb) {
        TrelloBoard trelloBoard = (TrelloBoard) entity;
        sb.append("('").append(trelloBoard.getBoardId()).append("','").append(trelloBoard.toString()).append("')");
    }

    @Override
    protected @NotNull String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected @NotNull String getTableColumnsForNewEntity() {
        return "(BOARD_ID, JSON_PRESENTATION)";
    }
}
