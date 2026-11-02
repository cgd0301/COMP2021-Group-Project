package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CROBSBookingTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);
    private static final LocalDate DAY = LocalDate.of(2026, 10, 5);
    private static final String SUPERADMIN = "S000000";
    private static final String AMY = "S000001";
    private static final String BOB = "S000002";
    private static final String OVERLAP = "Booking time overlaps with an existing reservation";
    private static final String BOOKING_NOT_FOUND = "Input target booking ID not found";
    private static final String ROOM_NOT_FOUND = "Input target room ID not found";
    private static final String INVALID_TIME = "Invalid time: start time must be earlier than end time";

    private MutableClock clock;
    private CROBS crobs;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(TODAY, LocalTime.of(8, 0));
        crobs = new CROBS(clock);
        crobs.login(SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 30, new HashSet<String>());
        crobs.addRoom(RoomType.MEETINGROOM, 10, new HashSet<String>());
        crobs.addUser("Amy");
        crobs.addUser("Bob");
        crobs.logout();
    }

    @Test
    void bookCreatesBookingsWithSequentialIDs() {
        crobs.login(AMY);
        String first = crobs.book("R000", DAY, time(10), time(12), "group meeting");
        String second = crobs.book("R001", DAY, time(10), time(12), "");

        assertEquals("B000000", first);
        assertEquals("B000001", second);
        Booking booking = crobs.getBookings().get(0);
        assertEquals(first, booking.getBookingID());
        assertEquals(AMY, booking.getUserID());
        assertEquals("R000", booking.getRoomID());
        assertEquals(DAY, booking.getDate());
        assertEquals(time(10), booking.getStartTime());
        assertEquals(time(12), booking.getEndTime());
        assertEquals("group meeting", booking.getPurpose());
    }

    @Test
    void bookRejectsGuest() {
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.book("R000", DAY, time(10), time(12), ""));
        assertEquals("Not yet logged in", ex.getMessage());
    }

    @Test
    void bookRejectsInvalidTimeRange() {
        crobs.login(AMY);
        assertEquals(INVALID_TIME, assertThrows(CrobsException.class,
                () -> crobs.book("R000", DAY, time(10), time(10), "")).getMessage());
        assertEquals(INVALID_TIME, assertThrows(CrobsException.class,
                () -> crobs.book("R000", DAY, time(12), time(10), "")).getMessage());
        assertTrue(crobs.getBookings().isEmpty());
    }

    @Test
    void bookRejectsUnknownRoom() {
        crobs.login(AMY);
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.book("R999", DAY, time(10), time(12), ""));
        assertEquals(ROOM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void bookRejectsOverlapWithAnotherUsersBooking() {
        crobs.login(AMY);
        crobs.book("R000", DAY, time(10), time(12), "");
        switchTo(BOB);

        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.book("R000", DAY, time(11), time(13), ""));
        assertEquals(OVERLAP, ex.getMessage());
        assertTrue(crobs.getBookings().isEmpty());
    }

    @Test
    void bookRejectsOverlapWithOwnBooking() {
        crobs.login(AMY);
        crobs.book("R000", DAY, time(10), time(12), "");
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.book("R000", DAY, time(9), time(13), ""));
        assertEquals(OVERLAP, ex.getMessage());
        assertEquals(1, crobs.getBookings().size());
    }

    @Test
    void bookAllowsAdjacentOtherRoomAndOtherDate() {
        crobs.login(AMY);
        crobs.book("R000", DAY, time(10), time(12), "");
        switchTo(BOB);

        assertDoesNotThrow(() -> crobs.book("R000", DAY, time(8), time(10), "before"));
        assertDoesNotThrow(() -> crobs.book("R000", DAY, time(12), time(14), "after"));
        assertDoesNotThrow(() -> crobs.book("R001", DAY, time(10), time(12), "other room"));
        assertDoesNotThrow(() -> crobs.book("R000", DAY.plusDays(1), time(10), time(12), "other day"));
        assertEquals(4, crobs.getBookings().size());
    }

    @Test
    void getBookingsIsSortedByDateAndStartTimeAndOnlyOwn() {
        crobs.login(AMY);
        String later = crobs.book("R000", DAY.plusDays(2), time(9), time(10), "");
        String afternoon = crobs.book("R000", DAY, time(14), time(15), "");
        String morning = crobs.book("R001", DAY, time(9), time(10), "");
        switchTo(BOB);
        crobs.book("R000", DAY, time(10), time(11), "");
        switchTo(AMY);

        List<Booking> bookings = crobs.getBookings();
        assertEquals(3, bookings.size());
        assertEquals(morning, bookings.get(0).getBookingID());
        assertEquals(afternoon, bookings.get(1).getBookingID());
        assertEquals(later, bookings.get(2).getBookingID());
        for (Booking booking : bookings) {
            assertEquals(AMY, booking.getUserID());
        }
    }

    @Test
    void getBookingsRejectsGuest() {
        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.getBookings());
        assertEquals("Not yet logged in", ex.getMessage());
    }

    @Test
    void deleteBookingFreesTheTimeslot() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "");
        crobs.deleteBooking(bookingID);

        assertTrue(crobs.getBookings().isEmpty());
        switchTo(BOB);
        assertDoesNotThrow(() -> crobs.book("R000", DAY, time(10), time(12), ""));
    }

    @Test
    void deleteBookingRejectsUnknownOrDeletedBooking() {
        crobs.login(AMY);
        assertEquals(BOOKING_NOT_FOUND, assertThrows(CrobsException.class,
                () -> crobs.deleteBooking("B999999")).getMessage());
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "");
        crobs.deleteBooking(bookingID);
        assertEquals(BOOKING_NOT_FOUND, assertThrows(CrobsException.class,
                () -> crobs.deleteBooking(bookingID)).getMessage());
    }

    @Test
    void deleteBookingHidesOtherUsersBooking() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "");
        switchTo(SUPERADMIN);

        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.deleteBooking(bookingID));
        assertEquals(BOOKING_NOT_FOUND, ex.getMessage());
        switchTo(AMY);
        assertEquals(1, crobs.getBookings().size());
    }

    @Test
    void deleteBookingRejectsGuest() {
        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.deleteBooking("B000000"));
        assertEquals("Not yet logged in", ex.getMessage());
    }

    @Test
    void modifyBookingInSameRoomIgnoresItself() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        crobs.modifyBooking(bookingID, "R000", DAY, time(10), time(11), "new");

        Booking booking = crobs.getBookings().get(0);
        assertEquals(bookingID, booking.getBookingID());
        assertEquals(AMY, booking.getUserID());
        assertEquals(time(11), booking.getEndTime());
        assertEquals("new", booking.getPurpose());
        switchTo(BOB);
        assertDoesNotThrow(() -> crobs.book("R000", DAY, time(11), time(12), ""));
    }

    @Test
    void modifyBookingMovesBookingToAnotherRoom() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        crobs.modifyBooking(bookingID, "R001", DAY.plusDays(1), time(14), time(15), "moved");

        Booking booking = crobs.getBookings().get(0);
        assertEquals(bookingID, booking.getBookingID());
        assertEquals(AMY, booking.getUserID());
        assertEquals("R001", booking.getRoomID());
        assertEquals(DAY.plusDays(1), booking.getDate());
        assertEquals(time(14), booking.getStartTime());
        assertEquals(time(15), booking.getEndTime());
        assertEquals("moved", booking.getPurpose());

        switchTo(SUPERADMIN);
        assertTrue(crobs.schedule("R000", DAY).isEmpty());
        Map<Booking, String> newRoomSchedule = crobs.schedule("R001", DAY);
        assertEquals(1, newRoomSchedule.size());
        assertSame(booking, newRoomSchedule.keySet().iterator().next());
        assertDoesNotThrow(() -> crobs.book("R000", DAY, time(10), time(12), ""));
        assertEquals(OVERLAP, assertThrows(CrobsException.class,
                () -> crobs.book("R001", DAY.plusDays(1), time(14), time(15), "")).getMessage());
    }

    @Test
    void modifyBookingRejectsOverlapInSameRoomAndLeavesBookingUnchanged() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        switchTo(BOB);
        crobs.book("R000", DAY, time(13), time(14), "");
        switchTo(AMY);

        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R000", DAY, time(11), time(13, 30), "new"));
        assertEquals(OVERLAP, ex.getMessage());
        assertUnchanged(crobs.getBookings().get(0), "R000", DAY, time(10), time(12), "old");
    }

    @Test
    void modifyBookingRejectsOverlapInNewRoomAndLeavesBookingUnchanged() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        switchTo(BOB);
        crobs.book("R001", DAY, time(10), time(12), "");
        switchTo(AMY);

        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R001", DAY, time(11), time(12), "new"));
        assertEquals(OVERLAP, ex.getMessage());
        assertUnchanged(crobs.getBookings().get(0), "R000", DAY, time(10), time(12), "old");
        switchTo(SUPERADMIN);
        assertEquals(1, crobs.schedule("R000", DAY).size());
        assertEquals(1, crobs.schedule("R001", DAY).size());
    }

    @Test
    void modifyBookingRejectsUnknownRoomOrBooking() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");

        assertEquals(ROOM_NOT_FOUND, assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R999", DAY, time(10), time(12), "")).getMessage());
        assertEquals(BOOKING_NOT_FOUND, assertThrows(CrobsException.class,
                () -> crobs.modifyBooking("B999999", "R000", DAY, time(10), time(12), "")).getMessage());
        assertUnchanged(crobs.getBookings().get(0), "R000", DAY, time(10), time(12), "old");
    }

    @Test
    void modifyBookingHidesOtherUsersBooking() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        switchTo(BOB);

        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R000", DAY, time(14), time(15), "mine"));
        assertEquals(BOOKING_NOT_FOUND, ex.getMessage());
        switchTo(AMY);
        assertUnchanged(crobs.getBookings().get(0), "R000", DAY, time(10), time(12), "old");
    }

    @Test
    void modifyBookingRejectsPastTarget() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        String expected = "Booking can not be modified to a past date/time";

        assertEquals(expected, assertThrows(CrobsException.class, () -> crobs.modifyBooking(bookingID,
                "R000", TODAY.minusDays(1), time(10), time(12), "")).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class, () -> crobs.modifyBooking(bookingID,
                "R000", TODAY, time(7), time(9), "")).getMessage());
        assertDoesNotThrow(() -> crobs.modifyBooking(bookingID, "R000", TODAY, time(8), time(9), "today"));
    }

    @Test
    void modifyBookingRejectsStartedBooking() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        clock.setDateTime(DAY, LocalTime.of(10, 30));

        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R000", DAY.plusDays(1), time(10), time(12), "new"));
        assertEquals("Modifying previous booking is not allowed", ex.getMessage());
        assertUnchanged(crobs.getBookings().get(0), "R000", DAY, time(10), time(12), "old");
    }

    @Test
    void modifyBookingRejectsInvalidTimeAndGuest() {
        crobs.login(AMY);
        String bookingID = crobs.book("R000", DAY, time(10), time(12), "old");
        assertEquals(INVALID_TIME, assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R000", DAY, time(12), time(11), "")).getMessage());
        crobs.logout();
        assertEquals("Not yet logged in", assertThrows(CrobsException.class,
                () -> crobs.modifyBooking(bookingID, "R000", DAY, time(10), time(11), "")).getMessage());
    }

    private void assertUnchanged(Booking booking, String roomID, LocalDate date,
                                 LocalTime startTime, LocalTime endTime, String purpose) {
        assertEquals(roomID, booking.getRoomID());
        assertEquals(date, booking.getDate());
        assertEquals(startTime, booking.getStartTime());
        assertEquals(endTime, booking.getEndTime());
        assertEquals(purpose, booking.getPurpose());
    }

    private void switchTo(String userID) {
        if (crobs.currentUser != null) {
            crobs.logout();
        }
        crobs.login(userID);
    }

    private static LocalTime time(int hour) {
        return LocalTime.of(hour, 0);
    }

    private static LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }
}
