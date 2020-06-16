package applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.ui.console;

import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.GoogleCalendarAndTrelloSynchCreator;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.business.GoogleCalendarAndTrelloSynchController;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsoleViewTest {

    @Test
    public void showUiHandledByControllerTest() throws IOException, ConfigurationException {
        GoogleCalendarAndTrelloSynchController controller = mock(GoogleCalendarAndTrelloSynchController.class);
        doNothing().when(controller).repostDailyEvents(any());
        GoogleCalendarAndTrelloSynchCreator creator = new GoogleCalendarAndTrelloSynchCreator();
        ConsoleView consoleView = new ConsoleView(controller);
        ConsoleView spyConsoleView = spy(consoleView);
        doReturn("Y", "", "n").when(spyConsoleView).getUserAnswer();
        spyConsoleView.show();
        spyConsoleView.show();
        spyConsoleView.show();
    }
}