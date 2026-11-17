package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CROBSMementoTest {
    @Test
    void mementoRestoresUsersRoomsBookingsAndGeneratedIDCounters() {
        ZoneId zone = ZoneId.systemDefault();
        Clock clock = Clock.fixed(LocalDate.of(2026, 11, 17).atTime(8, 0)
                .atZone(zone).toInstant(), zone);
        CROBS crobs = new CROBS(clock);
        crobs.login("S000000");
        crobs.addUser("Alex");
        String roomID = crobs.addRoom(RoomType.MEETINGROOM, 20, Collections.singleton("PROJECTOR"));
        crobs.book(roomID, LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "original");
        CROBS.Memento memento = crobs.createMemento();

        crobs.modifyRoomCapacity(roomID, 40);
        crobs.modifyBooking("B000000", roomID, LocalDate.of(2026, 12, 2),
                LocalTime.of(12, 0), LocalTime.of(13, 0), "changed");
        crobs.removeUser("S000001");
        crobs.addRoom(RoomType.COMPUTERLAB, 50, Collections.emptySet());

        crobs.restoreMemento(memento);

        assertEquals(1, crobs.getAllRooms().size());
        assertEquals(20, crobs.findRoom(roomID).getCapacity());
        assertEquals(Collections.singleton("PROJECTOR"), crobs.findRoom(roomID).getFeatures());
        assertEquals(1, crobs.getBookings().size());
        assertEquals(LocalDate.of(2026, 12, 1), crobs.getBookings().get(0).getDate());
        assertEquals("original", crobs.getBookings().get(0).getPurpose());
        assertEquals(UserRole.REGULAR, crobs.findUser("S000001").getRole());

        assertEquals("R001", crobs.addRoom(RoomType.COMPUTERLAB, 50, Collections.emptySet()));
        crobs.addUser("Next user");
        assertEquals(UserRole.REGULAR, crobs.findUser("S000002").getRole());
        assertEquals("B000001", crobs.book(roomID, LocalDate.of(2026, 12, 3),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "next booking"));
    }

    @Test
    void restoringMementoPreservesTheActiveLoginSession() {
        CROBS crobs = new CROBS();
        crobs.login("S000000");
        crobs.addUser("Alex");
        CROBS.Memento memento = crobs.createMemento();

        crobs.logout();
        crobs.login("S000001");
        crobs.restoreMemento(memento);

        assertEquals("S000001", crobs.currentUser.getUserID());
        assertEquals(UserRole.REGULAR, crobs.currentUser.getRole());
    }


    @Test
    void restoringMementoWhileLoggedOutKeepsTheSessionLoggedOut() {
        CROBS crobs = new CROBS();
        crobs.login("S000000");
        CROBS.Memento memento = crobs.createMemento();
        crobs.logout();

        crobs.restoreMemento(memento);

        assertTrue(crobs.currentUser == null);
        crobs.login("S000000");
        assertEquals("S000000", crobs.currentUser.getUserID());
    }

    @Test
    void restoringNullMementoIsRejected() {
        CROBS crobs = new CROBS();
        assertThrows(IllegalArgumentException.class, () -> crobs.restoreMemento(null));
        assertTrue(crobs.currentUser == null);
    }
}
