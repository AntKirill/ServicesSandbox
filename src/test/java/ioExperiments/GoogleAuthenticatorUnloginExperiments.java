package ioExperiments;

import network.services.ApiAuthenticator;
import network.services.ApiManager;
import network.services.ServicesManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class GoogleAuthenticatorUnloginExperiments {

    @Test
    void unlogin() throws IOException {
        ApiManager apiManager = ServicesManager.createTrelloApiManager();
        ApiAuthenticator authenticator = apiManager.getAuthenticator();
        authenticator.unlogin();
    }
}