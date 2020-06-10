package network.encryption;

import org.jetbrains.annotations.NotNull;
import utils.ApplicationUtils;
import utils.EncryptedResourceEntity;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Encryption {

    public static void main(String[] args) throws Exception {
        new Encryption().encryptCredentials();
    }

    private PublicKey readPublicKey() throws FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
        EncryptedResourceEntity resourceEntity = new EncryptedResourceEntity("rsa.public");
        List<String> lines = new BufferedReader(new InputStreamReader(resourceEntity.getAsInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.toList());

        String publicStr = lines.get(0);
        byte[] byteKey = Base64.getDecoder().decode(publicStr.getBytes());
        X509EncodedKeySpec ks = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(ks);
    }

    @NotNull
    private String encryptAES(String value, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        IvParameterSpec iv = new IvParameterSpec(Decryption.INIT_VECTOR.getBytes(UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public void encryptCredentials() throws Exception {
        List<String> credentialsFiles = Arrays.asList(
                ApplicationUtils.CREDENTIALS_DIRECTORY_PATH + "/google/calendar/credentials.json"
                , ApplicationUtils.CREDENTIALS_DIRECTORY_PATH + "/google/spreadsheets/credentials.json"
                , ApplicationUtils.CREDENTIALS_DIRECTORY_PATH + "/trello/credentials.json"
        );
        Decryption decryption = new Decryption();
        String aesKey = decryption.decryptAESKeyWithRSA(decryption.readPrivateKey());
        for (String credentials : credentialsFiles) {
            EncryptedResourceEntity entity = new EncryptedResourceEntity(credentials);
            InputStream inputStream = entity.getAsInputStream();
            String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

            Path path = Paths.get("encrypted" + credentials.replace(".json", ".encrypted"));
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            FileWriter fileWriter = new FileWriter(path.toFile());
            fileWriter.write(encryptAES(text, aesKey));
            fileWriter.close();
        }
    }
}
