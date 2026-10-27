package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

public class DeleteRoomCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length == 1) {
            crobs.deleteRoom(details[0]);
            System.out.printf("Successfully delete the target room (roomID: %s)\n", details[0]);
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }
        return false;
    }
}
