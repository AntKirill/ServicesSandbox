package network.services.trello;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.reflect.TypeToken;
import network.services.ApiRequestsRunner;
import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloCard;
import network.services.trello.entities.TrelloList;
import network.services.trello.entities.TrelloMemberInfo;
import network.services.trello.queries.ListOfTrelloListsQuery;
import network.services.trello.queries.PostToTrelloListQuery;
import network.services.trello.queries.TrelloUserSpecificQuery;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class TrelloApiWrapper implements ApiRequestsRunner {
    private final OAuth1Tokens myCredentials;
    private final JsonFactory myJsonFactory = new JacksonFactory();
    private final HttpTransport myHttpTransport = new NetHttpTransport();

    public TrelloApiWrapper(@NotNull OAuth1Tokens myCredentials) {
        this.myCredentials = myCredentials;
    }

    public TrelloMemberInfo getTrelloMembersInfo() throws IOException {
        HttpRequest request = getHttpGetRequest(new TrelloUserSpecificQuery(myCredentials,
                "https://api.trello.com/1/members/me/"));
        return request.execute().parseAs(TrelloMemberInfo.class);
    }

    private HttpRequest getHttpGetRequest(GenericUrl genericUrl) throws IOException {
        HttpRequestFactory requestFactory = getHttpRequestFactory();
        return requestFactory.buildGetRequest(genericUrl);
    }

    @NotNull
    private HttpRequestFactory getHttpRequestFactory() {
        return myHttpTransport.createRequestFactory(request -> request.setParser(new JsonObjectParser(myJsonFactory)));
    }

    private HttpRequest getHttpPostRequest(GenericUrl genericUrl) throws IOException {
        HttpRequestFactory requestFactory = getHttpRequestFactory();
        return requestFactory.buildPostRequest(genericUrl, null);
    }

    public TrelloBoard getTrelloBoardById(String boardId) throws IOException {
        String url = "https://api.trello.com/1/boards/";
        url += boardId;
        return getHttpGetRequest(new TrelloUserSpecificQuery(myCredentials, url)).execute().parseAs(TrelloBoard.class);
    }

    @SuppressWarnings("unchecked")
    public List<TrelloList> getAllListsFromBoard(TrelloBoard board) throws IOException {
        String url = "https://api.trello.com/1/boards/";
        url += board.getBoardId();
        url += "/lists";
        Type type = new TypeToken<List<TrelloList>>() {
        }.getType();
        return (List<TrelloList>) getHttpGetRequest(new ListOfTrelloListsQuery(myCredentials, url))
                .execute().parseAs(type);
    }

    public void postCardToList(TrelloList trelloList, TrelloCard trelloCard) throws IOException {
        String url = "https://api.trello.com/1/cards";
        PostToTrelloListQuery query = new PostToTrelloListQuery(myCredentials, url);
        query.setIdList(trelloList.getId());
        query.setWholeTrelloCard(trelloCard);
        getHttpPostRequest(query).execute();
    }
}
