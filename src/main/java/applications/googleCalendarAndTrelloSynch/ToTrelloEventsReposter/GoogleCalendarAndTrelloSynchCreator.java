package applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter;

import applications.Application;
import applications.ApplicationCreator;
import applications.googleCalendarAndTrelloSynch.ApplicationConstants;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.business.GoogleCalendarAndTrelloSynchController;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.business.GoogleCalendarAndTrelloSynchControllerImpl;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.ui.console.ConsoleView;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.ui.gui.GoogleCalendarTrelloSynchDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleCalendarAndTrelloSynchCreator extends ApplicationCreator {
    @NotNull
    @Override
    public String getApplicationName() {
        return ApplicationConstants.APPLICATION_NAME;
    }

    @Override
    protected @NotNull Object createApplicationController() throws IOException, GeneralSecurityException {
        return GoogleCalendarAndTrelloSynchControllerImpl.create();
    }

    @Override
    protected Application.@NotNull Viewable createConsoleUi(Object applicationController) {
        GoogleCalendarAndTrelloSynchController controller = (GoogleCalendarAndTrelloSynchController) applicationController;
        return new ConsoleView(controller);
    }

    @Override
    protected Application.@NotNull Viewable createGui(Object applicationController, @Nullable JFrame parent) {
        return () -> {
            GoogleCalendarTrelloSynchDialog dialog = new GoogleCalendarTrelloSynchDialog(parent, (GoogleCalendarAndTrelloSynchController) applicationController);
            dialog.prepareAndShow();
        };
    }

    @Override
    public String toString() {
        return getApplicationName();
    }

}
