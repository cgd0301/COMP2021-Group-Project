## Class: Booking

- Known: bookingID, ownerID, roomID, date, start time, end time, purpose
- Responsibility:
  1. Create a new Booking (Helper: null)
  2. Update information of booking (Helper: null)
  3. Return owner's ID (Helper: null)
  4. Return whether this booking overlaps the given time (Helper: null)
  5. Return whether this booking is in a given period (Helper: null)
  6. Return whether this booking is a future booking based on the given current time, excluding the booking that is currently on progress(Helper: null)


## Class: User

- Known: name, userID, role
- Responsibility:
  1. Update user's role (Helper: null)
  2. Configure a new user of given name, userID, and role (Helper: null)
  3. Return the role information of user (Helper: null)



## Class: Room

- Known: roomID, room type, capacity, features
- Responsibility:
  1. Update room type, capacity and features information (Helper: null)
  2. Configure a new room of specific requiremnts (Helper: null)
  3. Return room information, including room ID, room type, capacity, features(Helper: null)
  4. Check whether the room's features satisfy the given features. (Helper: null)
  5. Check whther the room's capacity satisfies the mini-capacity (Helper: null)



## CROBS

- Known: current user, current time, all users (dictionary; key=userID; value=user), all rooms (dictionary; key=roomID; value=room), all bookings (dictionary; key=bookingID; value=booking)
- Responsibility:
  1. [login] Help user log in the system(Helper: User)
  2. [logout] Help user log out the system (Helper: null)
  3. [addUser] Create a unique user ID, ask User to create a new regular user of the user ID, and add new regular user to all users only for (super)admin user (Helper: User)
  4. [removeUser] Check current user authority, if (super)admin, ask User to return target user's role. Then check if the target user is a regular user, if so, delete bookings whose ownerID is targetuserID by CROBS and delete user from all users. (Helper: User)
  5. [changeRole] Find the user, check whether the userID is the same as current userID, if not, ask user to update role for only superadmin user, if the user is to be changed to superadmin, the role of current user should be changed to an admin user (Helper: User)
  6. [addRoom] Check authority, if (super)admin, create a new unique roomID and ask Room to create new room of the ID and add the new room to all rooms (Helper: Room (for creating a new roon))
  7. [modifyRoom] Check authority, if (super)admin, find the room and ask Room to update room information (Helper: Room)
  8. [deleteRoom] Check authority, if (super)admin, find the room's booking in all bookings, ask Booking to test whether the booking is in the future, if so, delete them from all bookings. Then delete the room from all rooms (Helper: Booking)
  9. [listRoom] Ask Room to return room's information and list the room information (Helper: Room)
  10. [listAllRooms] Ask Room to return each room's information and list all rooms in sorted ascending order of roomID (Helper: Room)
  11. [book] Check whether a new booking time overlaps an old one in all bookings of the room, if not, create a new unique bookingID and ask Booking to create a new booking of the ID whose owner ID is current user's ID and add it to all bookings (Helper: Booking)
  12. [modifyBooking] Check whether the ownerID of the booking to be modified is the same as current userID or not, if so, for each booking of the (new) room in all bookings, ask Booking to inspect whether the modified booking time overlaps the room bookings (if roomID does not change, except current bookings to be modified), if not, ask Booking to update the inforamtion of the booking to be modified.(Helper: Booking, Room)
  13. [deleteBooking] Ask Booking to return owner's ID and check whether it is same as current user ID, if so, delete booking whose bookingID is same as input bookingID from all bookings (Helper: null)
  14. [listBookings] For each booking in all bookings, ask Booking to return ownerID and list bookings whose ownerID is the same as current user ID, be sorted by date and start time (Helper: Booking)
  15. [search] For each room in all rooms, ask Room to return the room's capacity and to check whether room's features satisfies the input features. For the room satisfies capacity and features, check bookings in all bookings whose roomID is same as the room and whether there exists booking overlaps, if not, return it as eligible room (Helper: Room, Booking)
  16. [schedule] For each booking in all bookings whose roomID is same as input roomID, ask Booking to check if it is in the given period, if so, list out the booking information (date, start time) in sorted order. Ask User to check whether current user's role is (super)admin, if so, show the corresponding userID for all book, if not, only show the userID when the ownerID of booking is same as userID of current user. Result sorted by date and start time (User, Booking, Room)

  17. Create a superadmin user of fixed user ID at the start of the programme, and put into the all users (Helper: User)

  - If needed, check authority and logging status 
  - time includes date, start time, and end time

