package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Room {
    private String roomID;
    private RoomType roomType;
    private int capacity;
    private Set<String> features;

    public Room(String roomID, RoomType roomType, int capacity, Set<String> features) {
        this.roomID = roomID;
        this.roomType = roomType;
        this.capacity = capacity;
        this.features = new HashSet<>(features);
    }

    public boolean satisfies(int minCapacity, Set<String> features) {
        if (this.capacity < minCapacity) {
            return false;
        }
        if (features != null) {
            for (String s : features) {
                if (!this.features.contains(s)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setRoomType(RoomType newType) {
        this.roomType = newType;
    }

    public void setRoomCapacity(int newCapacity) {
        this.capacity = newCapacity;
    }

    public void setRoomFeatures(Set<String> newFeatures) {
        this.features = new HashSet<>(newFeatures);
    }

    public String getRoomID() {
        return this.roomID;
    }

    public RoomType getRoomType() {
        return this.roomType;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public Set<String> getFeatures() {
        return Collections.unmodifiableSet(this.features);
    }
}
