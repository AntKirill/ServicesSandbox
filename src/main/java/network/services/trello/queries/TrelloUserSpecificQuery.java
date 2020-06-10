package network.services.trello.queries;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;
import network.services.trello.OAuth1Tokens;

public class TrelloUserSpecificQuery extends GenericUrl {
    @Key("key")
    private final String apiKey;

    @Key("token")
    private final String apiSecretToken;

    public TrelloUserSpecificQuery(OAuth1Tokens tokens, String encodedUrl) {
        super(encodedUrl);
        apiKey = tokens.apiKey;
        apiSecretToken = tokens.apiSecretToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecretToken() {
        return apiSecretToken;
    }
}
