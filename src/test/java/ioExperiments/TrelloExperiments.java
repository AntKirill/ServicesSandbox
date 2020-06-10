package ioExperiments;

import network.services.trello.TrelloApiManager;
import network.services.trello.TrelloApiWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TrelloExperiments {

    @Test
    void testTrelloAuthorization() throws IOException, GeneralSecurityException {
        TrelloApiWrapper runner = new TrelloApiManager().authenticateAndGetRequestsRunner();
        Assertions.assertNotNull(runner);
    }
}
