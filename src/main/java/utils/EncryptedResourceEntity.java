package utils;

import network.encryption.Decryption;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class EncryptedResourceEntity {
    private final String pathToEntity;

    public EncryptedResourceEntity(@NotNull String pathToEntity) {
        this.pathToEntity = pathToEntity;
    }

    @NotNull
    public InputStream getAsInputStream() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(pathToEntity);
        if (resourceAsStream == null) {
            throw new FileNotFoundException("File " + pathToEntity + " is not found in resources");
        }
        return resourceAsStream;
    }

    public String getDecrypted() throws IOException, GeneralSecurityException {
        Decryption decryption = new Decryption();
        return decryption.decryptApiToken(this);
    }
}
