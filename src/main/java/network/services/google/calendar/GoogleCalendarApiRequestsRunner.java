package network.services.google.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import network.services.ApiRequestsRunner;
import network.services.google.DateTimeConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleCalendarApiRequestsRunner implements ApiRequestsRunner {
    private static final String PRIMARY_CALENDAR_MARK = "primary";
    private final @NotNull Calendar myService;

    public GoogleCalendarApiRequestsRunner(@NotNull Calendar myService) {
        this.myService = myService;
    }

    @NotNull
    public List<com.google.api.services.calendar.model.CalendarListEntry> getAllCalendarsEntries() throws IOException {
        String pageToken = null;
        final List<com.google.api.services.calendar.model.CalendarListEntry> calendars = new ArrayList<>();
        do {
            CalendarList calendarList = myService.calendarList().list().setPageToken(pageToken).execute();
            calendars.addAll(calendarList.getItems());
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        return calendars;
    }

    @NotNull
    public List<com.google.api.services.calendar.model.Calendar> getAllCalendars() throws IOException {
        final ArrayList<com.google.api.services.calendar.model.Calendar> calendars = new ArrayList<>();
        final List<CalendarListEntry> calendarListEntries = getAllCalendarsEntries();
        for (CalendarListEntry calendarListEntry : calendarListEntries) {
            calendars.add(getCalendarById(calendarListEntry.getId()));
        }
        return calendars;
    }

    @NotNull
    public List<com.google.api.services.calendar.model.Calendar> getCalendarsByNames(@NotNull List<String> names) throws IOException {
        final List<CalendarListEntry> calendarsList = getAllCalendarsEntries();
        final HashSet<String> nestedNamesSet = new HashSet<>(names);
        boolean isPrimaryRequired = nestedNamesSet.contains(PRIMARY_CALENDAR_MARK);
        boolean isPrimaryAdded = false;
        List<com.google.api.services.calendar.model.Calendar> calendars = new ArrayList<>();
        for (CalendarListEntry entry : calendarsList) {
            final String entrySummary = entry.getSummary();
            if (nestedNamesSet.contains(entrySummary)) {
                if (isPrimaryRequired && entrySummary.equals(PRIMARY_CALENDAR_MARK)) {
                    isPrimaryAdded = true;
                }
                final com.google.api.services.calendar.model.Calendar calendar = getCalendarById(entry.getId());
                calendars.add(calendar);
            }
            if (isPrimaryRequired && !isPrimaryAdded && entry.isPrimary()) {
                calendars.add(getCalendarById(entry.getId()));
                isPrimaryAdded = true;
            }
        }

        return calendars;
    }

    @NotNull
    public com.google.api.services.calendar.model.Calendar getCalendarById(@NotNull String id) throws IOException {
        return myService.calendars().get(id).execute();
    }

    @NotNull
    public List<Event> getAlldayEvents(@NotNull LocalDate date,
                                       @NotNull List<com.google.api.services.calendar.model.Calendar> calendars) throws IOException {
        final LocalDateTime startDateTime = date.atStartOfDay();
        final LocalDateTime finishDateTime = startDateTime.plus(Duration.ofHours(1));
        final List<Event> events = new ArrayList<>();
        for (com.google.api.services.calendar.model.Calendar calendar : calendars) {
            final DateTimeConverter dateTimeConverterForTimeZone =
                    getDateTimeConverterForTimeZone(calendar.getTimeZone());
            final DateTime timeMinDateTime = dateTimeConverterForTimeZone.toGoogleDateTime(startDateTime);
            final DateTime timeMaxDateTime = dateTimeConverterForTimeZone.toGoogleDateTime(finishDateTime);
            final Events eventsOfCalendar = myService.events().list(calendar.getId())
                    .setTimeMin(timeMinDateTime)
                    .setTimeMax(timeMaxDateTime)
                    .setSingleEvents(false)
                    .setShowDeleted(false)
                    .execute();
            events.addAll(eventsOfCalendar.getItems());
        }
        return events;
    }

    @NotNull
    public List<Event> getUsualEventsOnDateFromCalendar(@NotNull LocalDate date,
                                                        @NotNull com.google.api.services.calendar.model.Calendar calendar) throws IOException {
        final LocalDateTime startDateTime = date.atStartOfDay();
        final LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        final DateTimeConverter dateTimeConverterForTimeZone = getDateTimeConverterForTimeZone(calendar.getTimeZone());
        final DateTime timeMinDateTime = dateTimeConverterForTimeZone.toGoogleDateTime(startDateTime);
        final DateTime timeMaxDateTime = dateTimeConverterForTimeZone.toGoogleDateTime(endDateTime);
        final List<Event> allDateEvents = myService.events().list(calendar.getId())
                .setTimeMin(timeMinDateTime)
                .setTimeMax(timeMaxDateTime)
                .setSingleEvents(true)
                .setShowDeleted(false)
                .execute().getItems();
        return allDateEvents.stream().filter(event -> event.getStart().getDateTime() != null).collect(Collectors.toList());
    }

    @NotNull
    public DateTimeConverter getDateTimeConverterForTimeZone(@Nullable String calendarTimeZone) {
        return new DateTimeConverter(calendarTimeZone);
    }

    public void patchEventInCalendar(@NotNull com.google.api.services.calendar.model.Calendar calendar,
                                     @NotNull String eventId,
                                     @NotNull Event eventPatch) throws IOException {
        myService.events().patch(calendar.getId(), eventId, eventPatch).execute();
    }

}
