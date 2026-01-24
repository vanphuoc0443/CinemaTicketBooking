package model;

/**
 * Class test các enum
 */
public class EnumTest {
    public static void main(String[] args) {
        System.out.println("========== TEST SEAT TYPE ==========");

        // Test SeatType
        for (SeatType type : SeatType.values()) {
            System.out.println(type.name() + ": " + type.toString());
            System.out.println("  Price: " + type.getPrice());
            System.out.println("  Formatted: " + type.getFormattedPrice());
        }

        // Test fromString
        SeatType vip = SeatType.fromString("VIP");
        System.out.println("\nParsed 'VIP': " + vip + " - " + vip.getPrice());

        System.out.println("\n========== TEST SEAT STATUS ==========");

        // Test SeatStatus
        for (SeatStatus status : SeatStatus.values()) {
            System.out.println(status.name() + ": " + status.toString());
            System.out.println("  Description: " + status.getDescription());
            System.out.println("  Is Available: " + status.isAvailable());
        }

        // Test transitions
        System.out.println("\nStatus Transitions:");
        SeatStatus available = SeatStatus.AVAILABLE;
        System.out.println("AVAILABLE -> RESERVED: " +
                available.canTransitionTo(SeatStatus.RESERVED));
        System.out.println("AVAILABLE -> BOOKED: " +
                available.canTransitionTo(SeatStatus.BOOKED));
        System.out.println("AVAILABLE -> AVAILABLE: " +
                available.canTransitionTo(SeatStatus.AVAILABLE));

        SeatStatus reserved = SeatStatus.RESERVED;
        System.out.println("\nRESERVED -> BOOKED: " +
                reserved.canTransitionTo(SeatStatus.BOOKED));
        System.out.println("RESERVED -> AVAILABLE: " +
                reserved.canTransitionTo(SeatStatus.AVAILABLE));

        System.out.println("\n========== TEST BOOKING STATUS ==========");

        // Test BookingStatus
        for (BookingStatus status : BookingStatus.values()) {
            System.out.println(status.name() + ": " + status.toString());
            System.out.println("  Description: " + status.getDescription());
            System.out.println("  Color: " + status.getDisplayColor());
            System.out.println("  Can be cancelled: " + status.canBeCancelled());
            System.out.println("  Can be confirmed: " + status.canBeConfirmed());
        }

        // Test transitions
        System.out.println("\nBooking Status Transitions:");
        BookingStatus pending = BookingStatus.PENDING;
        System.out.println("PENDING -> CONFIRMED: " +
                pending.canTransitionTo(BookingStatus.CONFIRMED));
        System.out.println("PENDING -> CANCELLED: " +
                pending.canTransitionTo(BookingStatus.CANCELLED));

        BookingStatus confirmed = BookingStatus.CONFIRMED;
        System.out.println("\nCONFIRMED -> CANCELLED: " +
                confirmed.canTransitionTo(BookingStatus.CANCELLED));
        System.out.println("CONFIRMED -> PENDING: " +
                confirmed.canTransitionTo(BookingStatus.PENDING));

        BookingStatus cancelled = BookingStatus.CANCELLED;
        System.out.println("\nCANCELLED -> CONFIRMED: " +
                cancelled.canTransitionTo(BookingStatus.CONFIRMED));

        System.out.println("\n========== ALL TESTS COMPLETED ==========");
    }
}