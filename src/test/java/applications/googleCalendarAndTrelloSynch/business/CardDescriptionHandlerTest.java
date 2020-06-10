package applications.googleCalendarAndTrelloSynch.business;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CardDescriptionHandlerTest {

    @Test
    void getDescriptionPresentation() {
        CardDescriptionHandler cardDescriptionHandler = new CardDescriptionHandler("<br>Hello! It is a text before list.<ol><li>InOl1</li><li>InOl2</li><li>InOl3</li></ol><br>It is a text after list.");
        String ans = cardDescriptionHandler.processHtmlDescription();
        String rightAns = "Hello! It is a text before list.\n" +
                "\n" +
                "1. InOl1\n" +
                "2. InOl2\n" +
                "3. InOl3\n" +
                "\n" +
                "It is a text after list.\n";
        Assertions.assertEquals(rightAns, ans);
    }
}