package exception;

import java.sql.SQLException;

public class DatabaseException extends CinemaException {
    private SQLException sqlException;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof SQLException) {
            this.sqlException = (SQLException) cause;
        }
    }

    public DatabaseException(String message, SQLException sqlException) {
        super(message, sqlException);
        this.sqlException = sqlException;
    }

    public SQLException getSqlException() {
        return sqlException;
    }

    public String getSqlState() {
        return sqlException != null ? sqlException.getSQLState() : null;
    }

    public int getSqlErrorCode() {
        return sqlException != null ? sqlException.getErrorCode() : 0;
    }
}