Date & Time:YYYY-MM-DD HH:mm:ss  e.g. 2026-10-18 14:30:45
UserID: Sxxxxxx (x is digit)     e.g. S123456
RoomID: Rxxx (x is digit)        e.g. R003
BookingID: Bxxxxxx (x is digit)  e.g. B123456

[REQ13 search]
Features should be represented as a comma-separated list without
spaces.(case-insensitive)
If there is not feature parameter input in the search function, we would only consider the paramters left as filter factor.


[REQ10 book]
Purpose paramter could be written into a sentence with space.

What if there lacks one or more paramters?
parameters for each function((par) for non-compulsory paramter)
addUser: name
removerUser: targetUserID
changeRole: targetUserID newwRole
login: userID
logout:
addRoom: roomType capacity (features)
modifyRoom: roomID field newValue
deleteRoom: roomID
book: roomID date startTime endTime (purpose)
modifyBooking: bookingID roomID date startTime endTime (purpose)
deleteBooking: bookingID
search: date startTime endTime minCapacity (features)
schedule: roomID weekStartdate
listRoom: roomID
listAllRooms:
listBookings:
quit: