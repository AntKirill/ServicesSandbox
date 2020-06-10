package applications.spreadsheetTimeLogsCleanup;

import applications.Application;
import com.google.api.services.sheets.v4.model.ValueRange;
import network.services.ServicesManager;
import network.services.google.spreadsheets.GoogleSpreadsheetsApiRequestsRunner;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimeLogsCleanuperController implements Application.ApplicationController {

    private final @NotNull GoogleSpreadsheetsApiRequestsRunner myRequestsRunner;

    protected TimeLogsCleanuperController(@NotNull GoogleSpreadsheetsApiRequestsRunner requestsRunner) {
        this.myRequestsRunner = requestsRunner;
    }

    public static TimeLogsCleanuperController createTimeLogsCleanuperController() throws IOException, GeneralSecurityException {
        return new TimeLogsCleanuperController(
                ServicesManager.createGoogleSpreadsheetsApiManager().authenticateAndGetRequestsRunner());
    }

    @Override
    public @NotNull String getApplicationName() {
        return TimeLogsCleanuperCreator.NAME;
    }

    public void onCleanupRequest(String spreadsheetId, String sheetName) throws IOException {
        ValueRange range = myRequestsRunner.downloadSheetFromSpreadsheet(spreadsheetId, sheetName);
        List<List<Object>> newContent = logsToTable(range.getValues());
        String newSheetName = "Picture of week";
        if (myRequestsRunner.isSheetExistsOnSpreadsheet(spreadsheetId, newSheetName)) {
            myRequestsRunner.deleteSheetFromSpreadsheet(spreadsheetId, newSheetName);
        }
        myRequestsRunner.addNewSheetToSpreadsheet(spreadsheetId, newSheetName);
        myRequestsRunner.appendDataToSheetOnSpreadsheet(spreadsheetId, newSheetName, newContent);
    }

    protected List<List<Object>> logsToTable(List<List<Object>> logs) {
        List<List<Object>> logsCopy = new ArrayList<>(logs);
        HashMap<Integer, DayPhotoInfo> table = new HashMap<>();
        List<Object> days = logsCopy.get(0);
        int margin = 0;
        for (int i = 0; i < days.size(); i++) {
            String dayStr = (String) days.get(i);
            if (dayStr.length() == 0) {
                margin++;
                continue;
            }
            table.put(i, new DayPhotoInfo(i, dayStr));
        }

        for (int i = 1; i < logsCopy.size(); i++) {
            List<Object> rowLog = logsCopy.get(i);
            for (int id = 0; id < rowLog.size(); id++) {
                updateTable(table, id, rowLog.get(id));
            }
        }

        List<List<Object>> finalTable = new ArrayList<>();
        finalTable.add(days);
        fillFinalTableFromTimeToTime(6, 24, table, margin, finalTable);
        fillFinalTableFromTimeToTime(0, 6, table, margin, finalTable);

        return finalTable;
    }

    private void fillFinalTableFromTimeToTime(int begTime, int endTime, HashMap<Integer, DayPhotoInfo> table,
                                              int margin, List<List<Object>> finalTable) {
        for (int i = begTime; i < endTime; i++) {
            finalTable.add(getRowForTime(i, table, margin - 1));
        }
    }

    private ArrayList<Object> getRowForTime(int leftEdgeOfTime, HashMap<Integer, DayPhotoInfo> filledTable,
                                            int amountOfFirstEmptyCells) {
        ArrayList<Object> row = new ArrayList<>();
        for (int i = 0; i < amountOfFirstEmptyCells; i++) {
            row.add("");
        }
        String timeRange = toTimeFormat(leftEdgeOfTime) + " - " + toTimeFormat(leftEdgeOfTime + 1);
        row.add(timeRange);
        for (DayPhotoInfo photo : filledTable.values()) {
            String daysDescription = photo.get(leftEdgeOfTime);
            if (daysDescription == null) {
                daysDescription = "Sleeping";
            }
            row.add(daysDescription);
        }
        return row;
    }

    private String toTimeFormat(Integer i) {
        String iString = i.toString();
        if (iString.length() == 1) {
            iString = "0" + iString;
        }
        return iString + ":00";
    }

    private void updateTable(HashMap<Integer, DayPhotoInfo> table, int id, Object o) {
        DayPhotoInfo dayPhotoInfo = table.get(id);
        if (dayPhotoInfo == null) {
            return;
        }
        dayPhotoInfo.update(o);
    }
}
