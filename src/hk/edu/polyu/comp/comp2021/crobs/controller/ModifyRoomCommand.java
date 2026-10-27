package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ModifyRoomCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length < 2) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        if (details[1].toUpperCase(Locale.ROOT).equals("ROOMTYPE")) {
            if (details.length == 3) {
                crobs.modifyRoomType(details[0], PmtParse.parseRoomType(details[2]));
            } else {
                throw new CrobsException("Incorrect number of input parameters");
            }
        } else if (details[1].toUpperCase(Locale.ROOT).equals("CAPACITY")) {
            if (details.length == 3) {
                crobs.modifyRoomCapacity(details[0], PmtParse.parseIntCapacity(details[2]));
            } else {
                throw new CrobsException("Incorrect number of input parameters");
            }
        } else if (details[1].toUpperCase(Locale.ROOT).equals("FEATURES")) {
            if (details.length == 2) {
                crobs.modifyRoomFeatures(details[0], null);
            } else {
                Set<String> newFeatures = new HashSet<>();
                for (int i = 2; i < details.length; i++) {
                    newFeatures.add(details[i]);
                }
                crobs.modifyRoomFeatures(details[0], newFeatures);
            }
        } else {
            throw new CrobsException("Invalid field paramter " +
                    "(should be ROOMTYPE, CAPACITY, or FEATURES)");
        }

        System.out.printf("Successfully modify an attribute of the target room (roomID: %s)\n", details[0]);
        return false;
    }
}
