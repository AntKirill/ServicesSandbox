package network.services.trello;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import network.services.AbstractAuthenticator;
import org.jetbrains.annotations.NotNull;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

public class TrelloAuthenticator extends AbstractAuthenticator<TrelloApiKey> {

    private static final String SCOPES = "https://trello.com/1/authorize?expiration=never&name="
            + ApplicationUtils.APPLICATION_NAME
            + "&scope=read,write&response_type=token";
    private OAuth1Tokens myCredentials = null;

    protected TrelloAuthenticator(String myTokenDirectoryPath, EncryptedResourceEntity credentials) {
        super(myTokenDirectoryPath, credentials, TrelloApiKey.class);
    }

    @Override
    public void authenticate() throws IOException, GeneralSecurityException {
        myCredentials = getCredentials();
    }

    @NotNull
    private OAuth1Tokens getCredentials() throws IOException, GeneralSecurityException {
        TrelloApiKey clientApiKey = super.loadClientCredentials();
        return new OAuth1Tokens(clientApiKey, getSecretToken(clientApiKey, "user"));
    }

    @NotNull
    private StoredCredential getSecretToken(TrelloApiKey clientSecretKey, String userId) throws IOException {
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new java.io.File(myTokenDirectoryPath));
        DataStore<StoredCredential> dataStore = StoredCredential.getDefaultDataStore(dataStoreFactory);
        StoredCredential storedCredential = dataStore.get(userId);
        if (storedCredential == null) {
            storedCredential = new StoredCredential();
            String token;
            try {
                token = downloadSecretTrelloToken(clientSecretKey);
            } catch (URISyntaxException e) {
                throw new IOException(e.getMessage());
            }
            storedCredential.setAccessToken(token);
            dataStore.set(userId, storedCredential);
        }
        return storedCredential;
    }

    @NotNull
    private String downloadSecretTrelloToken(TrelloApiKey apiKey) throws IOException, URISyntaxException {
        TrelloLocalServerReceiver receiver = new TrelloLocalServerReceiver.Builder().setPort(8888).build();
        String link = SCOPES + "&key=" + apiKey.getApiKey() +
                "&return_url=" + receiver.getRedirectUri();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(link));
        } else {
            System.out.println("Please proceed to the link: " + link);
        }
        try {
            receiver.waitForCode();
            return receiver.waitForCode();
        } finally {
            receiver.stop();
        }
    }

    @Override
    @NotNull
    public TrelloAdvancedRequestsRunner getRequestsRunner() {
        if (myCredentials == null) {
            throw new IllegalStateException("Authorization was not completed! Please, authorize first");
        }
        return new TrelloAdvancedRequestsRunner(myCredentials);
    }
}
