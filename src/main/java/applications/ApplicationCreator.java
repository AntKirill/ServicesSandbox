package applications;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public abstract class ApplicationCreator {
    private boolean isGuiMode = false;
    private @Nullable JFrame parentGuiComponent = null;

    public Application createApplication() throws IOException, GeneralSecurityException {
        Object applicationController = createApplicationController();
        final Application.Viewable viewable;
        if (isGuiMode) {
            viewable = createGui(applicationController, parentGuiComponent);
        } else {
            viewable = createConsoleUi(applicationController);
        }
        return viewable::show;
    }

    public ApplicationCreator setGuiMode(boolean guiMode) {
        isGuiMode = guiMode;
        return this;
    }

    public ApplicationCreator setParentGuiComponent(JFrame parentGuiComponent) {
        this.parentGuiComponent = parentGuiComponent;
        return this;
    }

    @NotNull
    public abstract String getApplicationName();

    protected abstract Object createApplicationController() throws IOException, GeneralSecurityException;

    protected abstract Application.Viewable createConsoleUi(Object applicationController);

    protected abstract Application.Viewable createGui(Object applicationController, @Nullable JFrame parent);
}
