package applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter;

import applications.googleCalendarAndTrelloSynch.database.GoogleCalendarByIdSpecification;
import applications.googleCalendarAndTrelloSynch.database.dao.CompactDao;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import network.services.google.DateTimeConverter;
import network.services.google.calendar.GoogleCalendarApiRequestsRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

public class GoogleCalendarEventsShifterControllerImpl implements GoogleCalendarEventsShifterController {
    protected static final String CALENDAR_ID = "calendarId";
    protected static final String SELECTED_BUTTON = "selectedButton";
    protected static final String MINUTES_SHIFT = "minutesShift";
    protected static final String HOURS_SHIFT = "hoursShift";

    private final @NotNull GoogleCalendarApiRequestsRunner myRequestsRunner;
    private final @NotNull CompactDao<Calendar> googleCalendarsDao;
    private final @NotNull Preferences preferences;

    public GoogleCalendarEventsShifterControllerImpl(@NotNull GoogleCalendarApiRequestsRunner myRequestsRunner,
                                                     @NotNull CompactDao<Calendar> googleCalendarsDao,
                                                     @NotNull Preferences preferences) {
        this.myRequestsRunner = myRequestsRunner;
        this.googleCalendarsDao = googleCalendarsDao;
        this.preferences = preferences;
    }

    @Override
    public void shiftGoogleCalendarEvents(@NotNull GoogleCalendarShifterConfiguration configuration) throws IOException {
        final Calendar calendar = configuration.getCalendar();
        final DateTimeConverter dateTimeConverterForTimeZone =
                myRequestsRunner.getDateTimeConverterForTimeZone(calendar.getTimeZone());
        final List<Event> usualEventsOnDateFromCalendar =
                myRequestsRunner.getUsualEventsOnDateFromCalendarLaterThanTime(configuration.getDateTime(),
                        calendar);

        final HashMap<String, Event> eventIdToPatch = new HashMap<>();
        long shiftMinutes = configuration.getFullShiftInMinutes();
        for (Event event : usualEventsOnDateFromCalendar) {
            final Event eventPath = new Event();
            final DateTime shiftedDateTimeStart = shiftDateTime(dateTimeConverterForTimeZone, shiftMinutes,
                    event.getStart().getDateTime());
            final DateTime shiftedDateTimeEnd = shiftDateTime(dateTimeConverterForTimeZone, shiftMinutes,
                    event.getEnd().getDateTime());
            eventPath.setStart(new EventDateTime().setDateTime(shiftedDateTimeStart));
            eventPath.setEnd(new EventDateTime().setDateTime(shiftedDateTimeEnd));
            eventIdToPatch.put(event.getId(), eventPath);
        }
        for (var entry : eventIdToPatch.entrySet()) {
            myRequestsRunner.patchEventInCalendar(calendar, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void updateLocalStoredInformation() throws IOException {
        final List<Calendar> allCalendars = myRequestsRunner.getAllCalendars();
        googleCalendarsDao.clearAll();
        googleCalendarsDao.addAll(allCalendars);
    }

    @Override
    public @NotNull List<Calendar> getLocalStoredGoogleCalendars() {
        return googleCalendarsDao.selectAll();
    }

    @Override
    public void updateDefaultConfiguration(@NotNull GoogleCalendarShifterConfiguration configuration) {
        preferences.put(CALENDAR_ID, configuration.getCalendar().getId());
        preferences.putInt(SELECTED_BUTTON, configuration.getSelectedRadioButton().toInt());
        preferences.putInt(MINUTES_SHIFT, configuration.getMinutesShift());
        preferences.putInt(HOURS_SHIFT, configuration.getHoursShift());
    }

    @Override
    public @Nullable GoogleCalendarShifterConfiguration getDefaultConfiguration() {
        GoogleCalendarShifterConfiguration.GoogleCalendarShifterConfigurationBuilder builder =
                new GoogleCalendarShifterConfiguration.GoogleCalendarShifterConfigurationBuilder();
        String calendarId = preferences.get(CALENDAR_ID, null);
        int selectedButtonInt = preferences.getInt(SELECTED_BUTTON, 0);
        GoogleCalendarShifterConfiguration.SelectedRadioButton selectedButton =
                GoogleCalendarShifterConfiguration.SelectedRadioButton.fromInteger(selectedButtonInt);
        int minutesShift = preferences.getInt(MINUTES_SHIFT, 0);
        int hoursShift = preferences.getInt(HOURS_SHIFT, 0);
        if (calendarId == null) {
            return null;
        }
        final Calendar calendar =
                googleCalendarsDao.selectQuery(new GoogleCalendarByIdSpecification(calendarId)).get(0);
        return builder.setCalendar(calendar)
                .setMinutesShift((short) minutesShift)
                .setDateTime(LocalDateTime.now())
                .setHoursShift((short) hoursShift)
                .setSelectedRadioButton(selectedButton)
                .createGoogleCalendarShifterConfiguration();
    }

    @NotNull
    private DateTime shiftDateTime(DateTimeConverter dateTimeConverterForTimeZone, long shiftMinutes,
                                   DateTime dateTime) {
        final LocalDateTime localDateTimeStart =
                dateTimeConverterForTimeZone.toLocalDateTime(dateTime);
        return dateTimeConverterForTimeZone.toGoogleDateTime(localDateTimeStart.plusMinutes(shiftMinutes));
    }

    @Override
    public @NotNull String getApplicationName() {
        return GoogleCalendarEventsShifterUtils.APPLICATION_NAME;
    }
}
