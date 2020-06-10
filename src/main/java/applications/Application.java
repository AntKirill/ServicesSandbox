package applications;

import org.jetbrains.annotations.NotNull;

public interface Application {
    void showHandledUi();

    interface Viewable {
        void show();
    }

    interface ApplicationController {
        @NotNull
        String getApplicationName();
    }
}
