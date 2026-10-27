package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;


public class ChangeRoleCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }
        String[] details = detail.split("\\s+");
        if (details.length == 2) {
            UserRole newRole = getNewRole(details);
            crobs.changeRole(details[0].toUpperCase(), newRole);
            System.out.printf("Successfully change the role of target user " +
                    "(userID: %s, current role: %s)\n", details[0], newRole.getRole());
        } else {
            throw new CrobsException("Incorrect number of parameters");
        }

        return false;
    }

    private static UserRole getNewRole(String[] details) {
        String role = details[1].toUpperCase();
        UserRole newRole;
        if (role.equals("REGULAR")) {
            newRole = UserRole.REGULAR;
        } else if (role.equals("ADMIN")) {
            newRole = UserRole.ADMIN;
        } else if (role.equals("SUPERADMIN")) {
            newRole = UserRole.SUPERADMIN;
        } else {
            throw new CrobsException("Invalid input parameters, newRole can only be " +
                    "REGULAR, ADMIN, and SUPERADMIN(case-insensitive)");
        }
        return newRole;
    }
}
