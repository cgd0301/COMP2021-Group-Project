package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
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