package network.services.trello;

import com.google.api.client.auth.oauth2.StoredCredential;
import org.jetbrains.annotations.NotNull;

public class OAuth1Tokens {
    public final String apiKey;
    public final String apiSecretToken;

    public OAuth1Tokens(@NotNull TrelloApiKey apiKey, @NotNull StoredCredential apiSecretToken) {
        this.apiKey = apiKey.getApiKey();
        this.apiSecretToken = apiSecretToken.getAccessToken();
    }
}
