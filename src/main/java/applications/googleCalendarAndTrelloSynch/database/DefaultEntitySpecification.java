package applications.googleCalendarAndTrelloSynch.database;

import database.SqlSpecification;
import org.jetbrains.annotations.NotNull;

public class DefaultEntitySpecification implements SqlSpecification {
    @Override
    public @NotNull String toSqlClauses() {
        return "IS_DEFAULT=TRUE";
    }
}
