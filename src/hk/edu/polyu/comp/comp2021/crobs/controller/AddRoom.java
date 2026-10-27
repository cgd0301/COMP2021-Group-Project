package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddRoom implements Command {

    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length == 2) {
            crobs.addRoom(PmtParse.parseRoomType(details[0]),
                    PmtParse.parseIntCapacity(details[1]), null);
        } else if (details.length >= 3) {
            Set<String> features = new HashSet<>();
            for (int i = 2; i < details.length; i++) {
                features.add(details[i].toUpperCase(Locale.ROOT));
            }
            crobs.addRoom(PmtParse.parseRoomType(details[0]),
                    PmtParse.parseIntCapacity(details[1]), features);
            System.out.println("Successfully add a room");
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }

        return false;
    }


}
