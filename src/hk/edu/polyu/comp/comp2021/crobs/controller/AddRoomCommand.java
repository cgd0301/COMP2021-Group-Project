package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddRoomCommand implements Command {

    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length == 2) {
            String roomID = crobs.addRoom(PmtParse.parseRoomType(details[0]),
                    PmtParse.parseIntCapacity(details[1]), new HashSet<String>());
            System.out.printf("Successfully add a room (roomID: %s)\n", roomID);
        } else if (details.length == 3) {
            Set<String> features = new HashSet<>();
            String[] featureDetails = details[2].split(",");
            for (String featureDetail : featureDetails) {
                features.add(featureDetail.toUpperCase(Locale.ROOT));
            }
            String roomID = crobs.addRoom(PmtParse.parseRoomType(details[0]),
                    PmtParse.parseIntCapacity(details[1]), features);
            System.out.printf("Successfully add a room (roomID: %s)\n", roomID);
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }

        return false;
    }


}
