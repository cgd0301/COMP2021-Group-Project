package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 5);
    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking("B000000", "S000001", "R000",
                DAY, LocalTime.of(10, 0), LocalTime.of(12, 0), "meeting");
    }

    @Test
    void gettersReturnConstructorValues() {
        assertEquals("B000000", booking.getBookingID());
        assertEquals("S000001", booking.getUserID());
        assertEquals("R000", booking.getRoomID());
        assertEquals(DAY, booking.getDate());
        assertEquals(LocalTime.of(10, 0), booking.getStartTime());
        assertEquals(LocalTime.of(12, 0), booking.getEndTime());
        assertEquals("meeting", booking.getPurpose());
    }

    @Test
    void settersUpdateMutableFields() {
        LocalDate newDay = DAY.plusDays(1);
        booking.setRoomID("R001");
        booking.setDate(newDay);
        booking.setStartTime(LocalTime.of(8, 0));
        booking.setEndTime(LocalTime.of(9, 30));
        booking.setPurpose("lecture");

        assertEquals("R001", booking.getRoomID());
        assertEquals(newDay, booking.getDate());
        assertEquals(LocalTime.of(8, 0), booking.getStartTime());
        assertEquals(LocalTime.of(9, 30), booking.getEndTime());
        assertEquals("lecture", booking.getPurpose());
        assertEquals("B000000", booking.getBookingID());
        assertEquals("S000001", booking.getUserID());
    }

    @Test
    void separateIntervalsDoNotConflict() {
        assertFalse(conflicts(7, 9));
        assertFalse(conflicts(13, 15));
    }

    @Test
    void adjacentIntervalsDoNotConflict() {
        assertFalse(conflicts(8, 10));
        assertFalse(conflicts(12, 14));
    }

    @Test
    void partiallyOverlappingIntervalsConflict() {
        assertTrue(conflicts(9, 11));
        assertTrue(conflicts(11, 13));
    }

    @Test
    void containingAndContainedIntervalsConflict() {
        assertTrue(conflicts(9, 13));
        assertTrue(booking.isConflict(DAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
    }

    @Test
    void identicalIntervalConflicts() {
        assertTrue(conflicts(10, 12));
    }

    @Test
    void sameTimeOnDifferentDateDoesNotConflict() {
        assertFalse(booking.isConflict(DAY.plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void conflictWithAnotherBooking() {
        Booking overlapping = new Booking("B000001", "S000002", "R001",
                DAY, LocalTime.of(11, 0), LocalTime.of(13, 0), "");
        Booking adjacent = new Booking("B000002", "S000002", "R000",
                DAY, LocalTime.of(12, 0), LocalTime.of(13, 0), "");
        assertTrue(booking.isConflict(overlapping));
        assertFalse(booking.isConflict(adjacent));
    }

    @Test
    void sameRoomComparesRoomID() {
        Booking sameRoom = new Booking("B000001", "S000002", "R000",
                DAY, LocalTime.of(14, 0), LocalTime.of(15, 0), "");
        Booking otherRoom = new Booking("B000002", "S000002", "R001",
                DAY, LocalTime.of(10, 0), LocalTime.of(12, 0), "");
        assertTrue(booking.isSameRoom(sameRoom));
        assertFalse(booking.isSameRoom(otherRoom));
    }

    private boolean conflicts(int startHour, int endHour) {
        return booking.isConflict(DAY, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }
}
