package applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter;

import com.google.api.services.calendar.model.Calendar;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class GoogleCalendarShifterConfiguration {
    private final @NotNull Calendar calendar;
    private final @NotNull LocalDateTime dateTime;
    private final @NotNull Short minutesShift;
    private final @NotNull Short hoursShift;
    private final @NotNull SelectedRadioButton selectedRadioButton;

    private GoogleCalendarShifterConfiguration(@NotNull Calendar calendar, @NotNull LocalDateTime dateTime,
                                               @NotNull Short minutesShift, @NotNull Short hoursShift,
                                               @NotNull SelectedRadioButton selectedRadioButton) {
        this.calendar = calendar;
        this.dateTime = dateTime;
        this.minutesShift = minutesShift;
        this.hoursShift = hoursShift;
        this.selectedRadioButton = selectedRadioButton;
    }

    public enum SelectedRadioButton {
        NOW(0), TOMORROW(1), TODAY_AT_TIME(2), ANY_DATE_TIME(3);

        private int i;

        SelectedRadioButton(int i) {
            this.i = i;
        }

        @NotNull
        public static SelectedRadioButton fromInteger(int i) {
            switch (i) {
                case 0:
                    return NOW;
                case 1:
                    return TOMORROW;
                case 2:
                    return TODAY_AT_TIME;
                case 3:
                    return ANY_DATE_TIME;
                default:
                    return NOW;
            }
        }

        public int toInt() {
            return i;
        }
    }


    public @NotNull Calendar getCalendar() {
        return calendar;
    }

    public @NotNull LocalDateTime getDateTime() {
        return dateTime;
    }

    @NotNull
    public Long getFullShiftInMinutes() {
        return 60L * hoursShift + minutesShift;
    }

    @NotNull
    public Short getMinutesShift() {
        return minutesShift;
    }

    @NotNull
    public Short getHoursShift() {
        return hoursShift;
    }

    @NotNull
    public SelectedRadioButton getSelectedRadioButton() {
        return selectedRadioButton;
    }

    public static class GoogleCalendarShifterConfigurationBuilder {
        private @NotNull Calendar calendar;
        private @NotNull LocalDateTime dateTime;
        private @NotNull Short minutesShift;
        private @NotNull Short hoursShift;
        private @NotNull SelectedRadioButton selectedRadioButton;

        @NotNull
        public GoogleCalendarShifterConfigurationBuilder setCalendar(@NotNull Calendar calendar) {
            this.calendar = calendar;
            return this;
        }

        @NotNull
        public GoogleCalendarShifterConfigurationBuilder setDateTime(@NotNull LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        @NotNull
        public GoogleCalendarShifterConfigurationBuilder setMinutesShift(@NotNull Short minutesShift) {
            this.minutesShift = minutesShift;
            return this;
        }

        @NotNull
        public GoogleCalendarShifterConfigurationBuilder setHoursShift(@NotNull Short hoursShift) {
            this.hoursShift = hoursShift;
            return this;
        }

        @NotNull
        public GoogleCalendarShifterConfigurationBuilder setSelectedRadioButton(@NotNull SelectedRadioButton selectedRadioButton) {
            this.selectedRadioButton = selectedRadioButton;
            return this;
        }

        @NotNull
        public GoogleCalendarShifterConfiguration createGoogleCalendarShifterConfiguration() {
            return new GoogleCalendarShifterConfiguration(calendar, dateTime, minutesShift, hoursShift,
                    selectedRadioButton);
        }
    }
}
