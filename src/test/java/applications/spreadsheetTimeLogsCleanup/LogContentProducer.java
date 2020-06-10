package applications.spreadsheetTimeLogsCleanup;

import java.util.ArrayList;
import java.util.List;

public class LogContentProducer {

    public static final String RIGHT_ANS_LOG2 = "[[, , , , T (17.09), W (18.09), Th (19.09), ], [, , , , 06:00 - 07:00, Sleeping, Sleeping, Sleeping], [, , , , 07:00 - 08:00, Sleeping, Sleeping, Sleeping], [, , , , 08:00 - 09:00, Sleeping, Sleeping, Sleeping], [, , , , 09:00 - 10:00, Sleeping, Sleeping, Sleeping], [, , , , 10:00 - 11:00, Sleeping, aaAAA adfadf (10:00 - 11:00), Sleeping], [, , , , 11:00 - 12:00, Sleeping, Sleeping, aaAAA adfadf asdfqe (11:00 - 13:20)], [, , , , 12:00 - 13:00, Sleeping, Sleeping, aaAAA adfadf asdfqe (11:00 - 13:20)], [, , , , 13:00 - 14:00, Sleeping, Sleeping, aaAAA adfadf asdfqe (11:00 - 13:20)\n" +
            "dfadfa dasfasdf (13:00 - 14:00)], [, , , , 14:00 - 15:00, Sleeping, Sleeping, Sleeping], [, , , , 15:00 - 16:00, Sleeping, Sleeping, Sleeping], [, , , , 16:00 - 17:00, Sleeping, Sleeping, Sleeping], [, , , , 17:00 - 18:00, Sleeping, Sleeping, Sleeping], [, , , , 18:00 - 19:00, Sleeping, Sleeping, Sleeping], [, , , , 19:00 - 20:00, Sleeping, Sleeping, Sleeping], [, , , , 20:00 - 21:00, Sleeping, 89132 jkhdf ajkh 89123 (20:00 - 00:00), Sleeping], [, , , , 21:00 - 22:00, Sleeping, 89132 jkhdf ajkh 89123 (20:00 - 00:00), Sleeping], [, , , , 22:00 - 23:00, Sleeping, 89132 jkhdf ajkh 89123 (20:00 - 00:00), Sleeping], [, , , , 23:00 - 24:00, Sleeping, 89132 jkhdf ajkh 89123 (20:00 - 00:00), Sleeping], [, , , , 00:00 - 01:00, Sleeping, Sleeping, Sleeping], [, , , , 01:00 - 02:00, Sleeping, b (1:00 - 2:00), Sleeping], [, , , , 02:00 - 03:00, Sleeping, Sleeping, Sleeping], [, , , , 03:00 - 04:00, Sleeping, Sleeping, a (3:00 - 4:00)], [, , , , 04:00 - 05:00, Sleeping, Sleeping, Sleeping], [, , , , 05:00 - 06:00, Sleeping, Sleeping, Sleeping]]";
    private static final String LOG1 = "[\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "T (17.09)\n" +
            "W (18.09)\n" +
            "Th (19.09)\n" +
            "]\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "b\n" +
            "a\n" +
            "]\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "aaAAA adfadf\n" +
            "aaAAA adfadf asdfqe\n" +
            "uqwekj ;lasdfj asfd\n" +
            "]\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "89132 jkhdf ajkh 89123 \n" +
            "dfadfa dasfasdf\n" +
            "a\n" +
            "]\n" +
            "]";
    private static final String LOG2 = "[\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "T (17.09)\n" +
            "W (18.09)\n" +
            "Th (19.09)\n" +
            "]\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "1 - 2 b\n" +
            "3-4 a\n" +
            "]\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "10 - 11 aaAAA adfadf\n" +
            "11 - 13:20 aaAAA adfadf asdfqe\n" +
            "14:00 - 15 uqwekj ;lasdfj asfd\n" +
            "]\n" +
            "[\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "20:00 - 00:00 89132 jkhdf ajkh 89123 \n" +
            "13:00-14:00 dfadfa dasfasdf\n" +
            "a\n" +
            "]\n" +
            "]";

    private static List<List<Object>> getLogs(String log) {
        List<List<Object>> lists = new ArrayList<>();
        assert log.charAt(0) == '[';
        assert log.charAt(log.length() - 1) == ']';
        ArrayList<Object> curList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < log.length() - 1; i++) {
            char c = log.charAt(i);
            if (c == '[') {
                curList = new ArrayList<>();
            } else if (c == ']') {
                lists.add(curList);
            } else if (c == '\n') {
                curList.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            } else {
                stringBuilder.append(c);
            }
        }
        return lists;
    }

    public static List<List<Object>> getLogs1() {
        return getLogs(LOG1);
    }

    public static List<List<Object>> getLogs2() {
        return getLogs(LOG2);
    }
}
