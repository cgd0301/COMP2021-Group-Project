package hk.edu.polyu.comp.comp2021.crobs.model;



import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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

    /**
     * Creates an internal state snapshot for the undo/redo facility.
     * The active login session is deliberately excluded because login and logout
     * are not undoable operations in the project specification.
     *
     * @return an isolated snapshot of all mutable CROBS data
     */
    public Memento createMemento() {
        Map<String, UserState> users = new HashMap<>();
        for (Map.Entry<String, User> entry : this.allUsers.entrySet()) {
            User user = entry.getValue();
            users.put(entry.getKey(),
                    new UserState(user.getName(), user.getUserID(), user.getRole()));
        }

        Map<String, RoomState> rooms = new HashMap<>();
        for (Map.Entry<String, Room> entry : this.allRooms.entrySet()) {
            Room room = entry.getValue();
            rooms.put(entry.getKey(), new RoomState(room.getRoomID(), room.getRoomType(),
                    room.getCapacity(), room.getFeatures()));
        }

        Map<String, BookingState> bookings = new HashMap<>();
        for (Map.Entry<String, Booking> entry : this.allBookings.entrySet()) {
            Booking booking = entry.getValue();
            bookings.put(entry.getKey(), new BookingState(booking.getBookingID(),
                    booking.getUserID(), booking.getRoomID(), booking.getDate(),
                    booking.getStartTime(), booking.getEndTime(), booking.getPurpose()));
        }

        Map<String, List<String>> userBookingIDs = new HashMap<>();
        Map<String, List<String>> roomBookingIDs = new HashMap<>();
        copyBookingIDs(this.userBookings, userBookingIDs);
        copyBookingIDs(this.roomBookings, roomBookingIDs);

        return new Memento(users, rooms, bookings, userBookingIDs, roomBookingIDs,
                this.nextUserNum, this.nextRoomNum, this.nextBookingNum);
    }

    /**
     * Restores mutable CROBS data from a previously captured snapshot while
     * preserving whichever user is currently logged in.
     *
     * @param memento the snapshot to restore
     */
    public void restoreMemento(Memento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("A state snapshot is required");
        }

        String activeUserID = this.currentUser == null ? null : this.currentUser.getUserID();
        if (activeUserID != null && !memento.userStates().containsKey(activeUserID)) {
            throw new CrobsException("Cannot restore a state that removes the logged-in user");
        }

        this.allUsers.clear();
        for (Map.Entry<String, UserState> entry : memento.userStates().entrySet()) {
            UserState state = entry.getValue();
            this.allUsers.put(entry.getKey(), new User(state.name(), state.userID(), state.role()));
        }

        this.allRooms.clear();
        for (Map.Entry<String, RoomState> entry : memento.roomStates().entrySet()) {
            RoomState state = entry.getValue();
            this.allRooms.put(entry.getKey(),
                    new Room(state.roomID(), state.roomType(), state.capacity(), state.features()));
        }

        this.allBookings.clear();
        for (Map.Entry<String, BookingState> entry : memento.bookingStates().entrySet()) {
            BookingState state = entry.getValue();
            this.allBookings.put(entry.getKey(), new Booking(state.bookingID(), state.userID(),
                    state.roomID(), state.date(), state.startTime(), state.endTime(), state.purpose()));
        }

        this.userBookings.clear();
        for (Map.Entry<String, List<String>> entry : memento.userBookingIDs().entrySet()) {
            List<Booking> restoredBookings = new LinkedList<>();
            for (String bookingID : entry.getValue()) {
                Booking booking = this.allBookings.get(bookingID);
                if (booking != null) {
                    restoredBookings.add(booking);
                }
            }
            this.userBookings.put(entry.getKey(), restoredBookings);
        }

        this.roomBookings.clear();
        for (Map.Entry<String, List<String>> entry : memento.roomBookingIDs().entrySet()) {
            List<Booking> restoredBookings = new LinkedList<>();
            for (String bookingID : entry.getValue()) {
                Booking booking = this.allBookings.get(bookingID);
                if (booking != null) {
                    restoredBookings.add(booking);
                }
            }
            this.roomBookings.put(entry.getKey(), restoredBookings);
        }

        this.nextUserNum = memento.nextUserNum();
        this.nextRoomNum = memento.nextRoomNum();
        this.nextBookingNum = memento.nextBookingNum();
        this.currentUser = activeUserID == null ? null : this.allUsers.get(activeUserID);
    }

    private static void copyBookingIDs(Map<String, List<Booking>> source,
                                       Map<String, List<String>> destination) {
        for (Map.Entry<String, List<Booking>> entry : source.entrySet()) {
            List<String> bookingIDs = new ArrayList<>();
            for (Booking booking : entry.getValue()) {
                bookingIDs.add(booking.getBookingID());
            }
            destination.put(entry.getKey(), bookingIDs);
        }
    }

    /**
     * Immutable snapshot of model state used by the application history manager.
     */
    public static final class Memento {
        private final Map<String, UserState> users;
        private final Map<String, RoomState> rooms;
        private final Map<String, BookingState> bookings;
        private final Map<String, List<String>> userBookingIDs;
        private final Map<String, List<String>> roomBookingIDs;
        private final long nextUserNum;
        private final long nextRoomNum;
        private final long nextBookingNum;

        /**
         * Creates a memento from isolated state data assembled by the enclosing model.
         *
         * @param users copied user state
         * @param rooms copied room state
         * @param bookings copied booking state
         * @param userBookingIDs copied user-to-booking associations
         * @param roomBookingIDs copied room-to-booking associations
         * @param nextUserNum next user identifier counter
         * @param nextRoomNum next room identifier counter
         * @param nextBookingNum next booking identifier counter
         */
        Memento(Map<String, UserState> users, Map<String, RoomState> rooms,
                Map<String, BookingState> bookings, Map<String, List<String>> userBookingIDs,
                Map<String, List<String>> roomBookingIDs, long nextUserNum,
                long nextRoomNum, long nextBookingNum) {
            this.users = new HashMap<>(users);
            this.rooms = new HashMap<>(rooms);
            this.bookings = new HashMap<>(bookings);
            this.userBookingIDs = copyIDLists(userBookingIDs);
            this.roomBookingIDs = copyIDLists(roomBookingIDs);
            this.nextUserNum = nextUserNum;
            this.nextRoomNum = nextRoomNum;
            this.nextBookingNum = nextBookingNum;
        }

        private Map<String, UserState> userStates() {
            return this.users;
        }

        private Map<String, RoomState> roomStates() {
            return this.rooms;
        }

        private Map<String, BookingState> bookingStates() {
            return this.bookings;
        }

        private Map<String, List<String>> userBookingIDs() {
            return this.userBookingIDs;
        }

        private Map<String, List<String>> roomBookingIDs() {
            return this.roomBookingIDs;
        }

        private long nextUserNum() {
            return this.nextUserNum;
        }

        private long nextRoomNum() {
            return this.nextRoomNum;
        }

        private long nextBookingNum() {
            return this.nextBookingNum;
        }

        private Map<String, List<String>> copyIDLists(Map<String, List<String>> source) {
            Map<String, List<String>> result = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : source.entrySet()) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return result;
        }
    }

    private static final class UserState {
        private final String name;
        private final String userID;
        private final UserRole role;

        /**
         * Creates a copied user state.
         *
         * @param name the user's display name
         * @param userID the user's identifier
         * @param role the user's role
         */
        UserState(String name, String userID, UserRole role) {
            this.name = name;
            this.userID = userID;
            this.role = role;
        }

        private String name() {
            return this.name;
        }

        private String userID() {
            return this.userID;
        }

        private UserRole role() {
            return this.role;
        }
    }

    private static final class RoomState {
        private final String roomID;
        private final RoomType roomType;
        private final int capacity;
        private final Set<String> features;

        /**
         * Creates a copied room state.
         *
         * @param roomID the room identifier
         * @param roomType the room type
         * @param capacity the room capacity
         * @param features the room features
         */
        RoomState(String roomID, RoomType roomType, int capacity, Set<String> features) {
            this.roomID = roomID;
            this.roomType = roomType;
            this.capacity = capacity;
            this.features = new HashSet<>(features);
        }

        private String roomID() {
            return this.roomID;
        }

        private RoomType roomType() {
            return this.roomType;
        }

        private int capacity() {
            return this.capacity;
        }

        private Set<String> features() {
            return this.features;
        }
    }

    private static final class BookingState {
        private final String bookingID;
        private final String userID;
        private final String roomID;
        private final LocalDate date;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String purpose;

        /**
         * Creates a copied booking state.
         *
         * @param bookingID the booking identifier
         * @param userID the booking owner
         * @param roomID the booked room
         * @param date the booking date
         * @param startTime the booking start time
         * @param endTime the booking end time
         * @param purpose the booking purpose
         */
        BookingState(String bookingID, String userID, String roomID, LocalDate date,
                     LocalTime startTime, LocalTime endTime, String purpose) {
            this.bookingID = bookingID;
            this.userID = userID;
            this.roomID = roomID;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.purpose = purpose;
        }

        private String bookingID() {
            return this.bookingID;
        }

        private String userID() {
            return this.userID;
        }

        private String roomID() {
            return this.roomID;
        }

        private LocalDate date() {
            return this.date;
        }

        private LocalTime startTime() {
            return this.startTime;
        }

        private LocalTime endTime() {
            return this.endTime;
        }

        private String purpose() {
            return this.purpose;
        }
    }

}
