package bootstrap;

import applications.Application;
import applications.appsManager.AppsManagerCreator;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        AppsManagerCreator creator = new AppsManagerCreator();
        Application application = creator.setGuiMode(true).createApplication();
        application.showHandledUi();
    }
}
