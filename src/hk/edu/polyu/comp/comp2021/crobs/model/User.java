package hk.edu.polyu.comp.comp2021.crobs.model;

public class User {
    private String name;
    private String userID;
    private UserRole role;

    User(String name, String userID, UserRole role) {
        this.name = name;
        this.userID = userID;
        this.role = role;
    }

    public void setRole(UserRole newRole) {
        this.role = newRole;
    }

    public boolean compare(UserRole minAuthority) {
        return this.role.getPermissionLevel() >= minAuthority.getPermissionLevel();
    }

    public String getName() {
        return this.name;
    }

    public String getUserID() {
        return this.userID;
    }

    public UserRole getRole() {
        return this.role;
    }
}
