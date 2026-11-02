package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomUserEnumTest {

    @Test
    void roomCopiesFeaturesOnConstruction() {
        Set<String> features = features("PROJECTOR", "WHITEBOARD");
        Room room = new Room("R000", RoomType.MEETINGROOM, 20, features);
        features.add("SPEAKER");

        assertEquals("R000", room.getRoomID());
        assertEquals(RoomType.MEETINGROOM, room.getRoomType());
        assertEquals(20, room.getCapacity());
        assertEquals(features("PROJECTOR", "WHITEBOARD"), room.getFeatures());
    }

    @Test
    void roomFeaturesCannotBeModifiedFromOutside() {
        Room room = new Room("R000", RoomType.MEETINGROOM, 20, features("PROJECTOR"));
        assertThrows(UnsupportedOperationException.class, () -> room.getFeatures().clear());
        assertThrows(UnsupportedOperationException.class, () -> room.getFeatures().add("SPEAKER"));
    }

    @Test
    void roomSettersReplaceValues() {
        Room room = new Room("R000", RoomType.MEETINGROOM, 20, features("PROJECTOR"));
        Set<String> newFeatures = features("SPEAKER");
        room.setRoomType(RoomType.SEMINARROOM);
        room.setRoomCapacity(45);
        room.setRoomFeatures(newFeatures);
        newFeatures.add("PROJECTOR");

        assertEquals(RoomType.SEMINARROOM, room.getRoomType());
        assertEquals(45, room.getCapacity());
        assertEquals(features("SPEAKER"), room.getFeatures());
    }

    @Test
    void satisfiesChecksCapacityBoundary() {
        Room room = new Room("R000", RoomType.COMPUTERLAB, 30, features("PC"));
        assertTrue(room.satisfies(29, features()));
        assertTrue(room.satisfies(30, features()));
        assertFalse(room.satisfies(31, features()));
    }

    @Test
    void satisfiesRequiresAllFeatures() {
        Room room = new Room("R000", RoomType.COMPUTERLAB, 30, features("PC", "PROJECTOR"));
        assertTrue(room.satisfies(1, features("PC")));
        assertTrue(room.satisfies(1, features("PC", "PROJECTOR")));
        assertFalse(room.satisfies(1, features("PC", "SPEAKER")));
    }

    @Test
    void satisfiesIgnoresMissingFeatureFilter() {
        Room room = new Room("R000", RoomType.COMPUTERLAB, 30, features());
        assertTrue(room.satisfies(1, null));
        assertTrue(room.satisfies(1, features()));
        assertFalse(room.satisfies(31, null));
    }

    @Test
    void userGettersAndRoleChange() {
        User user = new User("Amy", "S000001", UserRole.REGULAR);
        assertEquals("Amy", user.getName());
        assertEquals("S000001", user.getUserID());
        assertEquals(UserRole.REGULAR, user.getRole());

        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void userCompareFollowsPermissionLevels() {
        User regular = new User("R", "S000001", UserRole.REGULAR);
        User admin = new User("A", "S000002", UserRole.ADMIN);
        User superadmin = new User("S", "S000000", UserRole.SUPERADMIN);

        assertTrue(regular.compare(UserRole.REGULAR));
        assertFalse(regular.compare(UserRole.ADMIN));
        assertTrue(admin.compare(UserRole.ADMIN));
        assertFalse(admin.compare(UserRole.SUPERADMIN));
        assertTrue(superadmin.compare(UserRole.ADMIN));
        assertTrue(superadmin.compare(UserRole.SUPERADMIN));
    }

    @Test
    void userRoleNamesAndLevels() {
        assertEquals("REGULAR", UserRole.REGULAR.getRole());
        assertEquals("ADMIN", UserRole.ADMIN.getRole());
        assertEquals("SUPERADMIN", UserRole.SUPERADMIN.getRole());
        assertTrue(UserRole.REGULAR.getPermissionLevel() < UserRole.ADMIN.getPermissionLevel());
        assertTrue(UserRole.ADMIN.getPermissionLevel() < UserRole.SUPERADMIN.getPermissionLevel());
    }

    @Test
    void roomTypeNames() {
        assertEquals("LECTURETHEATRE", RoomType.LECTURETHEATRE.getType());
        assertEquals("COMPUTERLAB", RoomType.COMPUTERLAB.getType());
        assertEquals("MEETINGROOM", RoomType.MEETINGROOM.getType());
        assertEquals("SEMINARROOM", RoomType.SEMINARROOM.getType());
        assertEquals(4, RoomType.values().length);
    }

    private static Set<String> features(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }
}
