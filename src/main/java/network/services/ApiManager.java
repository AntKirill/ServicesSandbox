package network.services;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;

abstract public class ApiManager {
    @NotNull
    public ApiRequestsRunner authenticateAndGetRequestsRunner() throws IOException, GeneralSecurityException {
        final ApiAuthenticator authenticator = getAuthenticator();
        authenticator.authenticate();
        return authenticator.getRequestsRunner();
    }

    public abstract ApiAuthenticator getAuthenticator();
}
