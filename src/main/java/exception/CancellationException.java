package exception;

public class CancellationException extends CinemaException {
    private int bookingId;
    private String reason;

    public CancellationException(String message) {
        super(message);
    }

    public CancellationException(int bookingId, String message) {
        super(message);
        this.bookingId = bookingId;
    }

    public CancellationException(int bookingId, String reason, String message) {
        super(message);
        this.bookingId = bookingId;
        this.reason = reason;
    }

    public CancellationException(String errorCode, int bookingId, String message) {
        super(errorCode, message);
        this.bookingId = bookingId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}