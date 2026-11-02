package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            throw new CrobsException("Incorrect number of input parameters");
        }
        String[] details = detail.split("\\s+");
        if (details.length == 2) {
            System.out.printf("%-10s %-10s %-10s %-15s %-15s\n", "BookingID", "OwnerUserID", "Date",
                    "Start time", "End time");
            System.out.println("---------------------------------------------------------");

            LocalDate weekStartDate = PmtParse.parseLocalDate(details[1]);
            Map<Booking, String> scheduleBookings = crobs.schedule(details[0].toUpperCase(Locale.ROOT),
                    weekStartDate);
            for (Map.Entry<Booking, String> pair : scheduleBookings.entrySet()) {
                Booking booking = pair.getKey();
                String userID = pair.getValue();
                System.out.printf("%-10s %-10s %-10s %-15s %-15s\n", booking.getBookingID(), userID,
                        booking.getDate(), booking.getStartTime(), booking.getEndTime());
            }

            System.out.println("---------------------------------------------------------");
        } else {
            throw new CrobsException("Incorrect number of input parameters");
        }
        return false;
    }
}
