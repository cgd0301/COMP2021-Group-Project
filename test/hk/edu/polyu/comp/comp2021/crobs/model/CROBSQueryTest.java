package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CROBSQueryTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 5);
    private static final String SUPERADMIN = "S000000";
    private static final String AMY = "S000001";
    private static final String BOB = "S000002";

    private CROBS crobs;

    @BeforeEach
    void setUp() {
        crobs = new CROBS(new MutableClock(LocalDate.of(2026, 10, 1), LocalTime.of(8, 0)));
        crobs.login(SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 30, features("PC", "PROJECTOR"));
        crobs.addRoom(RoomType.MEETINGROOM, 10, features("PROJECTOR"));
        crobs.addRoom(RoomType.LECTURETHEATRE, 50, features());
        crobs.addRoom(RoomType.SEMINARROOM, 30, features("PC", "PROJECTOR", "SPEAKER"));
        crobs.addUser("Amy");
        crobs.addUser("Bob");
        crobs.logout();
    }

    @Test
    void searchWithoutFeaturesFiltersOnlyCapacityAndSortsByRoomID() {
        crobs.login(AMY);
        assertEquals(Arrays.asList("R000", "R001", "R002", "R003"), search(1, features()));
        assertEquals(Arrays.asList("R000", "R002", "R003"), search(30, features()));
        assertEquals(Arrays.asList("R002"), search(31, features()));
        assertTrue(search(51, features()).isEmpty());
    }

    @Test
    void searchRequiresAllFeatures() {
        crobs.login(AMY);
        assertEquals(Arrays.asList("R000", "R001", "R003"), search(1, features("PROJECTOR")));
        assertEquals(Arrays.asList("R000", "R003"), search(1, features("PC", "PROJECTOR")));
        assertEquals(Arrays.asList("R003"), search(1, features("SPEAKER", "PC")));
        assertTrue(search(1, features("WHITEBOARD")).isEmpty());
        assertTrue(search(40, features("PC")).isEmpty());
    }

    @Test
    void searchExcludesRoomsBookedDuringTimeslot() {
        crobs.login(AMY);
        crobs.book("R000", DAY, LocalTime.of(9, 0), LocalTime.of(11, 0), "");
        crobs.book("R003", DAY, LocalTime.of(10, 30), LocalTime.of(10, 45), "");
        crobs.book("R002", DAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "");
        crobs.book("R001", DAY.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), "");

        assertEquals(Arrays.asList("R001", "R002"), search(1, features()));
        assertEquals(Arrays.asList("R001"), search(1, features("PROJECTOR")));
    }

    @Test
    void searchRejectsInvalidTimeAndGuest() {
        CrobsException guest = assertThrows(CrobsException.class, () -> search(1, features()));
        assertEquals("Not yet logged in", guest.getMessage());

        crobs.login(AMY);
        CrobsException invalid = assertThrows(CrobsException.class, () -> crobs.search(DAY,
                LocalTime.of(11, 0), LocalTime.of(10, 0), 1, features()));
        assertEquals("Invalid time: start time must be earlier than end time", invalid.getMessage());
    }

    @Test
    void scheduleCoversSevenDaysSortedByDateAndStartTime() {
        crobs.login(SUPERADMIN);
        crobs.book("R000", DAY.minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), "before");
        String firstAfternoon = crobs.book("R000", DAY, LocalTime.of(14, 0), LocalTime.of(15, 0), "");
        String firstMorning = crobs.book("R000", DAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "");
        String lastDay = crobs.book("R000", DAY.plusDays(6), LocalTime.of(20, 0), LocalTime.of(21, 0), "");
        crobs.book("R000", DAY.plusDays(7), LocalTime.of(9, 0), LocalTime.of(10, 0), "after");
        crobs.book("R001", DAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "other room");

        Map<Booking, String> schedule = crobs.schedule("R000", DAY);
        List<String> bookingIDs = new ArrayList<>();
        for (Booking booking : schedule.keySet()) {
            bookingIDs.add(booking.getBookingID());
        }
        assertEquals(Arrays.asList(firstMorning, firstAfternoon, lastDay), bookingIDs);
    }

    @Test
    void scheduleOfRoomWithoutBookingsIsEmpty() {
        crobs.login(AMY);
        assertTrue(crobs.schedule("R002", DAY).isEmpty());
    }

    @Test
    void adminsSeeEveryOwner() {
        String amyBooking = bookAs(AMY, LocalTime.of(9, 0));
        String bobBooking = bookAs(BOB, LocalTime.of(14, 0));

        switchTo(SUPERADMIN);
        Map<String, String> owners = owners(crobs.schedule("R000", DAY));
        assertEquals(AMY, owners.get(amyBooking));
        assertEquals(BOB, owners.get(bobBooking));

        crobs.changeRole(BOB, UserRole.ADMIN);
        switchTo(BOB);
        owners = owners(crobs.schedule("R000", DAY));
        assertEquals(AMY, owners.get(amyBooking));
        assertEquals(BOB, owners.get(bobBooking));
    }

    @Test
    void regularUsersSeeOnlyTheirOwnUserID() {
        String amyBooking = bookAs(AMY, LocalTime.of(9, 0));
        String bobBooking = bookAs(BOB, LocalTime.of(14, 0));

        switchTo(AMY);
        Map<String, String> owners = owners(crobs.schedule("R000", DAY));
        assertEquals(2, owners.size());
        assertEquals(AMY, owners.get(amyBooking));
        assertEquals("", owners.get(bobBooking));
    }

    @Test
    void scheduleRejectsUnknownRoomAndGuest() {
        CrobsException guest = assertThrows(CrobsException.class, () -> crobs.schedule("R000", DAY));
        assertEquals("Not yet logged in", guest.getMessage());

        crobs.login(AMY);
        CrobsException unknown = assertThrows(CrobsException.class, () -> crobs.schedule("R999", DAY));
        assertEquals("Input target room ID not found", unknown.getMessage());
    }

    private String bookAs(String userID, LocalTime startTime) {
        switchTo(userID);
        return crobs.book("R000", DAY, startTime, startTime.plusHours(1), "");
    }

    private Map<String, String> owners(Map<Booking, String> schedule) {
        Map<String, String> owners = new HashMap<>();
        for (Map.Entry<Booking, String> entry : schedule.entrySet()) {
            owners.put(entry.getKey().getBookingID(), entry.getValue());
        }
        return owners;
    }

    private List<String> search(int minCapacity, Set<String> targetFeatures) {
        List<String> roomIDs = new ArrayList<>();
        for (Room room : crobs.search(DAY, LocalTime.of(10, 0), LocalTime.of(11, 0),
                minCapacity, targetFeatures)) {
            roomIDs.add(room.getRoomID());
        }
        return roomIDs;
    }

    private void switchTo(String userID) {
        if (crobs.currentUser != null) {
            crobs.logout();
        }
        crobs.login(userID);
    }

    private static Set<String> features(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }
}
