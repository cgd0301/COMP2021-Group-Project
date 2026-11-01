package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.model.RoomType;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
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
            long minCapacity = Long.parseLong(command);
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

    static LocalDate parseLocalDate(String strDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate date;
        try {
            date = LocalDate.parse(strDate, dateFormatter);
        } catch (DateTimeException ex) {
            throw new CrobsException("Invalid date format");
        }
        return date;
    }

    static LocalTime parseLocalTime(String strTime) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalTime time;
        try {
            time = LocalTime.parse(strTime, timeFormatter);
        } catch (DateTimeException ex) {
            throw new CrobsException("Invalid time format");
        }
        return time;
    }
}
