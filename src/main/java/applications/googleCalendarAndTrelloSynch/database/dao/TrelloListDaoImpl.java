package applications.googleCalendarAndTrelloSynch.database.dao;

import network.services.trello.entities.TrelloList;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class TrelloListDaoImpl extends InDbWithJsonPresentationDao<TrelloList> {
    protected static final String TABLE_NAME = "TRELLO_LISTS";

    protected TrelloListDaoImpl(@NotNull String dataBaseName) {
        super(dataBaseName, TrelloList.class);
    }

    @NotNull
    public static TrelloListDaoImpl createTrelloListDao(@NotNull String databaseName) {
        TrelloListDaoImpl trelloListDao = new TrelloListDaoImpl(databaseName);
        try {
            trelloListDao.runUpdate(trelloListDao.getCreateTableRequest());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return trelloListDao;
    }

    protected String getCreateTableRequest() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "\n" +
                "(\n" +
                "    LIST_ID       TEXT PRIMARY KEY,\n" +
                "    BELONGS_TO_BOARD_ID TEXT,\n" +
                "    JSON_PRESENTATION     TEXT,\n" +
                "    IS_DEFAULT          BOOLEAN DEFAULT FALSE," +
                "    FOREIGN KEY(BELONGS_TO_BOARD_ID) REFERENCES " + TrelloBoardDaoImpl.TABLE_NAME + "(BOARD_ID)\n" +
                ");";
    }

    @Override
    protected @NotNull String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void add(TrelloList entity) {
        throw new UnsupportedOperationException("Not implemented yet");

    }

    @Override
    public void update(TrelloList entity) {
        runUpdateNoThrow("UPDATE " + getTableName() + "\n" +
                "SET IS_DEFAULT = " + entity.get("IS_DEFAULT") + ", JSON_PRESENTATION = '" + entity.toString() + "', BELONGS_TO_BOARD_ID='" +
                entity.getIdBoard() + "'\n" + "WHERE LIST_ID = '" + entity.getId() + "'");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected void appendToStringBuilder(Object entity, StringBuilder sb) {
        TrelloList trelloList = (TrelloList) entity;
        sb.append("('").append(trelloList.getId()).append("','").append(trelloList.getIdBoard()).append("','").append(trelloList.toString()).append("')");
    }

    @Override
    protected @NotNull String getTableColumnsForNewEntity() {
        return "(LIST_ID, BELONGS_TO_BOARD_ID, JSON_PRESENTATION)";
    }
}
