package applications.appsManager;

import applications.Application;
import applications.ApplicationCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class AppsManagerControllerImpl implements AppsManagerController {
    @Override
    public @NotNull String getApplicationName() {
        return Utils.APPLICATION_NAME;
    }

    @Override
    public Application createNewApplication(@NotNull ApplicationCreator applicationCreator, boolean isGuiMode, @Nullable JFrame parent) throws IOException, GeneralSecurityException {
        return applicationCreator
                .setGuiMode(true)
                .setParentGuiComponent(parent)
                .createApplication();
    }
}
