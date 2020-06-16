package applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter;

import applications.Application;
import com.google.api.services.calendar.model.Calendar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public interface GoogleCalendarEventsShifterController extends Application.ApplicationController {
    void shiftGoogleCalendarEvents(@NotNull GoogleCalendarShifterConfiguration configuration) throws IOException;

    void updateLocalStoredInformation() throws IOException;

    @NotNull
    List<Calendar> getLocalStoredGoogleCalendars();

    void updateDefaultConfiguration(@NotNull GoogleCalendarShifterConfiguration configuration);

    @Nullable
    GoogleCalendarShifterConfiguration getDefaultConfiguration();
}
