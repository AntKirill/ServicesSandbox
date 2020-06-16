package applications.appsManager;

import applications.Application;
import applications.ApplicationCreator;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.prefs.Preferences;


public class AppsManagerCreator extends ApplicationCreator {
    private static final @NotNull Logger LOGGER = Logger.getLogger(AppsManagerCreator.class);

    @Override
    protected @NotNull Object createApplicationController() {
        return new AppsManagerControllerImpl(Preferences.userRoot().node("AppsManagerNode"));
    }

    @Override
    @NotNull
    protected Application.Viewable createConsoleUi(Object applicationController) {
        return new Application.Viewable() {
            private final @NotNull AppsManagerController myController = (AppsManagerController) applicationController;

            protected void printToUser(Object o) {
                System.out.println(o);
            }

            protected void printError(Object o) {
                System.err.println(o);
            }

            @NotNull
            protected String getUserInput() {
                Scanner scanner = new Scanner(System.in);
                String nextLine = scanner.nextLine();
                scanner.close();
                return nextLine;
            }

            @Override
            public void show() {
                List<ApplicationCreator> applicationCreators = ApplicationsManager.getAllApplicationFactories();
                HashMap<Integer, ApplicationCreator> idToApplication = new HashMap<>();
                int id = 0;
                for (ApplicationCreator creator : applicationCreators) {
                    idToApplication.put(id, creator);
                    id++;
                }
                printToUser("Which application would you like to create?");
                for (Map.Entry<Integer, ApplicationCreator> entry : idToApplication.entrySet()) {
                    printToUser(entry.getKey() + " - " + entry.getValue().getApplicationName());
                }
                String ans = getUserInput();
                int chosenId = Integer.parseInt(ans);
                ApplicationCreator creator = idToApplication.get(chosenId);
                printToUser("You chose: " + creator.getApplicationName());
                printToUser("Creating ...");
                Application application = null;
                try {
                    application = creator.setGuiMode(false).createApplication();
                } catch (IOException e) {
                    printError("Network request error occurred: " + e.getMessage());
                    LOGGER.error(e);
                    return;
                } catch (GeneralSecurityException e) {
                    printError("Network security error occurred: " + e.getMessage());
                    LOGGER.error(e);
                    return;
                } catch (RuntimeException e) {
                    printError("Internal error occurred.");
                    LOGGER.error(e);
                    return;
                }
                if (application == null) {
                    printError("Internal error occurred.");
                    LOGGER.error("Got unknown exception.");
                    return;
                }
                printToUser("Application successfully created! Executing ...");
                application.showHandledUi();
            }
        };
    }

    @Override
    public @NotNull String getApplicationName() {
        return Utils.APPLICATION_NAME;
    }

    @Override
    @NotNull
    protected Application.Viewable createGui(Object applicationController, @Nullable JFrame parent) {
        return new Application.Viewable() {
            private void centreWindow(Window frame) {
                final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
                int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
                frame.setLocation(x, y);
            }

            @Override
            public void show() {
                SwingUtilities.invokeLater(() -> {
                    Thread.setDefaultUncaughtExceptionHandler(new MyHandler());
                    JFrame allAppsTogetherFrame = new AllAppsTogether(Utils.APPLICATION_NAME,
                            (AppsManagerController) applicationController);
                    allAppsTogetherFrame.pack();
                    allAppsTogetherFrame.setSize(500, 300);
                    centreWindow(allAppsTogetherFrame);
                    allAppsTogetherFrame.setVisible(true);
                });
            }
        };
    }

    private static final class MyHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error("Exception caught: ", e);
        }
    }

}
