package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.security.cert.CRLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.time.format.ResolverStyle;

public class BookCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }
        String[] details = detail.split("\\s+", 5);
        for (int i = 0; i < 4; i++) {
            if (details[i] == null) {
                throw new CrobsException("Incorrect number of input parameters");
            }
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        try {
            date = LocalDate.parse(details[1], dateFormatter);
            startTime = LocalTime.parse(details[2], timeFormatter);
            endTime = LocalTime.parse(details[3], timeFormatter);
        } catch (DateTimeException ex) {
            throw new CrobsException("Invalid date or time format");
        }
        if (details[4] == null) {
            details[4] = "";
        }
        String bookingID = crobs.book(details[0], date, startTime, endTime, details[4]);
        System.out.printf("Successfully make a booking (bookingID: %s)\n", bookingID);
        return false;
    }
}
