package applications.googleCalendarAndTrelloSynch.database.dao;

import applications.googleCalendarAndTrelloSynch.ApplicationConstants;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.Configuration;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.Calendar;
import database.DatabaseHandler;
import database.SqlSpecification;
import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloList;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationDaoImpl extends DatabaseHandler implements DefaultConfigurationDao {
    private final static String TABLE_NAME = "CONFIGURATION";
    private final static String CALENDAR_TYPE_NAME = "calendar";
    private final static String TRELLO_BOARD_TYPE_NAME = "board";
    private final static String TRELLO_LIST_TYPE_NAME = "list";
    private final static String DATE_BUTTON_TYPE_NAME = "dateButton";
    private final static Logger LOGGER = Logger.getLogger(ConfigurationDaoImpl.class);

    private ConfigurationDaoImpl(@NotNull String dataBaseName) {
        super(dataBaseName);
    }

    public static ConfigurationDaoImpl createConfigurationDaoImpl() {
        ConfigurationDaoImpl configurationDao = new ConfigurationDaoImpl(ApplicationConstants.DB_NAME);
        configurationDao.runUpdateNoThrow("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "\n" +
                "(\n" +
                "    SETTING_TYPE TEXT,\n" +
                "    SETTING_ID   TEXT\n" +
                ");\n");
        return configurationDao;
    }

    @Override
    public @NotNull List<Configuration> selectQuery(@Nullable SqlSpecification sqlSpecification) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected String toValues(Configuration entity) {
        StringBuilder sb = new StringBuilder();
        boolean isFirstGo = true;
        for (Calendar calendar : entity.getGoogleCalendars()) {
            if (!isFirstGo) {
                sb.append(",");
            } else {
                isFirstGo = false;
            }
            sb.append("(").append("'").append(CALENDAR_TYPE_NAME).append("',").append("'").append(calendar.getId()).append("')");
        }
        sb.append(",(").append("'").append(TRELLO_BOARD_TYPE_NAME).append("',").append("'").append(entity.getTrelloBoard().getBoardId()).append("')");
        sb.append(",(").append("'").append(TRELLO_LIST_TYPE_NAME).append("',").append("'").append(entity.getTrelloList().getId()).append("')");
        sb.append(",(").append("'").append(DATE_BUTTON_TYPE_NAME).append("',").append("'").append(entity.getDateButtonClicked().toInt()).append("')");
        return sb.toString();
    }

    @Override
    public void add(Configuration entity) {
        runUpdateNoThrow("INSERT INTO " + TABLE_NAME + " (SETTING_TYPE, SETTING_ID) VALUES " + toValues(entity));
    }

    @Override
    public void update(Configuration entity) {
        deleteDefaultConfigurations();
        add(entity);
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Operation is not supported, use deleteDefaultConfigurations instead.");
    }

    @Override
    public void deleteDefaultConfigurations() {
        runUpdateNoThrow("DELETE FROM " + TABLE_NAME);
    }

    @Override
    public Configuration getCurrentDefaultConfiguration() {
        String qCalendars = getQuery1(GoogleCalendarDaoImpl.TABLE_NAME, CALENDAR_TYPE_NAME, ".CALENDAR_ID");
        List<Calendar> calendars = extractJsonData(qCalendars, Calendar.class);

        String qTrelloBoard = getQuery1(TrelloBoardDaoImpl.TABLE_NAME, TRELLO_BOARD_TYPE_NAME, ".BOARD_ID");
        List<TrelloBoard> boards = extractJsonData(qTrelloBoard, TrelloBoard.class);

        String qTrelloLists = getQueryLists();
        List<TrelloList> lists = extractJsonData(qTrelloLists, TrelloList.class);

        String qButton = getQueryButton();
        List<Configuration.DateButton> dateButtons = new ArrayList<>();
        runQueryNoThrow(qButton, resultSet -> {
            try {
                while (resultSet.next()) {
                    String idStr = resultSet.getString(1);
                    dateButtons.add(Configuration.DateButton.fromInteger(Integer.parseInt(idStr)));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        if (boards.size() > 1) {
            LOGGER.warn("Found " + boards.size() + "default boards, but the number is supposed to be 1.");
        }
        if (lists.size() > 1) {
            LOGGER.warn("Found " + boards.size() + "default lists, but the number is supposed to be 1.");
        }
        if (dateButtons.size() > 1) {
            LOGGER.warn("Found " + boards.size() + "default buttons, but the number is supposed to be 1.");
        }
        if (calendars.isEmpty() || boards.isEmpty() || lists.isEmpty() || dateButtons.isEmpty()) {
            return null;
        }
        return new Configuration.ConfigurationBuilder()
                .setGoogleCalendars(calendars)
                .setTrelloBoard(boards.get(0))
                .setTrelloList(lists.get(0))
                .setDateButtonClicked(dateButtons.get(0)).createConfiguration();
    }

    @NotNull
    protected String getQueryButton() {
        return "SELECT SETTING_ID FROM " + TABLE_NAME + " WHERE SETTING_TYPE='" + DATE_BUTTON_TYPE_NAME + "'";
    }

    @NotNull
    protected String getQueryLists() {
        return "SELECT " + TrelloListDaoImpl.TABLE_NAME + ".JSON_PRESENTATION FROM " + TABLE_NAME +
                " INNER JOIN " + TrelloListDaoImpl.TABLE_NAME +
                " ON " + TABLE_NAME + ".SETTING_TYPE='" + TRELLO_LIST_TYPE_NAME + "' AND " +
                TABLE_NAME + ".SETTING_ID=" + TrelloListDaoImpl.TABLE_NAME + ".LIST_ID " +
                "INNER JOIN " + TrelloBoardDaoImpl.TABLE_NAME +
                " ON " + TrelloListDaoImpl.TABLE_NAME + ".BELONGS_TO_BOARD_ID=" + TrelloBoardDaoImpl.TABLE_NAME + ".BOARD_ID";
    }

    @NotNull
    protected String getQuery1(String tableName, String calendarTypeName, String s) {
        return "SELECT " + tableName + ".JSON_PRESENTATION FROM " + TABLE_NAME +
                " INNER JOIN " + tableName +
                " ON " + TABLE_NAME + ".SETTING_TYPE='" + calendarTypeName + "' AND " +
                TABLE_NAME + ".SETTING_ID=" + tableName + s;
    }

    private <T extends GenericJson> List<T> extractJsonData(String q, Class<T> dataType) {
        List<T> entities = new ArrayList<>();
        JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
        runQueryNoThrow(q, resultSet -> {
            try {
                while (resultSet.next()) {
                    String json = resultSet.getString(1);
                    entities.add(jacksonFactory.fromString(json, dataType));
                }
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return entities;
    }
}
