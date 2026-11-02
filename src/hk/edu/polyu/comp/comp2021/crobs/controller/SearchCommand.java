package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class SearchCommand implements Command {
    private final int MAX_CAPACITY = 65535;
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }
        String[] details = detail.split("\\s+", 5);
        if (details.length < 4) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        int minCapacity;
        try {
            minCapacity = Integer.parseInt(details[3]);
        } catch (NumberFormatException ex) {
            throw new CrobsException("Invalid minCapacity paramter for searching " +
                    "(should be non-negative integer smaller or equal to 65535 \n");
        }
        if (minCapacity < 0 || minCapacity > MAX_CAPACITY) {
            throw new CrobsException("Invalid minCapacity paramter for searching " +
                    "(should be non-negative integer smaller or equal to 65535) \n");
        }

        LocalDate date = PmtParse.parseLocalDate(details[0]);
        LocalTime startTime = PmtParse.parseLocalTime(details[1]);
        LocalTime endTime = PmtParse.parseLocalTime(details[2]);

        Set<String> features = new HashSet<>();
        if (details.length == 5) {
            String[] strFeatures = details[4].split(",");
            for (int i = 0; i < strFeatures.length; i++) {
                strFeatures[i] = strFeatures[i].toUpperCase(Locale.ROOT);
            }
            features.addAll(Arrays.asList(strFeatures));
        }
        List<Room> searchResult = crobs.search(date, startTime, endTime, minCapacity, features);

        System.out.printf("%-10s %-15s %-10s %-10s \n", "Room ID", "Room type", "Capacity", "Features");
        System.out.println("----------------------------------------------------------");
        for (Room room : searchResult) {
            System.out.printf("%-10s %-15s %-10s ", room.getRoomID(), room.getRoomType().getType(), room.getCapacity());
            Set<String> roomFeatures = room.getFeatures();
            if (roomFeatures == null) {
                System.out.println("NULL");
            } else {
                int count = 0;
                for (String feature : roomFeatures) {
                    if (count == 0) {
                        System.out.print(feature);
                    } else {
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
