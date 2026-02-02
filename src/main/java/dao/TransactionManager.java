package dao;

import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class TransactionManager {
    private Connection connection;

    public TransactionManager() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    public void commit() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.commit();
        }
    }

    public void rollback() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // Thuc thi trong transaction
    public Object executeInTransaction(Callable<?> task) throws SQLException {
        try {
            beginTransaction();
            Object result = task.call();
            commit();
            return result;
        } catch (Exception e) {
            rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Transaction failed", e);
        } finally {
            close();
        }
    }
}