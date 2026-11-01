package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.security.cert.CRLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.time.format.ResolverStyle;
import java.util.Locale;

public class BookCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }
        String[] details = detail.split("\\s+", 5);
        if (details.length < 4) {
            throw new CrobsException("Incorrect number of input parameters");
        }
        String purpose = details.length == 5 ? details[4] : "";

        LocalDate date = PmtParse.parseLocalDate(details[1]);
        LocalTime startTime = PmtParse.parseLocalTime(details[2]);
        LocalTime endTime = PmtParse.parseLocalTime(details[3]);

        String bookingID = crobs.book(details[0].toUpperCase(Locale.ROOT), date, startTime, endTime, purpose);
        System.out.printf("Successfully make a booking (bookingID: %s)\n", bookingID);
        return false;
    }
}
