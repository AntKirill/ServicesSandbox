package applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter;

import com.google.api.services.calendar.model.Calendar;
import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

public class Configuration {
    private final @NotNull List<Calendar> googleCalendars;
    private final @NotNull TrelloBoard trelloBoard;
    private final @NotNull TrelloList trelloList;
    private final @Nullable LocalDate date;
    private final @NotNull Configuration.DateButton dateButtonClicked;

    private Configuration(@NotNull List<Calendar> googleCalendars,
                          @NotNull TrelloBoard trelloBoard,
                          @NotNull TrelloList trelloList,
                          @Nullable LocalDate date,
                          @NotNull Configuration.DateButton dateButtonClicked) {
        this.googleCalendars = googleCalendars;
        this.trelloBoard = trelloBoard;
        this.trelloList = trelloList;
        this.date = date;
        this.dateButtonClicked = dateButtonClicked;
    }

    @NotNull
    public List<Calendar> getGoogleCalendars() {
        return googleCalendars;
    }

    @NotNull
    public TrelloBoard getTrelloBoard() {
        return trelloBoard;
    }

    @NotNull
    public TrelloList getTrelloList() {
        return trelloList;
    }

    @Nullable
    public LocalDate getDate() {
        return date;
    }

    @NotNull
    public Configuration.DateButton getDateButtonClicked() {
        return dateButtonClicked;
    }

    public enum DateButton {
        TODAY(0), TOMORROW(1), OTHER_DATE(2);

        private final int value;

        DateButton(int value) {
            this.value = value;
        }

        public static DateButton fromInteger(int i) {
            switch (i) {
                case 0:
                    return TODAY;
                case 1:
                    return TOMORROW;
                default:
                    return OTHER_DATE;
            }
        }

        public int toInt() {
            return value;
        }

    }

    public static class ConfigurationBuilder {
        private List<Calendar> googleCalendars;
        private TrelloBoard trelloBoard;
        private TrelloList trelloList;
        private LocalDate date;
        private DateButton dateButtonClicked;

        public ConfigurationBuilder setGoogleCalendars(List<Calendar> googleCalendars) {
            this.googleCalendars = googleCalendars;
            return this;
        }

        public ConfigurationBuilder setTrelloBoard(TrelloBoard trelloBoard) {
            this.trelloBoard = trelloBoard;
            return this;
        }

        public ConfigurationBuilder setTrelloList(TrelloList trelloList) {
            this.trelloList = trelloList;
            return this;
        }

        public ConfigurationBuilder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public ConfigurationBuilder setDateButtonClicked(DateButton dateButtonClicked) {
            this.dateButtonClicked = dateButtonClicked;
            return this;
        }

        public Configuration createConfiguration() {
            return new Configuration(googleCalendars, trelloBoard, trelloList, date, dateButtonClicked);
        }
    }
}
