package applications.spreadsheetTimeLogsCleanup;

import applications.Application;
import applications.ApplicationCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TimeLogsCleanuperCreator extends ApplicationCreator {

    protected static final String NAME = "Google spreadsheet logs cleanup";
    private static final String SPREADSHEET_ID = "1RTAdg9kLplZb4_ryDnX6_HjmbTZ9vLWkohBFCPZ7Ncs";
    private static final String SHEET_NAME = "Logs";

    @NotNull
    @Override
    public String getApplicationName() {
        return NAME;
    }

    @Override
    protected @NotNull Object createApplicationController() throws IOException, GeneralSecurityException {
        return TimeLogsCleanuperController.createTimeLogsCleanuperController();
    }

    @Override
    @NotNull
    protected Application.Viewable createConsoleUi(Object applicationController) {
        TimeLogsCleanuperController controller = (TimeLogsCleanuperController) applicationController;
        return () -> {
            System.out.println("Cleanuping sheet with name: " + SHEET_NAME);
            try {
                controller.onCleanupRequest(SPREADSHEET_ID, SHEET_NAME);
            } catch (IOException e) {
                System.err.println("Error occurred");
            }
        };
    }

    @Override
    @NotNull
    protected Application.Viewable createGui(Object applicationController, @Nullable JFrame parent) {
        throw new UnsupportedOperationException("Gui is not supported for " + getApplicationName());
    }

    @Override
    public String toString() {
        return getApplicationName();
    }
}
