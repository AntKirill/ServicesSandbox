package network.services;


import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import utils.EncryptedResourceEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public abstract class AbstractAuthenticator<T extends GenericJson> implements ApiAuthenticator {
    protected final JsonFactory myJsonFactory = JacksonFactory.getDefaultInstance();
    protected final String myTokenDirectoryPath;
    protected final EncryptedResourceEntity myCredentials;
    private final Class<T> myTypeParameterClass;

    protected AbstractAuthenticator(String tokenDirectoryPath, EncryptedResourceEntity credentials,
                                    Class<T> typeParameterClass) {
        this.myTokenDirectoryPath = tokenDirectoryPath;
        this.myCredentials = credentials;
        this.myTypeParameterClass = typeParameterClass;
    }

    protected T loadClientCredentials() throws IOException, GeneralSecurityException {
        final String credentialsDecrypted = myCredentials.getDecrypted();
        return myJsonFactory.fromString(credentialsDecrypted, myTypeParameterClass);
    }

    @Override
    public void unlogin() throws IOException {
        Files.deleteIfExists(Paths.get(myTokenDirectoryPath + "/" + StoredCredential.DEFAULT_DATA_STORE_ID));
    }
}
