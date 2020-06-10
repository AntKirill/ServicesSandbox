package network.services;

import network.services.google.calendar.GoogleCalendarApiManager;
import network.services.google.spreadsheets.GoogleSpreadsheetsApiManager;
import network.services.trello.TrelloApiManager;

public class ServicesManager {
    public static TrelloApiManager createTrelloApiManager() {
        return new TrelloApiManager();
    }

    public static GoogleSpreadsheetsApiManager createGoogleSpreadsheetsApiManager() {
        return new GoogleSpreadsheetsApiManager();
    }

    public static GoogleCalendarApiManager createGoogleCalendarApiManager() {
        return new GoogleCalendarApiManager();
    }
}
