package network.services.google.spreadsheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import network.services.ApiRequestsRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GoogleSpreadsheetsApiRequestsRunner implements ApiRequestsRunner {

    private final Sheets myService;

    GoogleSpreadsheetsApiRequestsRunner(Sheets myService) {
        this.myService = myService;
    }

    private ValueRange getValueRange(String spreadsheetId, String range) throws IOException {
        return myService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
    }

    public ValueRange downloadSpreadsheetRange(String spreadsheetId, String sheetName, String range) throws IOException {
        return getValueRange(spreadsheetId, sheetName + "!" + range);
    }

    public ValueRange downloadSheetFromSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
        return getValueRange(spreadsheetId, sheetName);
    }

    public Spreadsheet downloadSpreadsheet(String spreadsheetId) throws IOException {
        return myService.spreadsheets().get(spreadsheetId).execute();
    }

    public boolean isSheetExistsOnSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
        boolean[] exists = new boolean[1];
        iterateSheetsOfSpreadsheet(spreadsheetId, sheet -> {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                exists[0] = true;
            }
        });
        return exists[0];
    }

    private void iterateSheetsOfSpreadsheet(String spreadsheetId, Consumer<Sheet> callback) throws IOException {
        Spreadsheet spreadsheet = downloadSpreadsheet(spreadsheetId);
        for (Sheet sheet : spreadsheet.getSheets()) {
            callback.accept(sheet);
        }
    }

    public void addNewSheetToSpreadsheet(String spreadsheetId, String newSheetName) throws IOException {
        BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest();
        AddSheetRequest q = new AddSheetRequest().setProperties(new SheetProperties().setTitle(newSheetName));
        request.setRequests(Collections.singletonList(new Request().setAddSheet(q)));
        myService.spreadsheets().batchUpdate(spreadsheetId, request).execute();
    }

    public void deleteSheetFromSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
        BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest();
        Integer[] targetId = new Integer[1];
        targetId[0] = -1;
        iterateSheetsOfSpreadsheet(spreadsheetId, sheet -> {
            SheetProperties properties = sheet.getProperties();
            if (properties.getTitle().equals(sheetName)) {
                targetId[0] = properties.getSheetId();
            }
        });
        DeleteSheetRequest q = new DeleteSheetRequest().setSheetId(targetId[0]);
        request.setRequests(Collections.singletonList(new Request().setDeleteSheet(q)));
        myService.spreadsheets().batchUpdate(spreadsheetId, request).execute();
    }

    public void appendDataToSheetOnSpreadsheet(String spreadsheetId, String sheetName, List<List<Object>> rowContent) throws IOException {
        ValueRange range = new ValueRange()
                .setRange(sheetName)
                .setValues(rowContent)
                .setMajorDimension("ROWS");
        myService.spreadsheets().values().append(spreadsheetId, sheetName, range).setValueInputOption("RAW").execute();
    }

}