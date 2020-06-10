package applications.spreadsheetTimeLogsCleanup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogCellSplitter {
    private static final Pattern LOG_CELL_TIME_PATTERN =
            Pattern.compile("\\d\\d?:?\\d?\\d?\\s*-\\s*\\d\\d?:?\\d?\\d?");
    private static final Pattern LOG_CELL_EDGE_TIME_PATTERN =
            Pattern.compile("\\d\\d?:?\\d?\\d?");
    final String myLogEntity;
    int myRoundedBeginTime;
    int myRoundedEndTime;
    String myBeginTime;
    String myEndTime;
    String myDescription;

    public LogCellSplitter(String logEntity) throws IllegalArgumentException {
        myLogEntity = logEntity;
        Matcher matcher = LOG_CELL_TIME_PATTERN.matcher(myLogEntity);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Log entity does not contain time");
        }
        String partWithTime = myLogEntity.substring(matcher.start(), matcher.end());
        myDescription = myLogEntity.substring(matcher.end()).trim();
        Matcher matcher1 = LOG_CELL_EDGE_TIME_PATTERN.matcher(partWithTime);

        boolean foundStart = matcher1.find();
        assert foundStart;
        String beginTime = partWithTime.substring(matcher1.start(), matcher1.end());

        boolean foundEnd = matcher1.find();
        assert foundEnd;
        String endTime = partWithTime.substring(matcher1.start(), matcher1.end());

        myBeginTime = processEdgeTime(beginTime);
        myEndTime = processEdgeTime(endTime);
        myRoundedBeginTime = roundBeginTime();
        myRoundedEndTime = roundEndTime();
    }

    private static String processEdgeTime(String edgeTime) {
        if (!edgeTime.contains(":")) {
            return edgeTime + ":" + "00";
        }
        return edgeTime;
    }

    private int roundEndTime() {
        String[] splited = myEndTime.split(":");
        int hours = Integer.parseInt(splited[0]);
        int minutes = Integer.parseInt(splited[1]);
        if (minutes == 0) {
            return hours;
        }
        return hours + 1;
    }

    private int roundBeginTime() {
        String[] splited = myBeginTime.split(":");
        int hours = Integer.parseInt(splited[0]);
        if (hours >= 24) {
            return hours - 24;
        }
        return hours;
    }
}
