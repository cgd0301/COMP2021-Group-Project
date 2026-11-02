package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class CROBSTest {
    private CROBS crobs;

    @Test
    void testCROBSConstructor() {
        CROBS crobs = new CROBS();
        assertNotNull(crobs);
    }

    @BeforeEach
    void setUp() {
        crobs = new CROBS();
    }

    @AfterEach
    void logoutAfterEach() {
        if (crobs.currentUser != null) {
            crobs.logout();
        }
    }

    @Test
    void normalLogin() {
        crobs.login("S000000");
        assertDoesNotThrow(() -> crobs.checkLoggedIn());
        crobs.logout();
    }

    @Test
    void repeatedLogin() {
        crobs.login("S000000");
        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.login("S000000"));
        assertEquals("Already logged in", ex.getMessage());
    }

    @Test
    void invalidUserIDLogin() {
        CrobsException ex1 = assertThrows(CrobsException.class, () -> crobs.login("ncjnciouwne"));
        CrobsException ex2 = assertThrows(CrobsException.class, () -> crobs.login("s111111"));
        assertEquals("User ID not found", ex1.getMessage());
        assertEquals("User ID not found", ex2.getMessage());
    }

    @Test
    void normalLogout() {
        crobs.login("S000000");
        crobs.logout();
        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.logout());
        assertEquals("Not yet logged in", ex.getMessage());
    }

    @Test
    void invalidLoggingStatusWhenLogout() {
        CrobsException ex1 = assertThrows(CrobsException.class, () -> crobs.logout());
        crobs.login("S000000");
        crobs.logout();
        CrobsException ex2 = assertThrows(CrobsException.class, () -> crobs.logout());
        assertEquals("Not yet logged in", ex1.getMessage());
        assertEquals("Not yet logged in", ex2.getMessage());

    }

    @Test
    void addUserRejectGuest() {
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.addUser("Amy"));
        assertEquals("Not yet logged in", ex.getMessage());
    }

    @Test
    void addUserRejectRegular() {
        this.loginAs(UserRole.REGULAR);
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.addUser("Amy"));
        assertEquals("Insufficient permission", ex.getMessage());
    }

    @Test
    void addUserCreatesRegularUsersInOrder() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        crobs.addUser("Bob");
        User userA = crobs.findUser("S000001");
        User userB = crobs.findUser("S000002");
        assertEquals("Amy", userA.getName());
        assertEquals(UserRole.REGULAR, userA.getRole());
        assertEquals("Bob", userB.getName());
    }

    @Test
    void removeUserNormalCase() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        crobs.removeUser("S000001");
        assertNull(crobs.findUser("S000001"));
    }

    @Test
    void removeUserWithInsufficientPermission() {
        crobs.login("S000000");
        crobs.addUser("Amy");
        crobs.logout();
        this.loginAs(UserRole.REGULAR);
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.removeUser("S000001"));
        assertEquals("Insufficient permission", ex.getMessage());
    }

    @Test
    void removeUserNotExists() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        crobs.addUser("Bob");
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.removeUser("S000003"));
        assertEquals("Input target user ID not found", ex.getMessage());
    }

    @Test
    void removeSuperOrAdminUser() {
        this.loginAs(UserRole.SUPERADMIN);

        crobs.addUser("Amy");
        crobs.changeRole("S000001", UserRole.ADMIN);
        CrobsException ex1 = assertThrows(CrobsException.class,
                () -> crobs.removeUser("S000001"));
        crobs.addUser("Bob");
        crobs.changeRole("S000002", UserRole.SUPERADMIN);
        CrobsException ex2 = assertThrows(CrobsException.class,
                () -> crobs.removeUser("S000002"));

        assertEquals("Removing (super) admin users is not allowed",
                ex1.getMessage());
        assertEquals("Removing (super) admin users is not allowed",
                ex2.getMessage());
    }

    @Test
    void changeRoleNormalCase() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        crobs.changeRole("S000001", UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, crobs.findUser("S000001").getRole());
    }

    @Test
    void changeRoleWithInsufficientPermission() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Admin");
        crobs.addUser("Regular");
        crobs.addUser("Amy");
        crobs.changeRole("S000001", UserRole.ADMIN);
        crobs.logout();

        crobs.login("S000001");
        CrobsException ex1 = assertThrows(CrobsException.class,
                () -> crobs.changeRole("S000003", UserRole.ADMIN));
        assertEquals("Insufficient permission",
                ex1.getMessage());
        crobs.logout();

        crobs.login("S000002");
        CrobsException ex2 = assertThrows(CrobsException.class,
                () -> crobs.changeRole("S000003", UserRole.ADMIN));
        assertEquals("Insufficient permission",
                ex2.getMessage());
        crobs.logout();

        assertEquals(UserRole.REGULAR, crobs.findUser("S000003").getRole());
    }

    @Test
    void changeRoleOnSuperadmin() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        crobs.changeRole("S000001", UserRole.SUPERADMIN);
        assertEquals(UserRole.ADMIN, crobs.findUser("S000000").getRole());
        assertEquals(UserRole.SUPERADMIN, crobs.findUser("S000001").getRole());
    }

    @Test
    void changeRoleUserIDNotExist() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.changeRole("S000002", UserRole.ADMIN));
        assertEquals("Input target user ID not found", ex.getMessage());
    }

    @Test
    void changeOwnRole() {
        this.loginAs(UserRole.SUPERADMIN);
        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.changeRole("S000000", UserRole.ADMIN));
        assertEquals("You can not change your own role", ex.getMessage());
    }

    @Test
    void addRoomNormally() {
        this.loginAs(UserRole.SUPERADMIN);
        Set<String> features = createFeatures();

        crobs.addRoom(RoomType.COMPUTERLAB, 10, features);
        crobs.addRoom(RoomType.LECTURETHEATRE, 8, features);
        Room createdRoom1 = crobs.findRoom("R000");
        assertNotNull(createdRoom1);
        assertTrue(checkRoomAttributes(createdRoom1, "R000",
                RoomType.COMPUTERLAB, 10, features));

        Room createdRoom2 = crobs.findRoom("R001");
        assertNotNull(createdRoom2);
        assertTrue(checkRoomAttributes(createdRoom2, "R001",
                RoomType.LECTURETHEATRE, 8, features));
    }

    @Test
    void addRoomWithCapacityExceedsMax() {
        this.loginAs(UserRole.SUPERADMIN);
        Set<String> features = createFeatures();

        CrobsException ex1 = assertThrows(CrobsException.class,
                () -> crobs.addRoom(RoomType.COMPUTERLAB, 100000, features));
        CrobsException ex2 = assertThrows(CrobsException.class,
                () -> crobs.addRoom(RoomType.COMPUTERLAB, 99999999, features));
        CrobsException ex3 = assertThrows(CrobsException.class,
                () -> crobs.addRoom(RoomType.COMPUTERLAB, 0, features));
        CrobsException ex4 = assertThrows(CrobsException.class,
                () -> crobs.addRoom(RoomType.COMPUTERLAB, -1, features));

        String invalidMinCapacityOutput = "Invalid minCapacity paramter " +
                "(should be positive integer smaller or equal to 65535)";
        assertEquals(invalidMinCapacityOutput, ex1.getMessage());
        assertEquals(invalidMinCapacityOutput, ex2.getMessage());
        assertEquals(invalidMinCapacityOutput, ex3.getMessage());
        assertEquals(invalidMinCapacityOutput, ex4.getMessage());
    }

    @Test
    void addRoomWithInsufficientPermission() {
        this.loginAs(UserRole.REGULAR);
        Set<String> features = this.createFeatures();

        CrobsException ex = assertThrows(CrobsException.class,
                () -> crobs.addRoom(RoomType.COMPUTERLAB, 10, features));
        assertEquals("Insufficient permission", ex.getMessage());
    }

    @Test
    void modifyRoomNormally() {
        this.loginAs(UserRole.ADMIN);

        Set<String> features = this.createFeatures();
        Set<String> newFeatures = new HashSet<>();
        newFeatures.add("tidy");
        newFeatures.add("multi_media");

        crobs.addRoom(RoomType.COMPUTERLAB, 10, features);
        Room room = crobs.findRoom("R000");
        crobs.modifyRoomType("R000", RoomType.LECTURETHEATRE);
        crobs.modifyRoomCapacity("R000", 5);
        crobs.modifyRoomFeatures("R000", newFeatures);

        assertTrue(checkRoomAttributes(room, "R000",
                RoomType.LECTURETHEATRE, 5, newFeatures));
    }

    @Test
    void modifyRoomWithInsufficientPermission() {
        Set<String> newFeatures = new HashSet<>();
        newFeatures.add("tidy");
        newFeatures.add("multi_media");

        this.loginAs(UserRole.SUPERADMIN);
        Set<String> features = this.createFeatures();
        crobs.addRoom(RoomType.COMPUTERLAB, 10, features);
        crobs.logout();
        this.loginAs(UserRole.REGULAR);

        CrobsException ex1 = assertThrows(CrobsException.class,
                () -> crobs.modifyRoomType("R000", RoomType.LECTURETHEATRE));
        CrobsException ex2 = assertThrows(CrobsException.class,
                () -> crobs.modifyRoomCapacity("R000", 8));
        CrobsException ex3 = assertThrows(CrobsException.class,
                () -> crobs.modifyRoomFeatures("R000", newFeatures));

        assertEquals("Insufficient permission", ex1.getMessage());
        assertEquals("Insufficient permission", ex2.getMessage());
        assertEquals("Insufficient permission", ex3.getMessage());
    }

    @Test
    void modifyRoomForRoomIDNotExists() {
        Set<String> newFeatures = new HashSet<>();
        newFeatures.add("tidy");
        newFeatures.add("multi_media");

        this.loginAs(UserRole.SUPERADMIN);
        Set<String> features = createFeatures();
        crobs.addRoom(RoomType.COMPUTERLAB, 10, features);

        CrobsException ex1 = assertThrows(CrobsException.class,
                () -> crobs.modifyRoomType("R999", RoomType.LECTURETHEATRE));
        CrobsException ex2 = assertThrows(CrobsException.class,
                () -> crobs.modifyRoomCapacity("R999", 8));
        CrobsException ex3 = assertThrows(CrobsException.class,
                () -> crobs.modifyRoomFeatures("R999", newFeatures));

        assertEquals("Input target room ID not found", ex1.getMessage());
        assertEquals("Input target room ID not found", ex2.getMessage());
        assertEquals("Input target room ID not found", ex3.getMessage());
    }

    @Test
    void constructorWithClockCreatesSuperadmin() {
        crobs = new CROBS(Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC));
        User superadmin = crobs.findUser("S000000");
        assertEquals("superadmin", superadmin.getName());
        assertEquals(UserRole.SUPERADMIN, superadmin.getRole());
        crobs.login("S000000");
        assertEquals(UserRole.SUPERADMIN, crobs.getCurrentUserRole());
    }

    @Test
    void userAndRoomCommandsRejectGuest() {
        Set<String> features = createFeatures();
        String expected = "Not yet logged in";
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.removeUser("S000001")).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.changeRole("S000001", UserRole.ADMIN)).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.addRoom(RoomType.COMPUTERLAB, 10, features)).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.modifyRoomType("R000", RoomType.MEETINGROOM)).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.modifyRoomCapacity("R000", 5)).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.modifyRoomFeatures("R000", features)).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.deleteRoom("R000")).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.findRoom("R000")).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.getAllRooms()).getMessage());
    }

    @Test
    void addRoomReturnsGeneratedRoomIDs() {
        this.loginAs(UserRole.SUPERADMIN);
        assertEquals("R000", crobs.addRoom(RoomType.MEETINGROOM, 10, new HashSet<String>()));
        assertEquals("R001", crobs.addRoom(RoomType.SEMINARROOM, 20, createFeatures()));
        assertTrue(crobs.findRoom("R000").getFeatures().isEmpty());
    }

    @Test
    void modifyRoomCapacityRejectsInvalidCapacity() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        String expected = "Invalid minCapacity paramter " +
                "(should be positive integer smaller or equal to 65535)";

        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.modifyRoomCapacity("R000", 0)).getMessage());
        assertEquals(expected, assertThrows(CrobsException.class,
                () -> crobs.modifyRoomCapacity("R000", 65536)).getMessage());
        crobs.modifyRoomCapacity("R000", 65535);
        assertEquals(65535, crobs.findRoom("R000").getCapacity());
    }

    @Test
    void findRoomRejectsUnknownRoom() {
        this.loginAs(UserRole.SUPERADMIN);
        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.findRoom("R000"));
        assertEquals("Input target room ID not found", ex.getMessage());
    }

    @Test
    void getAllRoomsReturnsReadOnlyView() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.addRoom(RoomType.MEETINGROOM, 6, createFeatures());

        Map<String, Room> rooms = crobs.getAllRooms();
        assertEquals(2, rooms.size());
        assertTrue(rooms.containsKey("R000"));
        assertTrue(rooms.containsKey("R001"));
        assertThrows(UnsupportedOperationException.class, () -> rooms.remove("R000"));
        assertThrows(UnsupportedOperationException.class, rooms::clear);
    }

    @Test
    void checkHelpersRejectInvalidInput() {
        User superadmin = crobs.findUser("S000000");
        User regular = new User("Tester", "S999999", UserRole.REGULAR);
        assertDoesNotThrow(() -> crobs.checkAuthority(superadmin, UserRole.SUPERADMIN));
        CrobsException ex1 = assertThrows(CrobsException.class,
                () -> crobs.checkAuthority(regular, UserRole.ADMIN));
        assertEquals("Insufficient permission", ex1.getMessage());

        String invalidTime = "Invalid time: start time must be earlier than end time";
        assertDoesNotThrow(() -> crobs.checkTimeValidation(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        assertEquals(invalidTime, assertThrows(CrobsException.class,
                () -> crobs.checkTimeValidation(LocalTime.of(10, 0), LocalTime.of(10, 0))).getMessage());
        assertEquals(invalidTime, assertThrows(CrobsException.class,
                () -> crobs.checkTimeValidation(LocalTime.of(11, 0), LocalTime.of(10, 0))).getMessage());
    }

    @Test
    void getCurrentUserRoleFollowsLogin() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addUser("Amy");
        crobs.logout();
        crobs.login("S000001");
        assertEquals(UserRole.REGULAR, crobs.getCurrentUserRole());
    }

    @Test
    void deleteRoomWithoutBookings() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.deleteRoom("R000");

        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.findRoom("R000"));
        assertEquals("Input target room ID not found", ex.getMessage());
        assertTrue(crobs.getAllRooms().isEmpty());
        assertEquals("R001", crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures()));
    }

    @Test
    void deleteRoomRejectsUnknownRoomAndRegularUser() {
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        CrobsException ex1 = assertThrows(CrobsException.class, () -> crobs.deleteRoom("R999"));
        assertEquals("Input target room ID not found", ex1.getMessage());

        crobs.addUser("Amy");
        switchTo("S000001");
        CrobsException ex2 = assertThrows(CrobsException.class, () -> crobs.deleteRoom("R000"));
        assertEquals("Insufficient permission", ex2.getMessage());
        assertNotNull(crobs.findRoom("R000"));
    }

    @Test
    void deleteRoomCancelsOnlyFutureBookings() {
        LocalDate today = LocalDate.of(2026, 10, 1);
        MutableClock clock = new MutableClock(today, LocalTime.of(8, 0));
        crobs = new CROBS(clock);
        crobs.login("S000000");
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.addUser("Amy");
        switchTo("S000001");
        String finished = crobs.book("R000", today, LocalTime.of(8, 30), LocalTime.of(9, 0), "past");
        String ongoing = crobs.book("R000", today, LocalTime.of(9, 30), LocalTime.of(11, 0), "now");
        crobs.book("R000", today, LocalTime.of(14, 0), LocalTime.of(15, 0), "later");
        crobs.book("R000", today.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), "tomorrow");

        clock.setDateTime(today, LocalTime.of(10, 0));
        switchTo("S000000");
        crobs.deleteRoom("R000");

        switchTo("S000001");
        List<Booking> remaining = crobs.getBookings();
        assertEquals(2, remaining.size());
        assertEquals(finished, remaining.get(0).getBookingID());
        assertEquals(ongoing, remaining.get(1).getBookingID());
    }

    @Test
    void deletedRoomCannotBeUsedButPastBookingsCanBeDeleted() {
        LocalDate today = LocalDate.of(2026, 10, 1);
        MutableClock clock = new MutableClock(today, LocalTime.of(8, 0));
        crobs = new CROBS(clock);
        crobs.login("S000000");
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.addUser("Amy");
        switchTo("S000001");
        String first = crobs.book("R000", today, LocalTime.of(8, 30), LocalTime.of(9, 0), "past");
        String second = crobs.book("R000", today, LocalTime.of(9, 0), LocalTime.of(9, 30), "past");

        clock.setDateTime(today, LocalTime.of(12, 0));
        switchTo("S000000");
        crobs.deleteRoom("R000");

        switchTo("S000001");
        String notFound = "Input target room ID not found";
        assertEquals(notFound, assertThrows(CrobsException.class, () -> crobs.book("R000",
                today.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), "")).getMessage());
        assertEquals(notFound, assertThrows(CrobsException.class,
                () -> crobs.schedule("R000", today)).getMessage());
        assertEquals(notFound, assertThrows(CrobsException.class, () -> crobs.modifyBooking(first,
                "R000", today.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), "")).getMessage());

        crobs.deleteBooking(first);
        crobs.deleteBooking(second);
        assertTrue(crobs.getBookings().isEmpty());
    }

    @Test
    void removeUserCancelsAllTheirBookings() {
        LocalDate day = LocalDate.of(2026, 10, 5);
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.addUser("Amy");
        switchTo("S000001");
        crobs.book("R000", day, LocalTime.of(10, 0), LocalTime.of(12, 0), "a");
        crobs.book("R000", day.plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), "b");

        switchTo("S000000");
        assertEquals(2, crobs.schedule("R000", day).size());
        crobs.removeUser("S000001");

        assertTrue(crobs.schedule("R000", day).isEmpty());
        assertDoesNotThrow(() -> crobs.book("R000", day, LocalTime.of(10, 0), LocalTime.of(12, 0), ""));
        CrobsException ex = assertThrows(CrobsException.class, () -> crobs.login("S000001"));
        assertEquals("Already logged in", ex.getMessage());
        crobs.logout();
        assertEquals("User ID not found",
                assertThrows(CrobsException.class, () -> crobs.login("S000001")).getMessage());
    }

    @Test
    void specificUserCanBookAndBeRemoved() {
        LocalDate day = LocalDate.of(2026, 10, 5);
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.logout();

        this.loginAs(UserRole.REGULAR);
        String bookingID = crobs.book("R000", day, LocalTime.of(10, 0), LocalTime.of(12, 0), "");
        assertEquals(1, crobs.getBookings().size());
        assertEquals("S999999", crobs.getBookings().get(0).getUserID());

        switchTo("S000000");
        crobs.removeUser("S999999");
        assertNull(crobs.findUser("S999999"));
        assertTrue(crobs.schedule("R000", day).isEmpty());
        assertEquals("Input target booking ID not found",
                assertThrows(CrobsException.class, () -> crobs.deleteBooking(bookingID)).getMessage());
    }

    @Test
    void addSpecificUserKeepsExistingBookings() {
        LocalDate day = LocalDate.of(2026, 10, 5);
        this.loginAs(UserRole.SUPERADMIN);
        crobs.addRoom(RoomType.COMPUTERLAB, 10, createFeatures());
        crobs.logout();
        this.loginAs(UserRole.ADMIN);
        crobs.book("R000", day, LocalTime.of(10, 0), LocalTime.of(12, 0), "");

        crobs.addSpecificUser(crobs.currentUser);
        assertEquals(1, crobs.getBookings().size());
    }

    private void switchTo(String userID) {
        if (crobs.currentUser != null) {
            crobs.logout();
        }
        crobs.login(userID);
    }

    private boolean checkRoomAttributes(Room room, String roomID, RoomType roomType, int capacity, Set<String> features) {
        boolean isWrong = room.getRoomType() != roomType || room.getCapacity() != capacity
                || !roomID.equals(room.getRoomID()) || !features.equals(room.getFeatures());
        return !isWrong;
    }

    private Set<String> createFeatures() {
        Set<String> features = new HashSet<>();
        features.add("large");
        features.add("spacious");
        features.add("cozy");
        return features;
    }

    private void loginAs(UserRole role) {
        if (role != UserRole.SUPERADMIN ){
            crobs.currentUser = new User("Tester", "S999999", role);
            crobs.addSpecificUser(crobs.currentUser);
        } else {
            crobs.login("S000000");
        }
    }
}