package network.encryption;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import utils.EncryptedResourceEntity;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Decryption {
    protected static final String INIT_VECTOR = "uAQMZXCr5VZU3iiY";
    private static final String ENCRYPTED_AES_KEY = "awgkBErQodCn5qyU3XL5oxudvvLI94lBQHYtLuZGZ0cnsLjS2KsARI/wwv/wliAIsq9nJJUU695kVQZowD+3jcYr9HNyormcWWWi/dgQ4hfC8QQ1uOpmjlRb2P2OWT770e4xu7o3/FAZvJ5P8hvi7rVNIrgaqhtyXnz3gaTMgFjH6KTRzRrdUb4wlcLPgTkzZhNAJwmu3zjxpQsC5FV+T6HTPrF6uqvR+bHMRemQ5mmgpecYC6FnsYeP5iVtoMRW+5wBFgxe2gk5NwLRsDMbR5YOIscLknSAbLSXo6kcTAqZ+3f44nQgkeq7Q2D77pwUig6fm4fcjK2n9slbUl02rA==";
    private static final Logger LOGGER = Logger.getLogger(Decryption.class);

    @NotNull
    protected PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<String> lines = Files.readAllLines(Paths.get("rsa.private"), UTF_8);
        String publicStr = lines.get(0);
        byte[] byteKey = Base64.getDecoder().decode(publicStr.getBytes());
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(ks);
    }

    @NotNull
    protected String decryptAESKeyWithRSA(PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] bytes = Base64.getDecoder().decode(Decryption.ENCRYPTED_AES_KEY);
        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    @NotNull
    private String decryptAES(String encrypted, String key) throws InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            LOGGER.error(e.getStackTrace());
            throw new IllegalStateException("Internal error happened during decryption.");
        }
    }

    @NotNull
    public String decryptApiToken(EncryptedResourceEntity resourceEntity) throws IOException, GeneralSecurityException {
        final String aesKey;
        final PrivateKey privateKey;
        try {
            privateKey = readPrivateKey();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getStackTrace());
            throw new IllegalStateException("Internal error during reading of private key.");
        }
        try {
            aesKey = decryptAESKeyWithRSA(privateKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error(e.getStackTrace());
            throw new IllegalStateException("Internal error happened during decryption.");
        }
        final InputStream inputStream = resourceEntity.getAsInputStream();
        final String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        return decryptAES(text, aesKey);
    }
}
