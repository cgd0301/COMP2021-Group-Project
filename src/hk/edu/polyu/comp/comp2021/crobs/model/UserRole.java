package hk.edu.polyu.comp.comp2021.crobs.model;

public enum UserRole {
    REGULAR(0, "REGULAR"),
    ADMIN(1, "ADMIN"),
    SUPERADMIN(2, "SUPERADMIN");

    private final int permissionLevel;
    private final String role;

    private UserRole(int permissionLevel, String role) {
        this.permissionLevel = permissionLevel;
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }

    public int getPermissionLevel() {
        return this.permissionLevel;
    }
}
