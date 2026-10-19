## Class: Booking

- Known: bookingID, ownerID, roomID, date, start time, end time, purpose
- Responsibility:
  1. Create a new Booking (Helper: null)
  2. Update information of booking (Helper: null)
  3. Return owner's ID (Helper: null)
  4. Return whether this booking overlaps the given time (Helper: null)


## Class: User

- Known: name, userID, role, personal bookings
- Responsibility:
  1. Update user's role (Helper: null)
  2. Configure a new user of role to be regular (Helper: null)
  3. Add booking ID to personal bookings (Helper: null)
  4. Delete booking ID from personal bookings (Helper: null)
  5. List all bookings in personal bookings (Helper: null)
  6. Return all personal bookings (Helper: null)


## Class: Room

- Known: roomID, room type, capacity, features, room bookings
- Responsibility:
  1. Update room type, capacity and features information (Helper: null)
  2. Configure a new room of specific requiremnts (Helper: null)
  3. Return room corresponding bookings (Helper: null)
  4. Return room information, including room ID, room type, capacity, features(Helper: null)
  5. Add booking to room bookings (Helper: null)
  6. Delete booking from room bookings (Helper: null)
  7. For each booking in room bookings, ask Booking to check whether the given time of new booking overlaps the bookings in room bookings, if there exists overlapping, return true as result of detection of overlapping (Helper: Booking)
  8. Check whether the given userID is the owner ID of booking in room bookings, ask Booking to return ownerID for checking (Helper: Booking)
  9. Check whether the room's features satisfy the given features. (Helper: null)
  10. Check whther the room's capacity satisfies the mini-capacity (Helper: null)



## CROBS

- Known: current user, all users, all rooms, all bookings
- Responsibility:
  1. Help user log in the system (Helper: User)
  2. Help user lo g out the system (Helper: null)
  3. Ask User to create a new regular user and sdd new regular user to all users only for admin user(Helper: User (for creating new regular user))
  4. Ask User to return personal bookings, and for each room in all rooms, ask Room to check whether this room has the user's booking and delete them, and delete the bookings from all bookings. (Helper: User, Room, Booking)
  5. Find the user and ask user to update role for only superadmin user(Helper: User)
  6. Ask Room to create new room and add the new room (Helper: Room (for creating a new roon))
  7. Find the room and ask Room to update room information (Helper: Room)
  8. Ask Room to return the room's corresponding bookings and delete room from all rooms, delete corresponding bookings from all bookings(Helper: Booking)
  9. Ask Room to return room's information and list the room information (Helper: Room)
  10. Ask Room to return each room's information and list all rooms (Helper: Room)
  11. Ask Room to check whether a new booking time overlaps an old one at same room, if not, ask Booking to create a new booking, ask User to add it to personal bookings, ask Room to add it to room bookings, and add it to all bookings (Helper: User, Booking, Room)
  12. Ask Room to delete the booking from room bookings and to inspect whether the modified booking time overlaps the room bookings (except current bookings to be modified), if not, ask Booking to update the inforamtion of the booking to be modified. (Helper: User, Booking, Room)
  13. Ask Booking to get owner's ID, ask user to delete the booking from personal bookings, ask Room to delete the booking from room bookings, and delete it from all bookings (Helper: User, Booking, Room)
  14. Ask User to list all bookings in personal bookings (Helper: Booking)
  15. For each room in all rooms, ask Room to return the room's capacity, to check whether the room is available during the given time period and to check whether room's features satisfies the input features, then return all eligible rooms (Helper: Room)
  16. Ask Room to return room bookings, for each booking in the room bookings, ask Booking to check if it is overlapped with given period, if so, list out the booking information (date, start time) in sorted order. Ask User to check whether current user's role is admin, if so, show the corresponding userID for all book, if not, do not show. (User, Booking, Room)

