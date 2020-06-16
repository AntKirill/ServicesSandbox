package applications.googleCalendarAndTrelloSynch.ui.gui;

public class ComboItem<T> {
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
