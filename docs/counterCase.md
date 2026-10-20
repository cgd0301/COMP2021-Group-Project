## [REQ2 addUser]
1. User name is empty
-> Output name should not be empty

2. Incorrect number parameter input
-> Output number of inputs is incorrect

## [REQ3 removeUser]
1. Target user ID not in the current users ID list
-> Output target user ID not in current user IDs list

2. Incorrect formate of target user ID
-> Output incorrect format warning

3. Incorrect number of parameter input
-> Output number of inputs is incorrect

4. Target user ID is not regular user ID
-> Output the authority warning: admin user do not have authority to delete admin/superadmin user

5. Delete user will delete all his bookings, not only including the future bookings

## [REQ4 changeRole]
1. The superadmin user modify a regular/admin user to be a superadmin
-> Since there is only one superadmin, provide a double check to the superadmin. Then change the regular user to be superadmin, the superadmin to be admin.

2. Target user ID is incorrect or not in the user ID list
-> Output input incorrect: user ID not exist

3. New Role paramter is not superadmin/admin/regular
-> Output input incorrect: newRole should be one of superadmin/admin/regular.

4. Target user ID is the superadmin's ID itself
-> Output input incorrect: cannot modify your own role, you can only transfer your superadmin to others by changing that one's role to be superadmin (which then makes you to be admin user)

## [REQ5 login]
1. userID not included in current user ID list
-> Output input incorrect: input ID does not exist 

2. userID format is wrong
-> Output input incorrect: format of user ID is incorrect

3. When user has already log in, he/she repeatedly log in
-> Output logging situation incorrect: you have already logged in

## [REQ6 logout]
1. The user has not logged in yet
-> Output logging situation incorrect: you have not logged in yet

2. The user has already logged out
-> Output logging situation incorrect: you have already logged out

## [REQ7 addRoom]
1. roomType not in the supported list
-> Ouput input incorrect: the roomType is not in the supported list, which only allows lectureTheatre, computerLab, seminarRoom, and meetingRoom

2. There is no features input at all
-> Set the features list to be empty list

3. Input capacity is not integer in acceptable range/ is negative
-> Output input incorrect: capacity should be positive integer smaller or equal to 65535

## [REQ8 modifyRoom]
1. roomID not exists
-> Output input incorrect: roomID not exists in current room ID list

2. When field is roomType, the new value is not in the supported list
-> Output input incorrect: the new value for roomType should be included in the supported list

3. When field is features
-> the new value could be empty

4. When field is capacity
-> it should be positive and not larger than 65535

5. field is not valid inpuy
-> Output input incorrect: invalid input

## [REQ9 deletRoom]
1. Input invalid
-> Output input incorrect: invalid input

2. roomID does not exist
-> Output input incorrect: roomID does not exist

3. Delete room will delete the booking of this room in future, but not delete the booking on progress or in the past.

## [REQ10 book]
1. User has not logged in
-> Output logging situation incorrect: You have not logged in

2. roomID does not exist
-> Output input incorrect: roomID does not exist

3. Time conflict
-> Output the current room at appointed time has been occupied

4. endTime before startTime
-> Output the endTime should be after the startTime

5. purpose could be empty

6. time is before current time (cannot book the room before current time)
-> Output time error: appointed time should not before current time

## [REQ11 modifyBooking]
1. bookingID does not exist
-> Output input incorrect: booking ID does not exist

2. Authority problem
-> the booking not belong to the logged-in user

Same counter cases as REQ10 (except REQ10 6; REQ10 3: time conflict excludes the time period of booking to be modified when new booking's room is same as original one)

## [REQ12 deletebooking]
1. booking ID does not exits
-> Output input incorrect: booking ID does not exist

2. current user ID is not the same as ownerID of the booking
-> Output userID does not match

3. Delete booking will remove the booking from all bookings rather than changing the status (there's not status field for a booking)

## [REQ13 search]
1. time error, start time later than end time
-> Output time error: start time should be before end time

2. minCapacity is negative or 0 or larger than 65535
-> Output input incorrect: minCapacity should be positive integer smaller or equal to 65535

3. features could be empty
-> Then features would not serve as a filter factor

## [REQ14 schedule]
1. roomID does not exist
-> Output input incorrect: roomID does not exist

2. Authority checking for regular user or admin user

3. If a room bookings gets across the mid-night of the counter time of 7-day
-> it still will be showcased

## [REQ15 listRoom]
1. roomID does not exist
-> Output input incorrect: roomID does not exist

2. feature is empty
-> When printing the features, prints NO FEATURES

## [REQ16 listAllRooms]
1. There is no room
-> Output there is no room

## [REQ17 listBookings]
1. If the user has not logged in
-> Output logging situation incorrect: You have not logged in

2. List bookings will list all the bookings made by user, including past, current and future, sorted by date and start time.
## [REQ18 quit]







[All Counter Cases]
1. Input paramters format error
2. Number of input paramters error
3. Authority error






