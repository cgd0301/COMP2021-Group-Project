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
        if (details.length < 2 || details.length > 3) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        details[0] = details[0].toUpperCase(Locale.ROOT);
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
                crobs.modifyRoomFeatures(details[0], new HashSet<String>());
            } else {
                String[] featureDetails = details[2].split(",");
                Set<String> newFeatures = new HashSet<>();
                for (String featureDetail : featureDetails) {
                    newFeatures.add(featureDetail.toUpperCase(Locale.ROOT));
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
