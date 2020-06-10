package applications.googleCalendarAndTrelloSynch;

import applications.Application;
import applications.ApplicationCreator;
import applications.googleCalendarAndTrelloSynch.business.GoogleCalendarAndTrelloSynchController;
import applications.googleCalendarAndTrelloSynch.business.GoogleCalendarAndTrelloSynchControllerImpl;
import applications.googleCalendarAndTrelloSynch.ui.console.ConsoleView;
import applications.googleCalendarAndTrelloSynch.ui.gui.GoogleCalendarTrelloSynchDialog;
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
    protected Object createApplicationController() throws IOException, GeneralSecurityException {
        return GoogleCalendarAndTrelloSynchControllerImpl.create();
    }

    @Override
    protected Application.Viewable createConsoleUi(Object applicationController) {
        GoogleCalendarAndTrelloSynchController controller = (GoogleCalendarAndTrelloSynchController) applicationController;
        return new ConsoleView(controller);
    }

    @Override
    protected Application.Viewable createGui(Object applicationController, @Nullable JFrame parent) {
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
