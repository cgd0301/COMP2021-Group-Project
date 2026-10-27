package hk.edu.polyu.comp.comp2021.crobs.model;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class CROBS {
    public User currentUser;
    private long nextUserNum = 1;
    private long nextRoomNum = 0;
    private final HashMap<String, User> allUsers = new HashMap<>();
    private final HashMap<String, Room> allRooms = new HashMap<>();

    public CROBS() {
        this.currentUser = null;
        this.allUsers.put("S000000", new User("superadmin", "S000000", UserRole.SUPERADMIN));
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
        allUsers.put(userID, newUser);
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
            // TODO: Delete the target user's bookings from allBookings
            allUsers.remove(targetUserID);
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

    public void addRoom(RoomType roomType, int capacity, Set<String> features) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

        String roomID = String.format("R%03d", nextRoomNum++);
        Room newRoom = new Room(roomID, roomType, capacity, features);
        this.allRooms.put(roomID, newRoom);
    }

    public void modifyRoomType(String roomID, RoomType newType) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);
        System.out.println(roomID);

        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        targetRoom.setRoomType(newType);
    }

    public void modifyRoomCapacity(String roomID, int newCapacity) {
        this.checkLoggedIn();
        this.checkAuthority(UserRole.ADMIN);

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

        Room targetRoom = this.allRooms.get(roomID);
        if (targetRoom == null) {
            throw new CrobsException("Input target room ID not found");
        }
        //TODO: Delete the future bookings relevant to the room
        allRooms.remove(roomID);
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

    public Set<Room> search(int minCapacity, Set<String> targetFeatures) {
        this.checkLoggedIn();

        Set<Room> searchResult = new HashSet<>();
        for (Room room : allRooms.values()) {
            if (room.satisfies(minCapacity, targetFeatures)) {
                searchResult.add(room);
            }
        }

        //TODO: Consider the overlap time for booking of a room
        return searchResult;
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

    public void checkAuthority(UserRole minAuthority) {
        if (!this.currentUser.compare(minAuthority)) {
            throw new CrobsException("Insufficient permission");
        }
    }

    public User findUser(String userID) {
        return allUsers.get(userID);
    }

    public void addSpecificUser(User user) {
        allUsers.put(user.getUserID(), user);
    }

}
