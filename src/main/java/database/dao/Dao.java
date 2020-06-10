package database.dao;

import database.SqlSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Dao<T> {

    @NotNull
    List<T> selectQuery(@Nullable SqlSpecification sqlSpecification);

    void add(T entity);

    void update(T entity);

    void delete(String id);

}
