package network.services.trello;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class TrelloAuthenticatorTest {

    @Test
    void testLoadingTrelloSecret(@TempDir Path tempDir) throws IOException, GeneralSecurityException {
        EncryptedResourceEntity entity = new EncryptedResourceEntity("network/services/trello/credentials.json");
        EncryptedResourceEntity entitySpy = spy(entity);
        doReturn("{\n" +
                "  \"api_key\": \"123\"\n" +
                "}").when(entitySpy).getDecrypted();
        TrelloAuthStub authenticator = new TrelloAuthStub(tempDir.toString(), "token.json", entitySpy);
        TrelloApiKey secret = authenticator.loadClientCredentials();
        Assertions.assertEquals("123", secret.getApiKey());
    }

    private final class TrelloAuthStub extends TrelloAuthenticator {

        protected TrelloAuthStub(String myTokenPath, String tokenFileName, EncryptedResourceEntity credentials) {
            super(myTokenPath, credentials);
        }

        @Override
        protected TrelloApiKey loadClientCredentials() throws IOException, GeneralSecurityException {
            return super.loadClientCredentials();
        }
    }

}