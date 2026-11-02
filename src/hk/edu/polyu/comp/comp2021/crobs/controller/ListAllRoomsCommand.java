package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ListAllRoomsCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail != null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        Map<String, Room> allRooms = crobs.getAllRooms();
        Map<String, Room> sortedAllRooms = new TreeMap<>(allRooms);

        System.out.printf("%-10s %-15s %-10s %-10s \n", "Room ID", "Room type", "Capacity", "Features");
        System.out.println("----------------------------------------------------------");

        for (Map.Entry<String, Room> pair : sortedAllRooms.entrySet()) {
            Room room = pair.getValue();
            System.out.printf("%-10s %-15s %-10s ", room.getRoomID(), room.getRoomType().getType(), room.getCapacity());
            Set<String> features = room.getFeatures();
            if (features == null) {
                System.out.println("NULL");
            } else {
                int count = 0;
                for (String feature : features) {
                    if (count == 0) {
                        System.out.print(feature);
                    }  else {
                        System.out.print(", " + feature);
                    }
                    count++;
                }
                System.out.print("\n");
            }
        }

        System.out.println("----------------------------------------------------------");
        return false;
    }
}
