package model;

import java.sql.Timestamp;

/**
 * Model để quản lý việc tạm giữ ghế (lock)
 * Khi user chọn ghế, ghế sẽ bị lock trong 10 phút
 * Nếu không thanh toán, ghế sẽ tự động unlock
 */
public class SeatLock {
    private int lockId;
    private int seatId;
    private int showtimeId;
    private int customerId;
    private Timestamp lockedAt;
    private Timestamp expiresAt;
    private String sessionToken;
    private boolean isActive;

    // Lock duration in minutes
    public static final int LOCK_DURATION_MINUTES = 10;

    public SeatLock() {
        this.isActive = true;
    }

    public SeatLock(int seatId, int showtimeId, int customerId, String sessionToken) {
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.customerId = customerId;
        this.sessionToken = sessionToken;
        this.isActive = true;

        // Set expiry time
        long now = System.currentTimeMillis();
        this.lockedAt = new Timestamp(now);
        this.expiresAt = new Timestamp(now + (LOCK_DURATION_MINUTES * 60 * 1000));
    }

    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Timestamp getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Timestamp lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Kiểm tra lock còn hiệu lực không
     */
    public boolean isExpired() {
        if (!isActive) {
            return true;
        }
        return expiresAt != null && expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Lấy số giây còn lại
     */
    public long getRemainingSeconds() {
        if (expiresAt == null) {
            return 0;
        }
        long remaining = (expiresAt.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    @Override
    public String toString() {
        return "SeatLock{" +
                "lockId=" + lockId +
                ", seatId=" + seatId +
                ", showtimeId=" + showtimeId +
                ", customerId=" + customerId +
                ", expiresAt=" + expiresAt +
                ", isActive=" + isActive +
                ", expired=" + isExpired() +
                '}';
    }
}