package hk.edu.polyu.comp.comp2021.crobs.model;



import java.lang.reflect.Array;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class CROBS {
    public User currentUser;
    private long nextUserNum = 1;
    private long nextRoomNum = 0;
    private long nextBookingNum = 0;
    private final Clock clock;
    private static final int MAXCAPACITY = 65535;
    private final HashMap<String, User> allUsers = new HashMap<>();
    private final HashMap<String, Room> allRooms = new HashMap<>();
    private final HashMap<String, Booking> allBookings = new HashMap<>();
    private final HashMap<String, List<Booking>> userBookings = new HashMap<>();
    private final HashMap<String, List<Booking>> roomBookings = new HashMap<>();
    private final Comparator<Booking> byDate = Comparator.comparing(Booking::getDate)
            .thenComparing(Booking::getStartTime).thenComparing(Booking::getBookingID);
    private final Comparator<Room> byRoomID = Comparator.comparing(Room::getRoomID);

    public CROBS() {
        this.allUsers.put("S000000", new User("superadmin", "S000000", UserRole.SUPERADMIN));
        this.userBookings.put("S000000", new LinkedList<>());
        this.clock = Clock.systemDefaultZone();
    }

    public CROBS(Clock clock) {
        this.allUsers.put("S000000", new User("superadmin", "S000000", UserRole.SUPERADMIN));
        this.userBookings.put("S000000", new LinkedList<>());
        this.clock = clock;
    }

    public void login(String userID) {
        User user = allUsers.get(userID);
        try {
            this.checkLoggedIn();
        } catch (RuntimeException ex) {
            if (user == null) {
                throw new CrobsException("User ID not found");
            } else {
                this.currentUser = user;
                return;
            }
        }
        throw new CrobsException("Already logged in");
    }

    public void logout() {
        this.checkLoggedIn();
        this.currentUser = null;
    }

    public void addUser(String name) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        String userID = String.format("S%06d", nextUserNum++);
        User newUser = new User(name, userID, UserRole.REGULAR);
        this.allUsers.put(userID, newUser);
        this.userBookings.put(userID, new LinkedList<Booking>());
    }

    public void removeUser(String targetUserID) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        User targetUser = allUsers.get(targetUserID);
        if (targetUser == null) {
            throw new CrobsException("Input target user ID not found");
        } else if (targetUser.getRole() != UserRole.REGULAR){
            throw new CrobsException("Removing (super) admin users is not allowed");
        } else {
            List<Booking> userBookings = this.userBookings.get(targetUserID);
            for (Booking booking : userBookings) {
                String roomID = booking.getRoomID();
                String bookingID = booking.getBookingID();
                this.roomBookings.get(roomID).remove(booking);
                this.allBookings.remove(bookingID);
            }
            this.userBookings.remove(targetUserID);
            this.allUsers.remove(targetUserID);
        }
    }

    public void changeRole(String targetUserID, UserRole newRole) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.SUPERADMIN);

        String currentUserID = currentUser.getUserID();
        User targetUser = allUsers.get(targetUserID);
        if (currentUserID.equals(targetUserID)) {
            throw new CrobsException("You can not change your own role");
        }
        if (targetUser == null) {
            throw new CrobsException("Input target user ID not found");
        }
        if (newRole == UserRole.SUPERADMIN) {
            currentUser.setRole(UserRole.ADMIN);
        }
        targetUser.setRole(newRole);
    }

    public String addRoom(RoomType roomType, int capacity, Set<String> features) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        if (capacity > MAXCAPACITY || capacity <= 0) {
            throw new CrobsException("Invalid minCapacity paramter " +
                    "(should be positive integer smaller or equal to 65535)");
        }

        String roomID = String.format("R%03d", nextRoomNum++);
        Room newRoom = new Room(roomID, roomType, capacity, features);
        this.allRooms.put(roomID, newRoom);
        this.roomBookings.put(roomID, new LinkedList<Booking>());
        return roomID;
    }

    public void modifyRoomType(String roomID, RoomType newType) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        targetRoom.setRoomType(newType);
    }

    public void modifyRoomCapacity(String roomID, int newCapacity) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        if (newCapacity > MAXCAPACITY || newCapacity <= 0) {
            throw new CrobsException("Invalid minCapacity paramter " +
                    "(should be positive integer smaller or equal to 65535)");
        }

        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        targetRoom.setRoomCapacity(newCapacity);
    }

    public void modifyRoomFeatures(String roomID, Set<String> newFeatures) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        targetRoom.setRoomFeatures(newFeatures);
    }

    public void deleteRoom(String roomID) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);
        LocalDate currentDate = LocalDate.now(clock);
        LocalTime currentTime = LocalTime.now(clock);

        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        List<Booking> roomBookings = this.roomBookings.get(roomID);
        List<Booking> roomBookingsTmp = new ArrayList<>(roomBookings);
        for (Booking booking : roomBookingsTmp) {
            if (booking.getDate().isAfter(currentDate) ||
                    (booking.getDate().isEqual(currentDate) && booking.getStartTime().isAfter(currentTime))) {
                this.removeBooking(booking.getBookingID());
            }
        }
        allRooms.remove(roomID);
        if (roomBookings.isEmpty()) {this.roomBookings.remove(roomID);}
    }

    public Room findRoom(String roomID) {
        this.checkLoggedIn();
        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        return targetRoom;
    }

    public Map<String, Room> getAllRooms() {
        checkLoggedIn();
        return Collections.unmodifiableMap(this.allRooms);
    }

    public String book(String roomID, LocalDate date, LocalTime startTime,
                     LocalTime endTime, String purpose) {
        this.checkLoggedIn();
        this.checkTimeValidation(startTime, endTime);
        String userID = this.currentUser.getUserID();

        List<Booking> roomBookings = this.roomBookings.get(roomID);
        List<Booking> userBookings = this.userBookings.get(userID);

        if (roomBookings == null || allRooms.get(roomID) == null) {
            throw new CrobsException("Input target room ID not found");
        }
        for (Booking booking : roomBookings) {
            if (booking.isConflict(date, startTime, endTime)) {
                throw new CrobsException("Booking time overlaps with an existing reservation");
            }
        }
        for (Booking booking : userBookings) {
            if (roomID.equals(booking.getRoomID()) &&
                    booking.isConflict(date, startTime, endTime)) {
                throw new CrobsException("Booking time overlaps with an existing reservation");
            }
        }

        String bookingID = String.format("B%06d", nextBookingNum++);
        Booking newBooking = new Booking(bookingID, userID, roomID,
                date, startTime, endTime, purpose);

        this.allBookings.put(bookingID, newBooking);
        roomBookings.add(newBooking);
        roomBookings.sort(byDate);
        userBookings.add(newBooking);
        userBookings.sort(byDate);

        return bookingID;
    }

    public void deleteBooking(String bookingID) {
        this.checkLoggedIn();

        Booking booking = allBookings.get(bookingID);
        if (booking == null) {
            throw new CrobsException("Input target booking ID not found");
        }

        String currentUserID = currentUser.getUserID();
        String ownerUserID = booking.getUserID();

        if (currentUserID.equals(ownerUserID)) {
            this.removeBooking(bookingID);
        } else {
            throw new CrobsException("Input target booking ID not found");
        }
    }

    private void removeBooking(String bookingID) {
        Booking booking = allBookings.get(bookingID);
        if (booking == null) {
            throw new CrobsException("Input target booking ID not found");
        }
        String ownerUserID = booking.getUserID();
        String roomID = booking.getRoomID();

        this.allBookings.remove(bookingID);
        this.userBookings.get(ownerUserID).remove(booking);
        this.roomBookings.get(roomID).remove(booking);
        if (!allRooms.containsKey(roomID) && this.roomBookings.get(roomID).isEmpty()) {
            this.roomBookings.remove(roomID);
        }
    }

    public void modifyBooking(String bookingID, String roomID, LocalDate date, LocalTime startTime,
                              LocalTime endTime, String purpose) {
        this.checkLoggedIn();
        LocalDate currentDate = LocalDate.now(clock);
        LocalTime currentTime = LocalTime.now(clock);

        this.checkTimeValidation(startTime, endTime);
        if (!allRooms.containsKey(roomID)) {
            throw new CrobsException("Input target room ID not found");
        }
        if (date.isBefore(currentDate) || (date.isEqual(currentDate) &&
                startTime.isBefore(currentTime))) {
            throw new CrobsException("Booking can not be modified to a past date/time");
        }

        Booking booking = this.allBookings.get(bookingID);
        if (booking == null) {
            throw new CrobsException("Input target booking ID not found");
        }

        String ownerUserID = booking.getUserID();
        String bookingRoomID = booking.getRoomID();
        LocalDate bookingDate = booking.getDate();
        LocalTime bookingStartTime = booking.getStartTime();
        if (!ownerUserID.equals(this.currentUser.getUserID())) {
            throw new CrobsException("Input target booking ID not found");
        }
        if (bookingDate.isBefore(currentDate) || (bookingDate.isEqual(currentDate) &&
                bookingStartTime.isBefore(currentTime))) {
            throw new CrobsException("Modifying previous booking is not allowed");
        }

        if (roomID.equals(bookingRoomID)) {
            for (Booking bookingTmp : this.roomBookings.get(roomID)) {
                if (!bookingTmp.getBookingID().equals(booking.getBookingID())) {
                    if (bookingTmp.isConflict(date, startTime, endTime)) {
                        throw new CrobsException("Booking time overlaps with an existing reservation");
                    }
                }
            }
        } else {
            for (Booking bookingTmp : this.roomBookings.get(roomID)) {
                if (bookingTmp.isConflict(date, startTime, endTime)) {
                    throw new CrobsException("Booking time overlaps with an existing reservation");
                }
            }
            this.roomBookings.get(bookingRoomID).remove(booking);
            this.roomBookings.get(roomID).add(booking);
            booking.setRoomID(roomID);
        }

        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose(purpose);
        this.roomBookings.get(roomID).sort(byDate);
        this.userBookings.get(ownerUserID).sort(byDate);
    }

    public  List<Booking> getBookings() {
        this.checkLoggedIn();
        String currentUserID = this.currentUser.getUserID();
        return userBookings.get(currentUserID);
    }



    public List<Room> search(LocalDate date, LocalTime starTime, LocalTime endTime,
                            int minCapacity, Set<String> targetFeatures) {
        this.checkLoggedIn();
        this.checkTimeValidation(starTime, endTime);

        List<Room> searchResult = new LinkedList<>();
        for (Room room : allRooms.values()) {
            boolean satisfies = room.satisfies(minCapacity, targetFeatures);
            List<Booking> roomBookings = this.roomBookings.get(room.getRoomID());
            for (Booking booking : roomBookings) {
                if (booking.isConflict(date, starTime, endTime)) {
                    satisfies = false;
                    break;
                }
            }
            if (!room.getFeatures().containsAll(targetFeatures)) {
                satisfies = false;
            }
            if (satisfies) {
                searchResult.add(room);
            }
        }
        searchResult.sort(byRoomID);
        return searchResult;
    }

    public Map<Booking, String> schedule(String roomID, LocalDate weekStartDate) {
        this.checkLoggedIn();

        Map<Booking, String> scheduleResult = new TreeMap<>(byDate);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        if (this.allRooms.get(roomID) == null) {
            throw new CrobsException("Input target room ID not found");
        }
        List<Booking> roomBookings = this.roomBookings.get(roomID);
        for (Booking booking : roomBookings) {
            LocalDate bookingDate = booking.getDate();
            if ((bookingDate.isAfter(weekStartDate) && bookingDate.isBefore(weekEndDate)) ||
                    bookingDate.isEqual(weekStartDate) || bookingDate.isEqual(weekEndDate)) {
                if (this.currentUser.getRole() == UserRole.REGULAR) {
                    if (booking.getUserID().equals(this.currentUser.getUserID())) {
                        scheduleResult.put(booking, this.currentUser.getUserID());
                    } else {
                        scheduleResult.put(booking, "");
                    }
                } else {
                    scheduleResult.put(booking, booking.getUserID());
                }
            }
        }
        return scheduleResult;
    }









    public void checkLoggedIn() {
        if (this.currentUser == null) {
            throw new CrobsException("Not yet logged in");
        }
    }

    public void checkAuthority(User user, UserRole minAuthority) {
        if (!user.compare(minAuthority)) {
            throw new CrobsException("Insufficient permission");
        }
    }

    public void checkTimeValidation(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new CrobsException("Invalid time: start time must be earlier than end time");
        }
    }

    public void checkAuthority(UserRole minAuthority) {
        if (!this.currentUser.compare(minAuthority)) {
            throw new CrobsException("Insufficient permission");
        }
    }

    public User findUser(String userID) {
        return allUsers.get(userID);
    }

    public UserRole getCurrentUserRole() {
        return this.currentUser.getRole();
    }


    public void addSpecificUser(User user) {
        allUsers.put(user.getUserID(), user);
        userBookings.putIfAbsent(user.getUserID(), new LinkedList<Booking>());
    }

}
