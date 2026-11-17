package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistorySecurityRegressionTest {
    @Test
    void historyIsRetainedButCannotBeUsedWhenLoggedOutOrByAnotherUser() {
        CROBS crobs = new CROBS();
        crobs.login("S000000");
        crobs.addUser("Alice");
        UndoRedoManager history = new UndoRedoManager();
        history.execute(crobs, new AddRoomCommand(), "MEETINGROOM 20");
        crobs.logout();
        assertThrows(CrobsException.class, () -> history.undo(crobs));
        crobs.login("S000001");
        assertThrows(CrobsException.class, () -> history.undo(crobs));
        assertTrue(history.canUndo());
        crobs.logout();
        crobs.login("S000000");
        history.undo(crobs);
        crobs.logout();
        assertThrows(CrobsException.class, () -> history.redo(crobs));
        crobs.login("S000001");
        assertThrows(CrobsException.class, () -> history.redo(crobs));
        assertTrue(history.canRedo());
        crobs.logout();
        crobs.login("S000000");
        history.redo(crobs);
        assertEquals(1, crobs.getAllRooms().size());
    }

    @Test
    void restoreRefusesToRemoveTheActiveUser() {
        CROBS crobs = new CROBS();
        crobs.login("S000000");
        CROBS.Memento before = crobs.createMemento();
        crobs.addUser("Alice");
        crobs.logout();
        crobs.login("S000001");
        assertThrows(CrobsException.class, () -> crobs.restoreMemento(before));
        assertEquals("S000001", crobs.currentUser.getUserID());
        assertNotNull(crobs.findUser("S000001"));
    }
}
