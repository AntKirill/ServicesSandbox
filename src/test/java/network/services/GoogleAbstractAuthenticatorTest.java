package network.services;

import network.services.google.spreadsheets.GoogleSpreadsheetsAuthenticator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.EncryptedResourceEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GoogleAbstractAuthenticatorTest {
    @Test
    void loadGoogleClientSecretsGoodCase(@TempDir Path tempDirCredentials) throws IOException, GeneralSecurityException {
        EncryptedResourceEntity resourceEntity = new EncryptedResourceEntity("network/services/credentials.json");
        EncryptedResourceEntity entitySpy = spy(resourceEntity);
        String text = new BufferedReader(new InputStreamReader(resourceEntity.getAsInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        doReturn(text).when(entitySpy).getDecrypted();
        GoogleSpreadsheetsAuthenticator authenticator = new GoogleSpreadsheetsAuthenticator(tempDirCredentials.toString(), entitySpy);
        Assertions.assertDoesNotThrow(authenticator::loadClientCredentials);
    }

    @Test
    void loadGoogleClientSecretsExceptions(@TempDir Path tempDirCredentials) {
        EncryptedResourceEntity resourceEntity = new EncryptedResourceEntity("network/services/credentials2.json");
        GoogleSpreadsheetsAuthenticator authenticator = new GoogleSpreadsheetsAuthenticator(tempDirCredentials.toString(),
                resourceEntity);
        Assertions.assertThrows(Exception.class, authenticator::loadClientCredentials);

        authenticator = new GoogleSpreadsheetsAuthenticator(tempDirCredentials.toString(),
                new EncryptedResourceEntity("NOT_EXIST"));
        Assertions.assertThrows(IOException.class, authenticator::loadClientCredentials);
    }
}
