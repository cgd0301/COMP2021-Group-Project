package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.util.List;

public class ListBookingsCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail != null) {
            throw new CrobsException("Incorrect number of input parameters");
        }

        List<Booking> bookings = crobs.getBookings();
        System.out.printf("%-10s %-10s %-10s %-15s %-15s %-15s %-20s \n",
                "Booking ID", "User ID", "Room ID", "Date", "Start Time", "End Time", "Purpose");
        System.out.println("--------------------------------------------" +
                "--------------------------------------------------");

        for (Booking booking : bookings) {
            System.out.printf("%-10s %-10s %-10s %-15s %-15s %-15s %-20s \n", booking.getBookingID(),
                    booking.getUserID(), booking.getRoomID(), booking.getDate(),
                    booking.getStartTime(), booking.getEndTime(), booking.getPurpose());
        }

        System.out.println("--------------------------------------------" +
                "--------------------------------------------------");
        return false;
    }
}
