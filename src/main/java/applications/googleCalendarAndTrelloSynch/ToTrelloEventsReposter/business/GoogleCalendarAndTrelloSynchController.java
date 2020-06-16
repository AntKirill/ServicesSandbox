package applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.business;

import applications.Application;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.Configuration;
import com.google.api.services.calendar.model.Calendar;
import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.List;

public interface GoogleCalendarAndTrelloSynchController extends Application.ApplicationController {
    void repostDailyEvents(@NotNull Configuration configuration) throws IOException, ConfigurationException;

    @NotNull
    List<TrelloBoard> getAllStoredTrelloBoards();

    @NotNull
    List<TrelloList> getAllStoredTrelloListsFromBoard(@NotNull TrelloBoard board);

    @NotNull
    List<Calendar> getAllStoredGoogleCalendars();

    void updateStorageWithFreshInformation() throws IOException;

    @Nullable
    Configuration getConfigurationToSelect();

    void updateDefaultConfiguration(@NotNull Configuration currentConfiguration);
}
