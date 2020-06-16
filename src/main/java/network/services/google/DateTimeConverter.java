package network.services.google;

import com.google.api.client.util.DateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeConverter {
    private final @Nullable ZoneId zoneId;
    private final @Nullable TimeZone timeZone;

    public DateTimeConverter(@Nullable String calendarTimeZone) {
        if (calendarTimeZone != null) {
            zoneId = ZoneId.of(calendarTimeZone);
            timeZone = TimeZone.getTimeZone(calendarTimeZone);
        } else {
            zoneId = null;
            timeZone = null;
        }
    }

    @NotNull
    public LocalDateTime toLocalDateTime(@NotNull DateTime dateTime) {
        if (dateTime.isDateOnly()) {
            throw new IllegalArgumentException("Only date passed");
        }
        return LocalDateTime.parse(dateTime.toStringRfc3339(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @NotNull
    public DateTime toGoogleDateTime(@NotNull LocalDateTime localDateTime) {
        if ((zoneId == null) || (timeZone == null)) {
            throw new IllegalStateException("Calendar time zone is not specified");
        }
        long l = TimeUnit.SECONDS.toMillis(localDateTime.atZone(zoneId).toEpochSecond());
        int offset = (int) TimeUnit.MILLISECONDS.toMinutes(timeZone.getOffset(l));
        return new DateTime(l, offset);
    }
}
