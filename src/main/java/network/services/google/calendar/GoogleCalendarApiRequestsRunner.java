package network.services.google.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import network.services.ApiRequestsRunner;
import network.services.google.DateTimeConverter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class GoogleCalendarApiRequestsRunner implements ApiRequestsRunner {
    private static final String PRIMARY_CALENDAR_MARK = "primary";
    private final Calendar myService;

    public GoogleCalendarApiRequestsRunner(Calendar myService) {
        this.myService = myService;
    }

    public List<com.google.api.services.calendar.model.CalendarListEntry> getAllCalendarsEntries() throws IOException {
        String pageToken = null;
        List<com.google.api.services.calendar.model.CalendarListEntry> calendars = new ArrayList<>();
        do {
            CalendarList calendarList = myService.calendarList().list().setPageToken(pageToken).execute();
            calendars.addAll(calendarList.getItems());
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        return calendars;
    }

    public List<com.google.api.services.calendar.model.Calendar> getAllCalendars() throws IOException {
        final ArrayList<com.google.api.services.calendar.model.Calendar> calendars = new ArrayList<>();
        final List<CalendarListEntry> calendarListEntries = getAllCalendarsEntries();
        for (CalendarListEntry calendarListEntry : calendarListEntries) {
            calendars.add(getCalendarById(calendarListEntry.getId()));
        }
        return calendars;
    }

    public List<com.google.api.services.calendar.model.Calendar> getCalendarsByNames(List<String> names) throws IOException {
        List<CalendarListEntry> calendarsList = getAllCalendarsEntries();
        HashSet<String> nestedNamesSet = new HashSet<>(names);
        boolean isPrimaryRequired = nestedNamesSet.contains(PRIMARY_CALENDAR_MARK);
        boolean isPrimaryAdded = false;
        List<com.google.api.services.calendar.model.Calendar> calendars = new ArrayList<>();
        for (CalendarListEntry entry : calendarsList) {
            String entrySummary = entry.getSummary();
            if (nestedNamesSet.contains(entrySummary)) {
                if (isPrimaryRequired && entrySummary.equals(PRIMARY_CALENDAR_MARK)) {
                    isPrimaryAdded = true;
                }
                com.google.api.services.calendar.model.Calendar calendar = getCalendarById(entry.getId());
                calendars.add(calendar);
            }
            if (isPrimaryRequired && !isPrimaryAdded && entry.isPrimary()) {
                calendars.add(getCalendarById(entry.getId()));
                isPrimaryAdded = true;
            }
        }

        return calendars;
    }

    public com.google.api.services.calendar.model.Calendar getCalendarById(String id) throws IOException {
        return myService.calendars().get(id).execute();
    }

    public List<Event> getAlldayEvents(LocalDate date, List<com.google.api.services.calendar.model.Calendar> calendars) throws IOException {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime finishDateTime = startDateTime.plus(Duration.ofHours(1));
        List<Event> events = new ArrayList<>();
        for (com.google.api.services.calendar.model.Calendar calendar : calendars) {
            String calendarTimeZone = calendar.getTimeZone();
            ZoneId zoneId = ZoneId.of(calendarTimeZone);
            TimeZone timeZone = TimeZone.getTimeZone(calendarTimeZone);
            long valueTimeStart = TimeUnit.SECONDS.toMillis(startDateTime.atZone(zoneId).toEpochSecond());
            long valueTimeEnd = TimeUnit.SECONDS.toMillis(finishDateTime.atZone(zoneId).toEpochSecond());
            int offset = (int) TimeUnit.MILLISECONDS.toMinutes(timeZone.getOffset(valueTimeStart));

            DateTime timeMinDateTime = new DateTime(valueTimeStart, offset);
            DateTime timeMaxDateTime = new DateTime(valueTimeEnd, offset);
            Events eventsOfCalendar = myService.events().list(calendar.getId())
                    .setTimeMin(timeMinDateTime)
                    .setTimeMax(timeMaxDateTime)
                    .setSingleEvents(false)
                    .setShowDeleted(false)
                    .execute();
            events.addAll(eventsOfCalendar.getItems());
        }
        return events;
    }

    public List<Event> getUsualEventsOnDateFromCalendar(LocalDate date,
                                                        com.google.api.services.calendar.model.Calendar calendar) throws IOException {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        DateTimeConverter dateTimeConverterForTimeZone = getDateTimeConverterForTimeZone(calendar.getTimeZone());
        DateTime timeMinDateTime = dateTimeConverterForTimeZone.toGoogleDateTime(startDateTime);
        DateTime timeMaxDateTime = dateTimeConverterForTimeZone.toGoogleDateTime(endDateTime);
        List<Event> allDateEvents = myService.events().list(calendar.getId())
                .setTimeMin(timeMinDateTime)
                .setTimeMax(timeMaxDateTime)
                .setSingleEvents(true)
                .setShowDeleted(false)
                .execute().getItems();
        List<Event> usualEvents = new ArrayList<>();
        for (Event event : allDateEvents) {
            if (event.getStart().getDateTime() != null) {
                usualEvents.add(event);
            }
        }
        return usualEvents;
    }

    public DateTimeConverter getDateTimeConverterForTimeZone(@Nullable String calendarTimeZone) {
        return new DateTimeConverter(calendarTimeZone);
    }

    public void patchEventFromCalendar(com.google.api.services.calendar.model.Calendar calendar, String eventId,
                                       Event eventPatch) throws IOException {
        myService.events().patch(calendar.getId(), eventId, eventPatch).execute();
    }

}
