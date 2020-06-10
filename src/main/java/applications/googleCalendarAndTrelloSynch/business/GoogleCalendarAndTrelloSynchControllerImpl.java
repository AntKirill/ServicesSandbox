package applications.googleCalendarAndTrelloSynch.business;

import applications.googleCalendarAndTrelloSynch.Configuration;
import applications.googleCalendarAndTrelloSynch.database.TrelloListByBoardIdSpecification;
import applications.googleCalendarAndTrelloSynch.database.dao.*;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import network.services.ServicesManager;
import network.services.google.calendar.GoogleCalendarApiRequestsRunner;
import network.services.trello.TrelloAdvancedRequestsRunner;
import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloCard;
import network.services.trello.entities.TrelloList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static applications.googleCalendarAndTrelloSynch.ApplicationConstants.APPLICATION_NAME;
import static applications.googleCalendarAndTrelloSynch.ApplicationConstants.DB_NAME;

public class GoogleCalendarAndTrelloSynchControllerImpl implements GoogleCalendarAndTrelloSynchController {

    private final @NotNull GoogleCalendarApiRequestsRunner myGoogleCalendarApiRequestsRunner;
    private final @NotNull TrelloAdvancedRequestsRunner myTrelloApiRequestsRunner;
    private final @NotNull DefaultConfigurationDao defaultConfigurationDao;
    private final @NotNull CompactDao<TrelloList> trelloListDao;
    private final @NotNull CompactDao<TrelloBoard> trelloBoardDao;
    private final @NotNull CompactDao<Calendar> googleCalendarDao;

    protected GoogleCalendarAndTrelloSynchControllerImpl(@NotNull GoogleCalendarApiRequestsRunner googleCalendarApiRequestsRunner,
                                                         @NotNull TrelloAdvancedRequestsRunner myTrelloApiRequestsRunner,
                                                         @NotNull DefaultConfigurationDao defaultConfigurationDao,
                                                         @NotNull CompactDao<TrelloList> trelloListDao,
                                                         @NotNull CompactDao<TrelloBoard> trelloBoardDao,
                                                         @NotNull CompactDao<Calendar> googleCalendarDao) {
        this.myGoogleCalendarApiRequestsRunner = googleCalendarApiRequestsRunner;
        this.myTrelloApiRequestsRunner = myTrelloApiRequestsRunner;
        this.defaultConfigurationDao = defaultConfigurationDao;
        this.trelloListDao = trelloListDao;
        this.trelloBoardDao = trelloBoardDao;
        this.googleCalendarDao = googleCalendarDao;
    }

    public static GoogleCalendarAndTrelloSynchController create() throws IOException, GeneralSecurityException {
        return new GoogleCalendarAndTrelloSynchControllerImpl(
                ServicesManager.createGoogleCalendarApiManager().authenticateAndGetRequestsRunner(),
                ServicesManager.createTrelloApiManager().authenticateAndGetRequestsRunner(),
                ConfigurationDaoImpl.createConfigurationDaoImpl(),
                TrelloListDaoImpl.createTrelloListDao(DB_NAME),
                TrelloBoardDaoImpl.createTrelloBoardDao(DB_NAME),
                GoogleCalendarDaoImpl.createGoogleCalendarDao(DB_NAME));
    }

    private static Duration measureTime(Runnable runnable) {
        Instant start = Instant.now();
        runnable.run();
        Instant end = Instant.now();
        return Duration.between(start, end);
    }

    @NotNull
    @Override
    public String getApplicationName() {
        return APPLICATION_NAME;
    }

    @Override
    public void repostDailyEvents(@NotNull Configuration configuration) throws IOException, ConfigurationException {
        final List<Calendar> calendars = configuration.getGoogleCalendars();
        final LocalDate date = configuration.getDate();
        if (date == null) {
            throw new ConfigurationException("The date is not set.");
        }
        final List<Event> events = myGoogleCalendarApiRequestsRunner.getAlldayEvents(date, calendars);
        final GoogleEventsFilter eventsFilter = getGoogleEventsFilter();
        final String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T19:30:00.000Z";
        for (Event event : events) {
            if (eventsFilter.isInterestingDailyEvent(event)) {
                final TrelloCard card = new TrelloCard();
                card.setName(event.getSummary());
                final CardDescriptionHandler cardDescriptionHandler = new CardDescriptionHandler(event.getDescription());
                card.setDescription(cardDescriptionHandler.getDescriptionPresentation());
                card.setDueDate(dateStr);
                myTrelloApiRequestsRunner.postCardToList(configuration.getTrelloList(), card);
            }
        }
    }

    @NotNull
    protected GoogleEventsFilter getGoogleEventsFilter() {
        return new GoogleEventsFilter();
    }

    @NotNull
    @Override
    public List<TrelloBoard> getAllStoredTrelloBoards() {
        return trelloBoardDao.selectAll();
    }

    @NotNull
    @Override
    public List<TrelloList> getAllStoredTrelloListsFromBoard(@NotNull TrelloBoard board) {
        return trelloListDao.selectQuery(new TrelloListByBoardIdSpecification(board.getBoardId()));
    }

    @NotNull
    @Override
    public List<Calendar> getAllStoredGoogleCalendars() {
        return googleCalendarDao.selectAll();
    }

    @Override
    public void updateStorageWithFreshInformation() throws IOException {
        updateGoogleCalendarInformation();
        final List<TrelloBoard> newBoards = updateTrelloBoardsInformation();
        updateTrelloListsInformation(newBoards);
    }

    @Nullable
    @Override
    public Configuration getConfigurationToSelect() {
        return defaultConfigurationDao.getCurrentDefaultConfiguration();
    }

    @Override
    public void updateDefaultConfiguration(@NotNull Configuration currentConfiguration) {
        defaultConfigurationDao.update(currentConfiguration);
    }

    private void updateTrelloListsInformation(List<TrelloBoard> boards) throws IOException {
        final List<TrelloList> trelloLists = new ArrayList<>();
        for (TrelloBoard board : boards) {
            trelloLists.addAll(myTrelloApiRequestsRunner.getAllListsFromBoard(board));
        }
        trelloListDao.clearAll();
        trelloListDao.addAll(trelloLists);
    }

    private void updateGoogleCalendarInformation() throws IOException {
        final List<Calendar> calendars = myGoogleCalendarApiRequestsRunner.getAllCalendars();
        googleCalendarDao.clearAll();
        googleCalendarDao.addAll(calendars);
    }

    @NotNull
    private List<TrelloBoard> updateTrelloBoardsInformation() throws IOException {
        final List<TrelloBoard> boards = myTrelloApiRequestsRunner.getAllTrelloBoards();
        trelloBoardDao.clearAll();
        trelloBoardDao.addAll(boards);
        return boards;
    }
}
// Solve daily wu1: {"created":"2019-10-08T10:04:19.000Z","creator":{"email":"kirant9797@gmail.com"},"end":{"date":"2019-10-09"},"etag":"\"3159985078410000\"","htmlLink":"https://www.google.com/calendar/event?eid=MTE1am1rMjdvNXBldWlqdG4zajc2ZXA5NzBfMjAxOTEwMDggNXN0ZjdpYWloZnZiYTVxbDdqZGZrMWo0dWdAZw","iCalUID":"115jmk27o5peuijtn3j76ep970@google.com","id":"115jmk27o5peuijtn3j76ep970","kind":"calendar#event","organizer":{"displayName":"RegularTasks","email":"5stf7iaihfvba5ql7jdfk1j4ug@group.calendar.google.com","self":true},"recurrence":["RRULE:FREQ=DAILY;UNTIL=20200124"],"reminders":{"useDefault":false},"sequence":0,"start":{"date":"2019-10-08"},"status":"confirmed","summary":"Solve daily WordUp","transparency":"transparent","updated":"2020-01-25T22:48:59.205Z"}
// Solve daily wu2: {"created":"2019-10-08T10:04:19.000Z","creator":{"email":"kirant9797@gmail.com"},"end":{"date":"2020-01-26"},"etag":"\"3159985078356000\"","htmlLink":"https://www.google.com/calendar/event?eid=MTE1am1rMjdvNXBldWlqdG4zajc2ZXA5NzBfUjIwMjAwMTI1XzIwMjAwMTI1IDVzdGY3aWFpaGZ2YmE1cWw3amRmazFqNHVnQGc","iCalUID":"115jmk27o5peuijtn3j76ep970_R20200125@google.com","id":"115jmk27o5peuijtn3j76ep970_R20200125","kind":"calendar#event","organizer":{"displayName":"RegularTasks","email":"5stf7iaihfvba5ql7jdfk1j4ug@group.calendar.google.com","self":true},"recurrence":["RRULE:FREQ=DAILY"],"reminders":{"useDefault":false},"sequence":0,"start":{"date":"2020-01-25"},"status":"confirmed","summary":"Solve daily WordUp","transparency":"transparent","updated":"2020-01-25T22:48:59.205Z"}