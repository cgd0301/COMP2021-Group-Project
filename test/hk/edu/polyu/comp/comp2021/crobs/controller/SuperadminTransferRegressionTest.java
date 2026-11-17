package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.model.UserRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SuperadminTransferRegressionTest {
    @Test
    void originalActorCanUndoAndRedoOwnTransferButOthersCannot() {
        CROBS crobs = new CROBS();
        crobs.login("S000000");
        crobs.addUser("Alice");
        crobs.addUser("Bob");
        UndoRedoManager history = new UndoRedoManager();
        history.execute(crobs, new ChangeRoleCommand(), "S000001 SUPERADMIN");
        crobs.logout();
        assertThrows(CrobsException.class, () -> history.undo(crobs));
        crobs.login("S000002");
        assertThrows(CrobsException.class, () -> history.undo(crobs));
        crobs.logout();
        crobs.login("S000001");
        assertThrows(CrobsException.class, () -> history.undo(crobs));
        assertTrue(history.canUndo());
        crobs.logout();
        crobs.login("S000000");
        for (int i = 0; i < 3; i++) {
            history.undo(crobs);
            assertEquals(UserRole.SUPERADMIN, crobs.findUser("S000000").getRole());
            assertEquals(UserRole.REGULAR, crobs.findUser("S000001").getRole());
            history.redo(crobs);
            assertEquals(UserRole.ADMIN, crobs.findUser("S000000").getRole());
            assertEquals(UserRole.SUPERADMIN, crobs.findUser("S000001").getRole());
        }
    }
}
