package applications.appsManager;

import applications.Application;
import applications.ApplicationCreator;
import applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter.GoogleCalendarEventsShifterCreator;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.GoogleCalendarAndTrelloSynchCreator;
import applications.spreadsheetTimeLogsCleanup.TimeLogsCleanuperCreator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class ApplicationsManager {

    public static Application createTimeLogsCleanuper() throws IOException, GeneralSecurityException {
        return new TimeLogsCleanuperCreator().createApplication();
    }

    public static Application createGoogleCalendarAndTrelloSynchronizer() throws IOException, GeneralSecurityException {
        return new GoogleCalendarAndTrelloSynchCreator().createApplication();
    }

    public static List<ApplicationCreator> getAllApplicationFactories() {
        return Arrays.asList(
                new GoogleCalendarAndTrelloSynchCreator(),
//                new TrelloCardsPostingCreator(),
                new TimeLogsCleanuperCreator(),
                new GoogleCalendarEventsShifterCreator());
    }
}
