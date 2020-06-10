package network.services.google.spreadsheets;

import network.services.ApiManager;
import org.jetbrains.annotations.NotNull;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleSpreadsheetsApiManager extends ApiManager {

    private static final String GOOGLE_CREDENTIALS_FILE_PATH = ApplicationUtils.CREDENTIALS_DIRECTORY_PATH +
            "/google/spreadsheets/credentials.encrypted";
    private static final String GOOGLE_TOKENS_DIRECTORY_PATH = ApplicationUtils.TOKENS_DIRECTORY_PATH +
            "/google/spreadsheets";

    @NotNull
    @Override
    public GoogleSpreadsheetsAuthenticator getAuthenticator() {
        return new GoogleSpreadsheetsAuthenticator(GOOGLE_TOKENS_DIRECTORY_PATH, new EncryptedResourceEntity(GOOGLE_CREDENTIALS_FILE_PATH));
    }

    @NotNull
    @Override
    public GoogleSpreadsheetsApiRequestsRunner authenticateAndGetRequestsRunner() throws IOException, GeneralSecurityException {
        return (GoogleSpreadsheetsApiRequestsRunner) super.authenticateAndGetRequestsRunner();
    }
}
