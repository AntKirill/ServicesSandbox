package applications.googleCalendarAndTrelloSynch.ui.gui;

import com.google.api.client.json.GenericJson;

public class ComboItem<T extends GenericJson> {
    private String key;
    private T value;

    public ComboItem(String key, T value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
