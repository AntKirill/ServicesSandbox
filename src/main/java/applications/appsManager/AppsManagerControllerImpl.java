package applications.appsManager;

import applications.Application;
import applications.ApplicationCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.prefs.Preferences;

public class AppsManagerControllerImpl implements AppsManagerController {
    private static final String APPLICATION_KEY = "application";
    private final @NotNull Preferences preferences;

    public AppsManagerControllerImpl(@NotNull Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public @NotNull String getApplicationName() {
        return Utils.APPLICATION_NAME;
    }

    @Override
    public Application createNewApplication(@NotNull ApplicationCreator applicationCreator, boolean isGuiMode,
                                            @Nullable JFrame parent) throws IOException, GeneralSecurityException {
        return applicationCreator
                .setGuiMode(true)
                .setParentGuiComponent(parent)
                .createApplication();
    }

    @Override
    public @Nullable String getDefaultApplicationName() {
        return preferences.get(APPLICATION_KEY, null);
    }

    @Override
    public void updateDefaultApplicationName(@NotNull String name) {
        preferences.put(APPLICATION_KEY, name);
    }
}
