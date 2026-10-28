package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.util.Locale;
import java.util.Set;


public class ListRoomCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+");
        if (details.length == 1) {
            Room targetRoom = crobs.findRoom(details[0].toUpperCase(Locale.ROOT));
            System.out.printf("Target room found\n RoomID: %s\n " +
                    "Room type: %s\n Room capacity: %s\n ", targetRoom.getRoomID(),
                    targetRoom.getRoomType().getType(), targetRoom.getCapacity());

            System.out.print("Room features: ");
            Set<String> features = targetRoom.getFeatures();
            if (features == null) {
                System.out.println("NULL");
            } else {
                int count = 0;
                for (String feature : features) {
                    if (count == 0) {
                        System.out.print(feature);
                    } else {
                        System.out.print(", " + feature);
                    }
                    count++;
                }
                System.out.print("\n");
            }
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }
        return false;
    }
}
