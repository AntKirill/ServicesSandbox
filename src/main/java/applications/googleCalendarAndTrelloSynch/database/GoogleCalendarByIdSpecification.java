package applications.googleCalendarAndTrelloSynch.database;

import database.SqlSpecification;
import org.jetbrains.annotations.NotNull;

public class GoogleCalendarByIdSpecification implements SqlSpecification {
    private final String calendarId;

    public GoogleCalendarByIdSpecification(String calendarId) {
        this.calendarId = calendarId;
    }

    @Override
    public @NotNull String toSqlClauses() {
        return "CALENDAR_ID='" + calendarId + "'";
    }
}
