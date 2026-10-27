package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;

public class AddUserCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length == 1) {
            crobs.addUser(details[0]);
            System.out.println("Successfully add a new regular user");
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }

        return false;
    }
}
