package controller;

import dao.SeatDAO;
import model.Seat;
import model.SeatStatus;
import exception.DatabaseException;
import exception.SeatUnavailableException;
import java.sql.SQLException;
import java.util.List;

public class SeatController {
    private SeatDAO seatDAO;

    public SeatController(SeatDAO seatDAO) {
        this.seatDAO = seatDAO;
    }

    // Lay tat ca ghe theo suat chieu
    public List<Seat> getSeatsByShowtime(int showtimeId) throws DatabaseException {
        try {
            return seatDAO.findByShowtime(showtimeId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai danh sach ghe", e);
        }
    }

    // Lay ghe con trong
    public List<Seat> getAvailableSeats(int showtimeId) throws DatabaseException {
        try {
            return seatDAO.findAvailableSeats(showtimeId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai ghe trong", e);
        }
    }

    // Lay thong tin ghe
    public Seat getSeatById(int seatId) throws DatabaseException {
        try {
            Seat seat = seatDAO.findById(seatId);
            if (seat == null) {
                throw new DatabaseException("Khong tim thay ghe");
            }
            return seat;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tai thong tin ghe", e);
        }
    }

    // Dat ghe tam thoi (Pessimistic Locking)
    public boolean reserveSeat(int seatId) throws SeatUnavailableException, DatabaseException {
        try {
            Seat seat = seatDAO.lockSeatForUpdate(seatId);

            if (seat == null) {
                throw new DatabaseException("Ghe khong ton tai");
            }

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatUnavailableException(
                        seatId,
                        "Ghe " + seat.getSeatNumber() + " da duoc dat"
                );
            }

            boolean success = seatDAO.updateSeatStatus(seatId, SeatStatus.RESERVED);

            if (!success) {
                throw new SeatUnavailableException(
                        seatId,
                        "Khong the dat ghe " + seat.getSeatNumber()
                );
            }

            return true;

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi dat ghe", e);
        }
    }

    // Huy dat ghe tam thoi
    public boolean releaseSeat(int seatId) throws DatabaseException {
        try {
            Seat seat = seatDAO.findById(seatId);

            if (seat == null) {
                throw new DatabaseException("Ghe khong ton tai");
            }

            if (seat.getStatus() != SeatStatus.RESERVED) {
                throw new DatabaseException("Ghe khong o trang thai tam giu");
            }

            return seatDAO.releaseSeat(seatId);

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi huy dat ghe", e);
        }
    }

    // Xac nhan dat ghe (RESERVED -> BOOKED)
    public boolean confirmSeat(int seatId) throws DatabaseException {
        try {
            Seat seat = seatDAO.findById(seatId);

            if (seat == null) {
                throw new DatabaseException("Ghe khong ton tai");
            }

            if (seat.getStatus() != SeatStatus.RESERVED) {
                throw new DatabaseException("Ghe phai o trang thai tam giu");
            }

            return seatDAO.updateSeatStatus(seatId, SeatStatus.BOOKED);

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi xac nhan ghe", e);
        }
    }
}
