package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;


public class RemoveUserCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length == 1) {
            crobs.removeUser(details[0].toUpperCase());
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }

        System.out.printf("Successfully remove the target user (userID: %s)\n", details[0]);
        return false;
    }
}
