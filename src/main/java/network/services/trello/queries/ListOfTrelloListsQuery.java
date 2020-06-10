package network.services.trello.queries;

import com.google.api.client.util.Key;
import network.services.trello.OAuth1Tokens;

public class ListOfTrelloListsQuery extends TrelloUserSpecificQuery {
    @Key("cards")
    private final String cards = "none";

//    @Key("filter")
//    private final String filter = "none";

    public ListOfTrelloListsQuery(OAuth1Tokens tokens, String encodedUrl) {
        super(tokens, encodedUrl);
    }
}
