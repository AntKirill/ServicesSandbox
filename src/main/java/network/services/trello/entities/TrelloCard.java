package network.services.trello.entities;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import org.jetbrains.annotations.Nullable;

public class TrelloCard extends GenericJson {
    @Key("desc")
    private String myDescription;

    @Key("name")
    private String name;

    @Key("id")
    private String id;

    @Key("due")
    private String dueDate;

    @Nullable
    public String getDescription() {
        return myDescription;
    }

    public void setDescription(String description) {
        this.myDescription = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
