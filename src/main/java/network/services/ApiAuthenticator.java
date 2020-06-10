package network.services;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface ApiAuthenticator {
    void authenticate() throws IOException, GeneralSecurityException;

    void unlogin() throws IOException;

    ApiRequestsRunner getRequestsRunner();
}
