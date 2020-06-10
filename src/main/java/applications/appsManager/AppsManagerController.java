package applications.appsManager;

import applications.Application;
import applications.ApplicationCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AppsManagerController extends Application.ApplicationController {
    Application createNewApplication(@NotNull ApplicationCreator applicationCreator, boolean isGuiMode, @Nullable JFrame parent) throws IOException, GeneralSecurityException;
}
