package network.services.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import network.services.AbstractAuthenticator;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public abstract class GoogleAuthenticator extends AbstractAuthenticator<GoogleClientSecrets> {

    private final List<String> myScopes;

    protected GoogleAuthenticator(String myTokenDirectoryPath, EncryptedResourceEntity credentials,
                                  List<String> myScopes) {
        super(myTokenDirectoryPath, credentials, GoogleClientSecrets.class);
        this.myScopes = myScopes;
    }

    protected NetHttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    protected Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException, GeneralSecurityException {
        GoogleClientSecrets clientSecrets = loadClientCredentials();

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, myJsonFactory, clientSecrets, myScopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(myTokenDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

}
