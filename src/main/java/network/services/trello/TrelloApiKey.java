package network.services.trello;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class TrelloApiKey extends GenericJson {
    @Key("api_key")
    private String apiKey;

    @Key("secret_token")
    private String secretToken;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }
}
