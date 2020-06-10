package ioExperiments;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.ValueRange;
import network.services.google.spreadsheets.GoogleSpreadsheetsApiManager;
import network.services.google.spreadsheets.GoogleSpreadsheetsApiRequestsRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleSpreadsheetsExperiments {

    private static final String TESTING_SPREADSHEET_ID = "1d_Cez60h67z5CrQei9jQKqMOOWqSlz0-nTdCGZzJbvE";

    @Test
    void testDownloadSheet() throws IOException, GeneralSecurityException {
        GoogleSpreadsheetsApiRequestsRunner runner = new GoogleSpreadsheetsApiManager().authenticateAndGetRequestsRunner();
        ValueRange range = runner.downloadSheetFromSpreadsheet(TESTING_SPREADSHEET_ID, "Class Data");
        System.out.println(range);

        String newSheetName = "Class Data 1";
        if (!runner.isSheetExistsOnSpreadsheet(TESTING_SPREADSHEET_ID, newSheetName)) {
            runner.addNewSheetToSpreadsheet(TESTING_SPREADSHEET_ID, newSheetName);
        } else {
            runner.deleteSheetFromSpreadsheet(TESTING_SPREADSHEET_ID, newSheetName);
        }

        runner.appendDataToSheetOnSpreadsheet(TESTING_SPREADSHEET_ID, newSheetName, range.getValues());
    }

    @Test
    void testStrageDeleteRequest() throws IOException, GeneralSecurityException {
        GoogleSpreadsheetsApiRequestsRunner runner = new GoogleSpreadsheetsApiManager().authenticateAndGetRequestsRunner();
        Assertions.assertThrows(GoogleJsonResponseException.class, () -> runner.deleteSheetFromSpreadsheet(TESTING_SPREADSHEET_ID, "HELLO"));
    }

}
