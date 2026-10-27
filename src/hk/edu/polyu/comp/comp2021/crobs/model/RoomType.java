package hk.edu.polyu.comp.comp2021.crobs.model;

public enum RoomType {
    LECTURETHEATRE("LECTURETHEATRE"),
    COMPUTERLAB("COMPUTERLAB"),
    MEETINGROOM("MEETINGROOM"),
    SEMINARROOM("SEMINARROOM");
    private final String roomType;

    private RoomType(String roomType) {
        this.roomType = roomType;
    }


    public String getType() {
        return this.roomType;
    }
}
