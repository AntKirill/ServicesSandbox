package applications.googleCalendarAndTrelloSynch.database.dao;

import database.dao.Dao;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CompactDao<T> extends Dao<T> {
    void addAll(List<T> entities);

    void clearAll();

    @NotNull
    List<T> selectAll();
}
