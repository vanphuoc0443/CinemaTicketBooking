package exception;

public class CinemaException extends Exception {
    private String errorCode;

    public CinemaException(String message) {
        super(message);
    }

    public CinemaException(String message, Throwable cause) {
        super(message, cause);
    }

    public CinemaException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CinemaException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}