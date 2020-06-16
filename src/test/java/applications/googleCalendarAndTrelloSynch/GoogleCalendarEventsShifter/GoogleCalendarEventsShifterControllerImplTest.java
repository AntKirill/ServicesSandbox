package applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter;

import applications.googleCalendarAndTrelloSynch.database.dao.CompactDao;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import network.services.google.DateTimeConverter;
import network.services.google.calendar.GoogleCalendarApiRequestsRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GoogleCalendarEventsShifterControllerImplTest {
    private final int shiftMinutes = 60;

    private Calendar getCalendarWithName(String calendarName) {
        return new Calendar().setSummary(calendarName).setTimeZone("Europe/Moscow");
    }

    private List<Event> getEvents() {
        ArrayList<Event> events = new ArrayList<>();
        {
            Event event = new Event();
            DateTime dateTimeStart = new DateTime("2020-06-16T21:00:00.000+03:00");
            DateTime dateTimeFinish = new DateTime("2020-06-16T22:00:00.000+03:00");
            event.setStart(new EventDateTime().setDateTime(dateTimeStart));
            event.setEnd(new EventDateTime().setDateTime(dateTimeFinish));
            event.setId("1");
            events.add(event);
        }
        {
            Event event = new Event();
            DateTime dateTimeStart = new DateTime("2020-06-16T17:00:00.000+03:00");
            DateTime dateTimeFinish = new DateTime("2020-06-16T17:30:00.000+03:00");
            event.setStart(new EventDateTime().setDateTime(dateTimeStart));
            event.setEnd(new EventDateTime().setDateTime(dateTimeFinish));
            event.setId("2");
            events.add(event);
        }
        return events;
    }

    private final class Checker implements Answer<Object> {
        private final HashMap<String, Event> idToEvent;
        private final Calendar calendar;
        private final DateTimeConverter converter;

        private Checker(List<Event> events, Calendar calendar) {
            idToEvent = new HashMap<>();
            this.calendar = calendar;
            events.forEach(event -> idToEvent.put(event.getId(), event));
            converter = new DateTimeConverter("Europe/Moscow");
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Calendar receivedCalendar = invocation.getArgument(0, Calendar.class);
            String receivedEventId = invocation.getArgument(1, String.class);
            Event receivedEventPatch = invocation.getArgument(2, Event.class);
            Assertions.assertEquals(calendar, receivedCalendar);
            Assertions.assertTrue(idToEvent.containsKey(receivedEventId));
            Event realEvent = idToEvent.remove(receivedEventId);
            LocalDateTime expectedStartTime =
                    converter.toLocalDateTime(realEvent.getStart().getDateTime()).plusMinutes(shiftMinutes);
            LocalDateTime expectedEndTime =
                    converter.toLocalDateTime(realEvent.getEnd().getDateTime()).plusMinutes(shiftMinutes);
            Assertions.assertEquals(expectedStartTime,
                    converter.toLocalDateTime(receivedEventPatch.getStart().getDateTime()));
            Assertions.assertEquals(expectedEndTime,
                    converter.toLocalDateTime(receivedEventPatch.getEnd().getDateTime()));
            return null;
        }
    }

    @Test
    void shiftGoogleCalendarEvents() throws IOException {
        GoogleCalendarApiRequestsRunner googleCalendarApiRequestsRunner = mock(GoogleCalendarApiRequestsRunner.class);
        doCallRealMethod().when(googleCalendarApiRequestsRunner).getDateTimeConverterForTimeZone(anyString());
        List<Event> events = getEvents();
        Calendar calendar = getCalendarWithName("SuperCalendar");
        doReturn(events).when(googleCalendarApiRequestsRunner).getUsualEventsOnDateFromCalendarLaterThanTime(any(LocalDateTime.class), any(Calendar.class));
        doAnswer(new Checker(events, calendar)).when(googleCalendarApiRequestsRunner).patchEventInCalendar(any(),
                anyString(),
                any());
        GoogleCalendarEventsShifterControllerImpl googleCalendarEventsShifterController =
                new GoogleCalendarEventsShifterControllerImpl(googleCalendarApiRequestsRunner,
                        (CompactDao<Calendar>) mock(CompactDao.class),
                        mock(Preferences.class));
        @NotNull GoogleCalendarShifterConfiguration config =
                new GoogleCalendarShifterConfiguration.GoogleCalendarShifterConfigurationBuilder()
                        .setCalendar(calendar)
                        .setDateTime(LocalDateTime.now())
                        .setMinutesShift(Integer.valueOf(shiftMinutes).shortValue())
                        .setHoursShift(Integer.valueOf(0).shortValue())
                        .setSelectedRadioButton(GoogleCalendarShifterConfiguration.SelectedRadioButton.NOW)
                        .createGoogleCalendarShifterConfiguration();
        googleCalendarEventsShifterController.shiftGoogleCalendarEvents(config);
    }
}