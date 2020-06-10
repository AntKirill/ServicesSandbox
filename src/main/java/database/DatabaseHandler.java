package database;

import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.function.Consumer;

public abstract class DatabaseHandler {

    protected final @NotNull String dataBaseName;

    protected DatabaseHandler(@NotNull String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    protected void runQuery(@NotNull String query, @NotNull ThrowingConsumer<ResultSet, SQLException> resultSetConsumer) throws SQLException {
        try (final Connection con = getConnection();
             final Statement st = con.createStatement();
             final ResultSet rs = st.executeQuery(query)) {
            resultSetConsumer.accept(rs);
        }
    }

    protected void runQueryNoThrow(@NotNull String query, @NotNull Consumer<ResultSet> resultSetConsumer) {
        try (final Connection con = getConnection();
             final Statement st = con.createStatement();
             final ResultSet rs = st.executeQuery(query)) {
            resultSetConsumer.accept(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void runUpdate(@NotNull String updateStatement) throws SQLException {
        try (final Connection con = getConnection();
             final Statement st = con.createStatement()) {
            st.executeUpdate(updateStatement);
        }
    }

    protected void runUpdateNoThrow(@NotNull String updateStatement) {
        try (final Connection con = getConnection();
             final Statement st = con.createStatement()) {
            st.executeUpdate(updateStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dataBaseName);
    }

    @FunctionalInterface
    protected interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

}
