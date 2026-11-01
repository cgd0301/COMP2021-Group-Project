package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;


public class ModifyBookingCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String[] details = detail.split("\\s+", 6);
        if (details.length < 5) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        String purpose = details.length == 6 ? details[5] : "";
        LocalDate date = PmtParse.parseLocalDate(details[2]);
        LocalTime startTime = PmtParse.parseLocalTime(details[3]);
        LocalTime endTime = PmtParse.parseLocalTime(details[4]);
        crobs.modifyBooking(details[0].toUpperCase(Locale.ROOT), details[1].toUpperCase(Locale.ROOT),
                date, startTime, endTime, purpose);

        return false;
    }
}
