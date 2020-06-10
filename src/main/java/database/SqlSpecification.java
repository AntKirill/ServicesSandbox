package database;

import org.jetbrains.annotations.NotNull;

public interface SqlSpecification {
    @NotNull
    String toSqlClauses();
}
