package applications.googleCalendarAndTrelloSynch.database.dao;

import com.google.api.services.calendar.model.Calendar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoogleCalendarDaoImpl extends InDbWithJsonPresentationDao<Calendar> {

    protected final static String TABLE_NAME = "GOOGLE_CALENDAR";

    protected GoogleCalendarDaoImpl(@NotNull String databaseName) {
        super(databaseName, Calendar.class);
    }

    public static GoogleCalendarDaoImpl createGoogleCalendarDao(@NotNull String databaseName) {
        GoogleCalendarDaoImpl googleCalendarDao = new GoogleCalendarDaoImpl(databaseName);
        googleCalendarDao.runUpdateNoThrow("CREATE TABLE IF NOT EXISTS GOOGLE_CALENDAR (\n" +
                "    CALENDAR_ID       TEXT PRIMARY KEY,\n" +
                "    CALENDAR_NAME     TEXT,\n" +
                "    JSON_PRESENTATION TEXT,\n" +
                "    IS_DEFAULT        BOOLEAN default false" +
                ")");
        return googleCalendarDao;
    }

    @Nullable
    public Calendar get(String id) {
        List<Calendar> calendars = selectQuery(() -> "CALENDAR_ID='" + id + "'");
        if (calendars.isEmpty()) {
            return null;
        }
        return calendars.get(0);
    }

    @NotNull
    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void add(Calendar entity) {
        final String jsonPresentation = entity.toString();
        runUpdateNoThrow("INSERT OR REPLACE INTO GOOGLE_CALENDAR (CALENDAR_ID, CALENDAR_NAME, JSON_PRESENTATION)\n" +
                "VALUES ('" + entity.getId() + "', '" + entity.getSummary() + "', '" + jsonPresentation + "')");
    }

    @Override
    public void update(Calendar entity) {
        Calendar oldCalendar = get(entity.getId());
        if (oldCalendar == null) {
            return;
        }
        runUpdateNoThrow("UPDATE GOOGLE_CALENDAR\n" +
                "SET CALENDAR_NAME = '" + entity.getSummary() + "', JSON_PRESENTATION = '" + entity.toString() + "', IS_DEFAULT= " + entity.get("IS_DEFAULT") + "\n" +
                "WHERE CALENDAR_ID = '" + entity.getId() + "'");
    }

    @Override
    public void delete(String id) {
        runUpdateNoThrow("DELETE FROM GOOGLE_CALENDAR WHERE CALENDAR_ID = '" + id + "'");
    }

    @Override
    protected void appendToStringBuilder(Object entity, StringBuilder sb) {
        Calendar calendar = (Calendar) entity;
        sb.append("('").append(calendar.getId()).append("','").append(calendar.getSummary()).append("','").append(calendar.toString()).append("')");
    }

    @Override
    protected @NotNull String getTableColumnsForNewEntity() {
        return "(CALENDAR_ID, CALENDAR_NAME, JSON_PRESENTATION)";
    }
}
