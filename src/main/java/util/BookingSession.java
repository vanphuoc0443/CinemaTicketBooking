package util;

import java.util.*;

/**
 * Carries booking context between screens:
 * showtimeId, sessionToken, selected DB seatIds.
 */
public class BookingSession {

    private static int showtimeId = 1; // demo default
    private static String sessionToken;
    private static List<Integer> selectedSeatIds = new ArrayList<>();
    private static Map<String, Integer> seatNumberToId = new HashMap<>();

    public static void start(int showtimeId) {
        BookingSession.showtimeId = showtimeId;
        BookingSession.sessionToken = UUID.randomUUID().toString();
        BookingSession.selectedSeatIds.clear();
        BookingSession.seatNumberToId.clear();
    }

    public static int getShowtimeId() {
        return showtimeId;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static List<Integer> getSelectedSeatIds() {
        return selectedSeatIds;
    }

    public static void addSeatId(int seatId) {
        if (!selectedSeatIds.contains(seatId))
            selectedSeatIds.add(seatId);
    }

    public static void removeSeatId(int seatId) {
        selectedSeatIds.remove(Integer.valueOf(seatId));
    }

    public static void mapSeatNumber(String seatNumber, int seatId) {
        seatNumberToId.put(seatNumber, seatId);
    }

    public static Integer getSeatIdByNumber(String seatNumber) {
        return seatNumberToId.get(seatNumber);
    }

    public static void clear() {
        sessionToken = null;
        selectedSeatIds.clear();
        seatNumberToId.clear();
    }
}
