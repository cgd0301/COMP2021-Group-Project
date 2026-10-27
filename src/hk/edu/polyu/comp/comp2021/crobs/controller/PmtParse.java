package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.model.RoomType;

import java.util.Locale;

public class PmtParse {
    private static final int MAX_CAPACITY = 65535;

    private PmtParse() {
    }

    static RoomType parseRoomType(String command) {
        command = command.toUpperCase(Locale.ROOT);
        try {
            return RoomType.valueOf(command);
        } catch (RuntimeException ex) {
            throw new CrobsException("Invalid roomType parameter \n" +
                    "Allowed values are: LECTURETHEATRE, COMPUTERLAB, " +
                    "MEETINGROOM, SEMINARROOM (case-insensitive)");
        }
    }

    static int parseIntCapacity(String command) {
        if (command.matches("[0-9]+")) {
            long minCapacity = Integer.parseInt(command);
            if (minCapacity <= 0 || minCapacity > MAX_CAPACITY) {
                throw new CrobsException("Invalid minCapacity paramter " +
                        "(should be positive integer smaller or equal to 65535)");
            } else {
                return Math.toIntExact(minCapacity);
            }
        } else {
            throw new CrobsException("Invalid minCapacity paramter " +
                    "(should be positive integer smaller or equal to 65535)");
        }
    }
}
