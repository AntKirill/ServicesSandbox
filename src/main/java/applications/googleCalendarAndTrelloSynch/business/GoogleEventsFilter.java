package applications.googleCalendarAndTrelloSynch.business;

import com.google.api.services.calendar.model.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

public class GoogleEventsFilter {
    private final @NotNull HashSet<String> uniqueIds = new HashSet<>();

    private boolean isFinished(@Nullable LocalDate eventFinishDate) {
        if (eventFinishDate == null) {
            return false;
        }
        LocalDate date = LocalDate.now();
        return date.isAfter(eventFinishDate);
    }

    public boolean isInterestingDailyEvent(Event event) {
        final String eventId = event.getId();
        final List<String> rec = event.getRecurrence();
        LocalDate endDate = null;
        for (String str : rec) {
            String[] recParts = str.split(";");
            String[] firstPart = recParts[0].split(":");
            String control = firstPart[0];
            if (control.equals("RRULE")) {
                recParts[0] = firstPart[1];
                for (String part : recParts) {
                    if (part.startsWith("UNTIL=")) {
                        String endDateStr = part.replace("UNTIL=", "");
                        endDate = LocalDate.parse(endDateStr, DateTimeFormatter.BASIC_ISO_DATE);
                        break;
                    }
                }
                break;
            }
        }
        if (!event.getStatus().equals("cancelled") && !uniqueIds.contains(eventId) && !isFinished(endDate)) {
            uniqueIds.add(eventId);
            return true;
        }
        return false;
    }
}
