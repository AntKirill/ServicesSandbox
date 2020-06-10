package network.services.google.calendar;

import network.services.ApiManager;
import org.jetbrains.annotations.NotNull;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleCalendarApiManager extends ApiManager {
    private static final String GOOGLE_CRED_DIR_PATH = "/google/calendar";
    private static final String GOOGLE_TOKEN_DIRECTORY_PATH = ApplicationUtils.TOKENS_DIRECTORY_PATH +
            GOOGLE_CRED_DIR_PATH;
    private static final String GOOGLE_CRED_FILE_PATH = ApplicationUtils.CREDENTIALS_DIRECTORY_PATH +
            GOOGLE_CRED_DIR_PATH + "/credentials.encrypted";

    @NotNull
    @Override
    public GoogleCalendarApiRequestsRunner authenticateAndGetRequestsRunner() throws IOException, GeneralSecurityException {
        return (GoogleCalendarApiRequestsRunner) super.authenticateAndGetRequestsRunner();
    }

    @Override
    public GoogleCalendarAuthenticator getAuthenticator() {
        return new GoogleCalendarAuthenticator(GOOGLE_TOKEN_DIRECTORY_PATH, new EncryptedResourceEntity(GOOGLE_CRED_FILE_PATH));
    }
}
