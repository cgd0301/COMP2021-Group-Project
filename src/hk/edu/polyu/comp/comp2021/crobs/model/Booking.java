package hk.edu.polyu.comp.comp2021.crobs.model;


import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
    private String bookingID;
    private String userID;
    private String roomID;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;

    public Booking(String bookingID, String userID, String roomID,
                   LocalDate date, LocalTime startTime, LocalTime endTime, String purpose) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.roomID = roomID;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
    }

    public boolean isSameRoom(Booking booking) {
        return this.roomID.equals(booking.getRoomID());
    }

    public boolean isConflict(Booking booking) {
        return this.isConflict(booking.getDate(), booking.getStartTime(), booking.getEndTime());
    }

    public boolean isConflict(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return this.date.equals(date) && this.startTime.isBefore(endTime)
                && this.endTime.isAfter(startTime);
    }

    public String getBookingID() {
        return this.bookingID;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getRoomID() {
        return this.roomID;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
