package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions.*;

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



    private void loginAs(UserRole role) {
        if (role != UserRole.SUPERADMIN ){
            crobs.currentUser = new User("Tester", "S999999", role);
            crobs.addSpecificUser(crobs.currentUser);
        } else {
            crobs.login("S000000");
        }
    }
}