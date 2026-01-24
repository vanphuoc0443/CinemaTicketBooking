package exception;

public class SeatUnavailableException extends CinemaException {
    private int seatId;
    private String seatNumber;

    public SeatUnavailableException(String message) {
        super(message);
        this.seatId = 0;
        this.seatNumber = "";
    }

    public SeatUnavailableException(int seatId, String message) {
        super(message);
        this.seatId = seatId;
        this.seatNumber = "";
    }

    public int getSeatId() {
        return seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
