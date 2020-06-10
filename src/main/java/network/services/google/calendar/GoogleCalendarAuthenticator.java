package network.services.google.calendar;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import network.services.google.GoogleAuthenticator;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleCalendarAuthenticator extends GoogleAuthenticator {
    private Calendar myService = null;

    protected GoogleCalendarAuthenticator(String myTokenDirectoryPath, EncryptedResourceEntity credentials) {
        super(myTokenDirectoryPath, credentials,
                Collections.singletonList(CalendarScopes.CALENDAR));
    }

    @Override
    public void authenticate() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = getHttpTransport();
        myService = new Calendar.Builder(httpTransport, myJsonFactory, getCredentials(httpTransport))
                .setApplicationName(ApplicationUtils.APPLICATION_NAME)
                .build();
    }

    @Override
    public GoogleCalendarApiRequestsRunner getRequestsRunner() {
        if (myService == null) {
            throw new IllegalStateException("Authentication was not completed");
        }
        return new GoogleCalendarApiRequestsRunner(myService);
    }
}
