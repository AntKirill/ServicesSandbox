package network.services.trello;

import network.services.ApiManager;
import org.jetbrains.annotations.NotNull;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TrelloApiManager extends ApiManager {

    private static final String TRELLO_CREDENTIALS_FILE_PATH = ApplicationUtils.CREDENTIALS_DIRECTORY_PATH +
            "/trello/credentials.encrypted";
    private static final String TRELLO_TOKEN_DIRECTORY_PATH = ApplicationUtils.TOKENS_DIRECTORY_PATH +
            "/trello";

    @NotNull
    @Override
    public TrelloAuthenticator getAuthenticator() {
        return new TrelloAuthenticator(TRELLO_TOKEN_DIRECTORY_PATH, new EncryptedResourceEntity(TRELLO_CREDENTIALS_FILE_PATH));
    }

    @NotNull
    @Override
    public TrelloAdvancedRequestsRunner authenticateAndGetRequestsRunner() throws IOException, GeneralSecurityException {
        return (TrelloAdvancedRequestsRunner) super.authenticateAndGetRequestsRunner();
    }
}
