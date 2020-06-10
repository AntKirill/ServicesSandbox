package applications.spreadsheetTimeLogsCleanup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeLogsCleanuperCreatorTest {
    @Test
    void logsToTable() {
        TimeLogsCleanuperController mockedController = mock(TimeLogsCleanuperController.class);
        when(mockedController.logsToTable(any())).thenCallRealMethod();
        Assertions.assertThrows(IllegalArgumentException.class, () -> mockedController.logsToTable(LogContentProducer.getLogs1()));
        List<List<Object>> table = mockedController.logsToTable(LogContentProducer.getLogs2());
        Assertions.assertEquals(LogContentProducer.RIGHT_ANS_LOG2, table.toString());
    }
}