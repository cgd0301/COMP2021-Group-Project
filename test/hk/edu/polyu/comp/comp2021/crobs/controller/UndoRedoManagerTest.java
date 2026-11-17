package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.model.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.RoomType;
import hk.edu.polyu.comp.comp2021.crobs.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoRedoManagerTest {
    private CROBS crobs;
    private UndoRedoManager history;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        Clock clock = Clock.fixed(LocalDate.of(2026, 11, 17).atTime(8, 0)
                .atZone(zone).toInstant(), zone);
        crobs = new CROBS(clock);
        crobs.login("S000000");
        history = new UndoRedoManager();
    }

    @Test
    void undoAndRedoRestoreRoomAndGeneratedIdentifiers() {
        history.execute(crobs, new AddRoomCommand(), "MEETINGROOM 20 PROJECTOR,WHITEBOARD");
        assertEquals(1, crobs.getAllRooms().size());
        assertEquals("R000", crobs.findRoom("R000").getRoomID());

        history.undo(crobs);
        assertTrue(crobs.getAllRooms().isEmpty());
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());

        history.redo(crobs);
        Room restored = crobs.findRoom("R000");
        assertEquals(20, restored.getCapacity());
        assertEquals(2, restored.getFeatures().size());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void newMutationClearsRedoHistory() {
        history.execute(crobs, new AddRoomCommand(), "MEETINGROOM 20");
        history.undo(crobs);
        history.execute(crobs, new AddRoomCommand(), "COMPUTERLAB 30");

        assertEquals(1, crobs.getAllRooms().size());
        assertEquals("R000", crobs.findRoom("R000").getRoomID());
        assertFalse(history.canRedo());
    }

    @Test
    void undoAndRedoRestoreBookingAndAllBookingIndexes() {
        String roomID = crobs.addRoom(RoomType.MEETINGROOM, 20, Collections.emptySet());
        history.execute(crobs, new BookCommand(),
                roomID + " 2026-12-01 10:00 11:00 team meeting");
        assertEquals(1, crobs.getBookings().size());
        String bookingID = crobs.getBookings().get(0).getBookingID();

        history.undo(crobs);
        assertTrue(crobs.getBookings().isEmpty());
        assertTrue(crobs.schedule(roomID, LocalDate.of(2026, 12, 1)).isEmpty());

        history.redo(crobs);
        assertEquals(bookingID, crobs.getBookings().get(0).getBookingID());
        assertEquals(1, crobs.schedule(roomID, LocalDate.of(2026, 12, 1)).size());
    }

    @Test
    void undoAndRedoRestoreModifiedBookingFields() {
        String roomOne = crobs.addRoom(RoomType.MEETINGROOM, 20, Collections.emptySet());
        String roomTwo = crobs.addRoom(RoomType.SEMINARROOM, 30, Collections.emptySet());
        String bookingID = crobs.book(roomOne, LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "original");

        history.execute(crobs, new ModifyBookingCommand(), bookingID
                + " " + roomTwo + " 2026-12-02 12:00 13:00 revised purpose");
        assertEquals(roomTwo, crobs.getBookings().get(0).getRoomID());
        assertEquals("revised purpose", crobs.getBookings().get(0).getPurpose());

        history.undo(crobs);
        Booking restored = crobs.getBookings().get(0);
        assertEquals(roomOne, restored.getRoomID());
        assertEquals(LocalDate.of(2026, 12, 1), restored.getDate());
        assertEquals(LocalTime.of(10, 0), restored.getStartTime());
        assertEquals("original", restored.getPurpose());

        history.redo(crobs);
        assertEquals(roomTwo, crobs.getBookings().get(0).getRoomID());
        assertEquals(LocalDate.of(2026, 12, 2), crobs.getBookings().get(0).getDate());
    }

    @Test
    void undoAndRedoRestoreDeleteRoomAndItsFutureBookings() {
        String roomID = crobs.addRoom(RoomType.MEETINGROOM, 20, Collections.emptySet());
        crobs.book(roomID, LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "future");

        history.execute(crobs, new DeleteRoomCommand(), roomID);
        assertThrows(CrobsException.class, () -> crobs.findRoom(roomID));
        assertTrue(crobs.getBookings().isEmpty());

        history.undo(crobs);
        assertNotNull(crobs.findRoom(roomID));
        assertEquals(1, crobs.getBookings().size());

        history.redo(crobs);
        assertThrows(CrobsException.class, () -> crobs.findRoom(roomID));
        assertTrue(crobs.getBookings().isEmpty());
    }

    @Test
    void undoAndRedoRestoreRemovedUserAndTheirBookings() {
        crobs.addUser("Alex");
        String userID = "S000001";
        String roomID = crobs.addRoom(RoomType.MEETINGROOM, 20, Collections.emptySet());
        crobs.logout();
        crobs.login(userID);
        crobs.book(roomID, LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "user booking");
        crobs.logout();
        crobs.login("S000000");

        history.execute(crobs, new RemoveUserCommand(), userID);
        assertNull(crobs.findUser(userID));
        assertTrue(crobs.schedule(roomID, LocalDate.of(2026, 12, 1)).isEmpty());

        history.undo(crobs);
        assertNotNull(crobs.findUser(userID));
        assertEquals(1, crobs.schedule(roomID, LocalDate.of(2026, 12, 1)).size());
        assertEquals("S000000", crobs.currentUser.getUserID());

        history.redo(crobs);
        assertNull(crobs.findUser(userID));
        assertTrue(crobs.schedule(roomID, LocalDate.of(2026, 12, 1)).isEmpty());
    }

    @Test
    void undoRestoresBothRolesWhenSuperadminPromotesAnotherUser() {
        crobs.addUser("Alex");
        history.execute(crobs, new ChangeRoleCommand(), "S000001 SUPERADMIN");
        assertEquals(UserRole.ADMIN, crobs.currentUser.getRole());
        assertEquals(UserRole.SUPERADMIN, crobs.findUser("S000001").getRole());

        history.undo(crobs);
        assertEquals(UserRole.SUPERADMIN, crobs.currentUser.getRole());
        assertEquals(UserRole.REGULAR, crobs.findUser("S000001").getRole());

        history.redo(crobs);
        assertEquals(UserRole.ADMIN, crobs.currentUser.getRole());
        assertEquals(UserRole.SUPERADMIN, crobs.findUser("S000001").getRole());
    }

    @Test
    void failedCommandDoesNotEnterHistory() {
        assertThrows(CrobsException.class,
                () -> history.execute(crobs, new AddRoomCommand(), "NOT_A_ROOM 20"));
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoAndRedoWithoutAvailableHistoryAreRejected() {
        assertEquals("Nothing to undo", assertThrows(CrobsException.class,
                () -> history.undo(crobs)).getMessage());
        assertEquals("Nothing to redo", assertThrows(CrobsException.class,
                () -> history.redo(crobs)).getMessage());
    }

    @Test
    void cliLogsUndoAndRedoButDoesNotUndoLoginOrQueryCommands() {
        Cli cli = new Cli();
        StringWriter destination = new StringWriter();
        OperationLog log = new TextOperationLog(destination);

        assertFalse(cli.processLine(crobs, "addRoom MEETINGROOM 20", log));
        assertFalse(cli.processLine(crobs, "listAllRooms", log));
        assertFalse(cli.processLine(crobs, "logout", log));
        assertFalse(cli.processLine(crobs, "login S000000", log));
        assertFalse(cli.processLine(crobs, "undo", log));
        assertTrue(crobs.getAllRooms().isEmpty());
        assertFalse(cli.processLine(crobs, "redo", log));
        assertEquals(1, crobs.getAllRooms().size());

        assertTrue(destination.toString().contains("undo" + System.lineSeparator()));
        assertTrue(destination.toString().contains("redo" + System.lineSeparator()));
    }
}
