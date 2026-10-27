package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.HashMap;

public class CROBS {
    public User currentUser;
    private long nextUserNum = 1;
    private final HashMap<String, User> allUsers = new HashMap<>();

    public CROBS() {
        this.currentUser = null;
        this.allUsers.put("S000000", new User("superadmin", "S000000", UserRole.SUPERADMIN));
    }

    public void login(String userID) {
        User user = allUsers.get(userID);
        try {
            this.checkLoggedIn();
        } catch (RuntimeException ex) {
            if (user == null) {
                throw new CrobsException("User ID not found");
            } else {
                this.currentUser = user;
                System.out.println("Successfully logged in");
                return;
            }
        }
        throw new CrobsException("Already logged in");
    }

    public void logout() {
        this.checkLoggedIn();
        this.currentUser = null;
        System.out.println("Successfully logged out");
    }

    public void addUser(String name) {
        this.checkLoggedIn();
        this.checkAuthority(currentUser, UserRole.ADMIN);

        String userID = String.format("S%06d", nextUserNum++);
        User newUser = new User(name, userID, UserRole.REGULAR);
        allUsers.put(userID, newUser);
        System.out.printf("Successfully add a new regular user, userID: %s\n", userID);
    }

    public void removeUser(String targetUserID) {
        this.checkLoggedIn();
        this.checkAuthority(currentUser, UserRole.ADMIN);

        User targetUser = allUsers.get(targetUserID);
        if (targetUser == null) {
            throw new CrobsException("Input target ID not found");
        } else if (targetUser.getRole() != UserRole.REGULAR){
            throw new CrobsException("Removing (super) admin users is not allowed");
        } else {
            // TODO: Delete the target user's bookings from allBookings
            allUsers.remove(targetUserID);
        }
    }

    public void changeRole(String targetUserID, UserRole newRole) {
        this.checkLoggedIn();
        this.checkAuthority(currentUser, UserRole.SUPERADMIN);

        String currentUserID = currentUser.getUserID();
        User targetUser = allUsers.get(targetUserID);
        if (currentUserID.equals(targetUserID)) {
            throw new CrobsException("You can not change your own role");
        }
        if (targetUser == null) {
            throw new CrobsException("Input target user ID not found");
        }
        if (newRole == UserRole.SUPERADMIN) {
            currentUser.setRole(UserRole.ADMIN);
        }
        targetUser.setRole(newRole);
    }

    public void checkLoggedIn() {
        if (this.currentUser == null) {
            throw new CrobsException("Not yet logged in");
        }
    }

    public void checkAuthority(User user, UserRole minAuthority) {
        if (!user.compare(minAuthority)) {
            throw new CrobsException("Insufficient permission");
        }
    }

    public User findUser(String userID) {
        return allUsers.get(userID);
    }

    public void addSpecificUser(User user) {
        allUsers.put(user.getUserID(), user);
    }

}
