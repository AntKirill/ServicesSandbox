package network.services.trello.queries;

import com.google.api.client.util.Key;
import network.services.trello.OAuth1Tokens;
import network.services.trello.entities.TrelloCard;

public class PostToTrelloListQuery extends TrelloUserSpecificQuery {
    @Key("keepFromSource")
    private final String keepFromSourceFlag = "all";
    @Key("name")
    private String cardName;
    @Key("idList")
    private String idList;
    @Key("desc")
    private String cardDescription;
    @Key("due")
    private String dueDate;

    public PostToTrelloListQuery(OAuth1Tokens tokens, String encodedUrl) {
        super(tokens, encodedUrl);
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getIdList() {
        return idList;
    }

    public void setIdList(String idList) {
        this.idList = idList;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setWholeTrelloCard(TrelloCard card) {
        for (String key : this.getClassInfo().getNames()) {
            if (card.containsKey(key)) {
                this.put(key, card.get(key));
            }
        }
    }

}
