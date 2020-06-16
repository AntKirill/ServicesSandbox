package applications.googleCalendarAndTrelloSynch.database.dao;

import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.Configuration;
import database.dao.Dao;
import org.jetbrains.annotations.Nullable;

public interface DefaultConfigurationDao extends Dao<Configuration> {
    @Nullable
    Configuration getCurrentDefaultConfiguration();

    void deleteDefaultConfigurations();
}
