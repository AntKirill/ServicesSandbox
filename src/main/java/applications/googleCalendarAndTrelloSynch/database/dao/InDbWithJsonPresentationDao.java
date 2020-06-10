package applications.googleCalendarAndTrelloSynch.database.dao;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import database.DatabaseHandler;
import database.SqlSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class InDbWithJsonPresentationDao<T extends GenericJson> extends DatabaseHandler implements CompactDao<T> {

    private Class<T> type;

    protected InDbWithJsonPresentationDao(@NotNull String dataBaseName, @NotNull Class<T> type) {
        super(dataBaseName);
        this.type = type;
    }

    protected void addEntitiesToArrayFromJson(ArrayList<T> entities, ResultSet resultSet) throws SQLException, IOException {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        while (resultSet.next()) {
            String jsonPresentation = resultSet.getString(1);
            entities.add(jsonFactory.fromString(jsonPresentation, type));
        }
    }

    @Override
    public @NotNull List<T> selectQuery(@Nullable SqlSpecification sqlSpecification) {
        final ArrayList<T> entities = new ArrayList<>();
        String select_json_presentation_from_google_calendar = "SELECT JSON_PRESENTATION FROM " + getTableName();
        if (sqlSpecification != null) {
            String specification = sqlSpecification.toSqlClauses();
            select_json_presentation_from_google_calendar += " WHERE " + specification;
        }
        runQueryNoThrow(select_json_presentation_from_google_calendar, resultSet -> {
            try {
                addEntitiesToArrayFromJson(entities, resultSet);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        return entities;
    }

    @NotNull
    @Override
    public List<T> selectAll() {
        return selectQuery(null);
    }

    @NotNull
    protected abstract String getTableName();

    @NotNull
    protected abstract String getTableColumnsForNewEntity();

    abstract protected void appendToStringBuilder(Object entity, StringBuilder sb);

    @Override
    public void addAll(List<T> entities) {
        runUpdateNoThrow("INSERT INTO " + getTableName() + " " + getTableColumnsForNewEntity() + " VALUES " + toValues(entities));
    }

    @Override
    public void clearAll() {
        runUpdateNoThrow("DELETE FROM " + getTableName());
        runUpdateNoThrow("VACUUM");
    }

    protected String toValues(List<T> entities) {
        StringBuilder ans = new StringBuilder();
        boolean isFirst = true;
        for (T entity : entities) {
            if (isFirst) {
                isFirst = false;
            } else {
                ans.append(",\n");
            }
            appendToStringBuilder(entity, ans);
        }
//        ans.append(";");
        return ans.toString();
    }
}
