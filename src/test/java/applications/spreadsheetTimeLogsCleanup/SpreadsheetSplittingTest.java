package applications.spreadsheetTimeLogsCleanup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpreadsheetSplittingTest {


    @Test
    void testLogEntitySplitter() {
        doTestMatchingStrings("10:00 - 11:00 llol", "10:00", "11:00", "llol");
        doTestMatchingStrings("10 - 11 padf", "10:00", "11:00", "padf");
        doTestMatchingStrings("10-11:30 padf", "10:00", "11:30", "padf");

        doTestRoundTime("10:00 - 11:00 adaf", 10, 11);
        doTestRoundTime("23:00 - 0", 23, 0);
        doTestRoundTime("23:15 - 23:47", 23, 24);
        doTestRoundTime("00:15 - 00:47", 0, 1);
        doTestRoundTime("22:15 - 01:47", 22, 2);
        doTestRoundTime("24:15 - 01:47", 0, 2);
    }

    private void doTestMatchingStrings(String logEntity, String begTime, String endTime, String description) {
        LogCellSplitter splitter = new LogCellSplitter(logEntity);
        Assertions.assertEquals(begTime, splitter.myBeginTime);
        Assertions.assertEquals(endTime, splitter.myEndTime);
        Assertions.assertEquals(description, splitter.myDescription);
    }

    private void doTestRoundTime(String logEntity, int roundedBegTime, int roundedEndTime) {
        LogCellSplitter splitter = new LogCellSplitter(logEntity);
        Assertions.assertEquals(roundedBegTime, splitter.myRoundedBeginTime);
        Assertions.assertEquals(roundedEndTime, splitter.myRoundedEndTime);
    }
}