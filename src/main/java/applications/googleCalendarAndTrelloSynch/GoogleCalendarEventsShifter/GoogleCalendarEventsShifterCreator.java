package applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter;

import applications.Application;
import applications.ApplicationCreator;
import applications.googleCalendarAndTrelloSynch.ApplicationConstants;
import applications.googleCalendarAndTrelloSynch.database.dao.GoogleCalendarDaoImpl;
import network.services.ServicesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.prefs.Preferences;

public class GoogleCalendarEventsShifterCreator extends ApplicationCreator {
    @Override
    public @NotNull String getApplicationName() {
        return GoogleCalendarEventsShifterUtils.APPLICATION_NAME;
    }

    @Override
    protected @NotNull Object createApplicationController() throws IOException, GeneralSecurityException {
        return new GoogleCalendarEventsShifterControllerImpl(
                ServicesManager.createGoogleCalendarApiManager().authenticateAndGetRequestsRunner(),
                GoogleCalendarDaoImpl.createGoogleCalendarDao(ApplicationConstants.DB_NAME),
                Preferences.userRoot().node("GoogleCalendarShifterNode"));
    }

    @Override
    @NotNull
    protected Application.Viewable createConsoleUi(Object applicationController) {
        throw new UnsupportedOperationException("This method is not implemented yet for application: " + getApplicationName());
    }

    @Override
    @NotNull
    protected Application.Viewable createGui(Object applicationController, @Nullable JFrame parent) {
        return () -> {
            assert parent != null;
            GoogleCalendarEventsShift dialog =
                    new GoogleCalendarEventsShift(
                            (GoogleCalendarEventsShifterController) applicationController, parent);
            dialog.setTitle(getApplicationName());
            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setSize(500, 350);
            dialog.setVisible(true);
        };
    }

    @Override
    public String toString() {
        return getApplicationName();
    }
}
