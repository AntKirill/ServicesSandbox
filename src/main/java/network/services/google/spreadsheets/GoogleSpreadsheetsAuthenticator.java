package network.services.google.spreadsheets;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import network.services.google.GoogleAuthenticator;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleSpreadsheetsAuthenticator extends GoogleAuthenticator {
    private Sheets myService = null;

    public GoogleSpreadsheetsAuthenticator(String tokenDirectoryPath, EncryptedResourceEntity credentials) {
        super(tokenDirectoryPath, credentials,
                Collections.singletonList(SheetsScopes.SPREADSHEETS));
    }

    @Override
    public void authenticate() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = getHttpTransport();
        myService = new Sheets.Builder(httpTransport, myJsonFactory, getCredentials(httpTransport))
                .setApplicationName(ApplicationUtils.APPLICATION_NAME)
                .build();
    }

    @Override
    public GoogleSpreadsheetsApiRequestsRunner getRequestsRunner() {
        if (myService == null) {
            throw new IllegalStateException("Authentication was not completed");
        }
        return new GoogleSpreadsheetsApiRequestsRunner(myService);
    }
}
